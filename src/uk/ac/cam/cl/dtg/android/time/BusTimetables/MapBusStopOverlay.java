package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.util.ArrayList;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class MapBusStopOverlay extends ItemizedOverlay<BusStopMarker> implements Runnable {

	// Collection of map markers in live usage
	private ArrayList<BusStopMarker> markers = new ArrayList<BusStopMarker>();

	// The context in which we were created (store pointer just so we can fire
	// a dialog
	Context ourContext;

	// The mapview we're on.
	MapView theMap;

	/** The last time we calculated detail levels*/
	private long lastDetailCalc = 0;

	/** how many millis to wait before we calc new detail levels*/
	private static long detailStep = 500;

	/** Zoom level below (i.e. farther our than) which we don't both showing markers*/
	private int maxMarkersZoomLevel = 9;

	// connection to the database
	private DataStoreHelper dsh;
	private SQLiteDatabase conn;

	// Define some stuff for hit detection
	private Rect touchableBounds = new Rect();
	private static final int MIN_TOUCHABLE_WIDTH = 50;
	private static final int MIN_TOUCHABLE_HEIGHT = 50; 

	// Store screen centre so we only re-eval marks when user moves map
	GeoPoint mapCentre = null;
	private int mapLatSpan = 0;
	private int mapLongSpan = 0;
	private int mapZoomLevel = 0;

	/** If set to true, the refresh markers thread will continue running */
	private boolean performUpdates = true;


	/**
	 * Constructor. JDoes nothing except call populate()
	 * 
	 * @param defaultMarker
	 */
	private MapBusStopOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));		
		populate();

	}

	/**
	 * Initialises the Bus Stop overlay
	 * @param defaultMarker Drawable to use as the marker on the overlay
	 * @param con Context the overlay is working within
	 * @param mv MapView the overlay is drawing to
	 */
	public MapBusStopOverlay(Drawable defaultMarker, Context con, MapView mv)
	{
		// Default constructor
		this(defaultMarker);

		// Save references to the context and mapview we're running in
		ourContext = con;
		theMap = mv;

		// Open connection to database for later
		dsh = new DataStoreHelper(ourContext);
		conn = dsh.getReadableDatabase();

	}	

	/**
	 * Recalculates which markers are visible, and displays them on the map
	 */
	public void refreshMarkers() {

		long time = System.currentTimeMillis();

		if(time - lastDetailCalc > detailStep) {

			GeoPoint centre = theMap.getMapCenter();

			int l =  centre.getLongitudeE6() - (int)(theMap.getLongitudeSpan() / 2);
			int r = centre.getLongitudeE6() + (int)(theMap.getLongitudeSpan() / 2);
			int t = centre.getLatitudeE6() + (int)(theMap.getLatitudeSpan() / 2);
			int b = centre.getLatitudeE6() - (int)(theMap.getLatitudeSpan() / 2);

			String sql = "SELECT * FROM busstops WHERE longE6 > "+ l + " AND longE6 < "+r+" AND latE6 > " + b + " AND latE6 < "+t+" ORDER BY dataSet ASC LIMIT 80";

			try {
				Log.i("RefreshMarkers","SQL query is" + sql);

				Cursor markersInView = conn.rawQuery(sql, null);

				Log.i("RefreshMarkers","Rows returned: "+markersInView.getCount());

				markersInView.moveToFirst();

				//Log.i("RefreshMarkers","First marker: "+markersInView.getString(1)+" "+markersInView.getString(2)+" "+markersInView.getInt(4)+"/"+markersInView.getInt(5));

				ArrayList<BusStopMarker> temp = new ArrayList<BusStopMarker>();
				while(true) {
					if(markersInView.isAfterLast()) break;
					BusStopMarker m = new BusStopMarker(new BusStop(markersInView.getString(2), 0,0, markersInView.getString(1)),markersInView.getInt(4), markersInView.getInt(5));

					temp.add(m);
					markersInView.moveToNext();
				}


				Log.i("RefreshMarkers","There are now "+markers.size()+" markers on map.");
				markers = temp;

				setLastFocusedIndex(-1); 
				populate();


			} catch (SQLiteException e) {
				Log.e("RefreshMarkers",e.getMessage());
			}


			lastDetailCalc = time;

			Log.i("RefreshMarkers","Time taken: "+(System.currentTimeMillis() - time));

		}
	}





	@Override
	protected BusStopMarker createItem(int i) {
		return markers.get(i);
	}

	/**
	 * Deal with a tap on screen - show the Bus Stop information dialog
	 * 
	 */
	@Override  
	protected boolean onTap(int i) { 

		// Fetch the marker the user selected
		BusStopMarker clickedMarker = markers.get(i);

		// Show dialog
		Intent in = new Intent(ourContext, BusStopActivity.class);
		in.putExtra(AppMain.BUSSTOP_INTENT_KEY,clickedMarker.busStop);
		ourContext.startActivity(in);
		
		boolean haptic = Preferences.getBool("haptics", true);
		if(haptic) {
			Vibrator vibrator = (Vibrator)ourContext.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(50);
		}


		return(true);  
	}  

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mv) {

		/*	if(mv.getZoomLevel() < maxMarkersZoomLevel) {
			markers.clear();
		} else {
			RefreshMarkers();
		}*/

		// Store map centre / span for the worker thread
		cacheMapData();

		return false;
	}

	@Override
	protected boolean hitTest(BusStopMarker item, Drawable marker, int hitX, int hitY) {

		Rect bounds = marker.getBounds();

		int width = bounds.width();
		int height = bounds.height();
		int centerX = bounds.centerX();
		int centerY = bounds.centerY();

		int touchWidth = Math.max(MIN_TOUCHABLE_WIDTH, width);
		int touchLeft = centerX - touchWidth / 2;
		int touchHeight = Math.max(MIN_TOUCHABLE_HEIGHT, height);
		int touchTop = centerY - touchHeight / 2;

		touchableBounds.set(touchLeft, touchTop, touchLeft + touchWidth,
				touchTop + touchHeight);

		return touchableBounds.contains(hitX, hitY); 
	}

	protected void updateMarkers(ArrayList<BusStopMarker> newMarkers) {

		markers = newMarkers;
		setLastFocusedIndex(-1); 
		populate();		

	}

	@Override
  protected void finalize() {
		conn.close();
	}

	@Override
	public int size() {
		return markers.size();
	}

	private void cacheMapData() {

		// Store map centre / span for the worker thread
		mapCentre = theMap.getMapCenter();
		mapLatSpan = theMap.getLatitudeSpan();
		mapLongSpan = theMap.getLongitudeSpan();
		mapZoomLevel = theMap.getZoomLevel();

	}

	public void startWorkerThread() {

		cacheMapData();		
		performUpdates = true;
		Thread t = new Thread(this);
		t.setName("MapMarkerWorker");
		t.start();
	}

	public void stopWorkerThread() {

		performUpdates = false;

	}

	/*
	 * For dealing with IPC between lookup thread and UI thread
	 */
	private Handler handler = new Handler() {

		//@Override
		@Override
    @SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {

			//Log.i("Message handler","Got message: "+msg.arg1);

			// Update markers
			if(msg.arg1 == 0) {

				updateMarkers((ArrayList<BusStopMarker>)msg.obj);

			}


		}

	};

	@Override
  public void run() {

		int oldZoomLevel = -1;
		int oldLat = -1;
		int oldLong = -1;
		int updateCount = 0;

		while(performUpdates) {

			cacheMapData();

			// Has either the zoom level or the maps centre changed since last call?
			if(!(oldZoomLevel == mapZoomLevel && oldLat == mapCentre.getLatitudeE6() && oldLong == mapCentre.getLongitudeE6())) {

				ArrayList<BusStopMarker> temp = new ArrayList<BusStopMarker>();

				//Log.i("RefreshMarkers","Running update");

				// Select an area twice the size of the screen to be the focus region
				int mcLong = mapCentre.getLongitudeE6();
				int mcLat = mapCentre.getLatitudeE6();
				int l = mcLong - (int)(mapLongSpan/2);
				int r = mcLong + (int)(mapLongSpan/2);
				int t = mcLat + (int)(mapLatSpan/2);
				int b = mcLat - (int)(mapLatSpan/2);

				String sql = String.format("SELECT *, abs(longE6%s%d) + abs(latE6%s%d) AS distance " +
						"FROM busstops WHERE longE6 > %d AND longE6 < %d AND latE6 > %d AND latE6 < %d " +
						"ORDER BY dataSet ASC, distance ASC LIMIT 100",
				    (mcLong<0 ? "+" : "-"),mcLong,(mcLat<0 ? "+" : "-"),mcLat,l,r,b,t);//sqlite doesn't like -- so we must translate to +

				try {

					if(mapZoomLevel > maxMarkersZoomLevel) {
						
						Cursor markersInView = conn.rawQuery(sql, null);
						markersInView.moveToFirst();

						while(true) {
							
							if(markersInView.isAfterLast()) break;
							BusStopMarker m = new BusStopMarker(new BusStop(markersInView.getString(2), 0,0, markersInView.getString(1)),markersInView.getInt(4), markersInView.getInt(5));
							temp.add(m);
							markersInView.moveToNext();
							
						}

					}

					//Log.i("RefreshMarkers","There are now "+markers.size()+" markers on map.");

					// Send new markers back to UI thread
					Message m = Message.obtain();
					m.arg1 = 0;
					m.obj = temp;
					handler.sendMessage(m);


				} catch (SQLiteException e) {
					Log.e("RefreshMarkers",e.getMessage());
				}

				//Log.i("RefreshMarkers","Time taken: "+(System.currentTimeMillis() - time));

				// Now store old values (but only after 2 updates)
				// TODO: god knows why we need to do this
				if(updateCount > 1) {
					oldZoomLevel = mapZoomLevel;
					oldLat = mapCentre.getLatitudeE6();
					oldLong = mapCentre.getLongitudeE6();
				} else {
					updateCount++;
				}

			}

			try {
				Thread.sleep(detailStep);
			} catch (InterruptedException e) {// if interupted just continue
			}

		}

	}
}