package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.util.List;

import uk.ac.cam.cl.dtg.android.time.Constants;
import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MapViewActivity extends MapActivity {

	public MapView mapView;
	List<Overlay> mapOverlays;
	Drawable busStopMarker;
	Drawable highlightMarker;
	MapBusStopOverlay itemizedOverlay;
	MapHighlightOverlay highlightOverlay;
	MyLocationOverlay locationOverlay;
	TextView statusText;
	boolean obtainedFix = false;

	// Declare intent filters and receivers for us to use
	public static final String MY_LOCATION_INTENT = "uk.ac.cam.cl.dtg.android.time.BusTimetables.MY_LOCATION_INTENT";
  public static final String REFRESH_INTENT = "uk.ac.cam.cl.dtg.android.time.BusTimetables.REFRESH_INTENT";
	protected final IntentFilter filter = new IntentFilter(
			MapHighlightOverlay.HIGHLIGHT_INTENT);
	private MapViewIntentReceiver receiver = new MapViewIntentReceiver();
	protected final IntentFilter locationfilter = new IntentFilter(MY_LOCATION_INTENT);
	protected final IntentFilter refreshFilter = new IntentFilter(REFRESH_INTENT);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.map);

		LinearLayout mainLayout = (LinearLayout) findViewById(R.id.MapLayout);
		System.out.println("API "+Constants.MAPS_APIKEY);
		mapView = new MapView(this, Constants.MAPS_APIKEY);
		LinearLayout.LayoutParams mapLayout = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		mainLayout.addView(mapView, mapLayout);
		mapView.setClickable(true);
		mapView.setHapticFeedbackEnabled(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setTraffic(true);

		// Instantiate fields for displaying overlays
		mapOverlays = mapView.getOverlays();
		busStopMarker = this.getResources().getDrawable(R.drawable.flag_red);
		itemizedOverlay = new MapBusStopOverlay(busStopMarker, this, mapView);

		// Instantiate an overlay for highlighting bus stops
		highlightMarker = this.getResources().getDrawable(R.drawable.circle2);
		highlightOverlay = new MapHighlightOverlay(highlightMarker);

		locationOverlay = new MyLocationOverlay(this, mapView);
		locationOverlay.runOnFirstFix(new Runnable() {
			@Override
      public void run() {
				animateToLocation();
			}
		});

		// Add overlays to map
		mapOverlays.add(itemizedOverlay);
		mapOverlays.add(highlightOverlay);
		mapOverlays.add(locationOverlay);

		// Bind the intent receiver to the filter
		registerReceiver(receiver, filter);
		registerReceiver(receiver, locationfilter);
		registerReceiver(receiver, refreshFilter);

		// TODO: make map zoom to 52.199499;0.128403 if there are no bus stops
		// in DB
		// How many bus stops installed?
		DataStore db = new DataStore(this, false);
		int stopsInstalled = db.getBusStopCount();
		db.close();

		// Perform initial check to see if the database is empty. If so, show
		// message.
		if (stopsInstalled == 0) {
			Log.i("MapBootup", "No stops. Animating to cambridge.");
			mapView.getController().animateTo(
					new GeoPoint((int) (52.199499 * 1E6),
							(int) (0.128403 * 1E6)));
			mapView.getController().setZoom(14);
		}

	}

	@Override
	protected void onPause() {

		super.onPause();

		// Disable compass if we're an on actual device
		if (isDevice())
			locationOverlay.disableCompass();

		locationOverlay.disableMyLocation();
		itemizedOverlay.stopWorkerThread();

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Enable the compass, only if we're on an actual device
		if (isDevice())
			locationOverlay.enableCompass();

		// Enable location services
		locationOverlay.enableMyLocation();

		// Start the marker overlay worker thread
		itemizedOverlay.startWorkerThread();

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	protected void highlightStop(BusStop stop) {

		// Do the highlight
		highlightOverlay.doHighlight((int) stop.getLatitude(),
				(int) stop.getLongitude(), mapView);

		// Switch to this tab
		TabActivity t = (TabActivity) getParent();
		t.getTabHost().setCurrentTab(0);

		mapView.invalidate();

	}

	class MapViewIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(MapHighlightOverlay.HIGHLIGHT_INTENT)) {

				Log.i("HighlightIntentReceiver", "Got highlight message!");
				BusStop stop = (BusStop) intent
						.getSerializableExtra(AppMain.BUSSTOP_INTENT_KEY);
				Log.i("HighlightIntentReceiver",
						"Stop is: " + stop.getLatitude());

				highlightStop(stop);

			} else if (intent.getAction().equals(MY_LOCATION_INTENT)) {

				animateToLocation();
			} else if (intent.getAction().equals(REFRESH_INTENT)){
			  itemizedOverlay.refreshMarkers();
			}

		}

	}

	public boolean isDevice() {
		return !"sdk".equals(Build.MODEL) && !"sdk".equals(Build.PRODUCT)
				&& !"generic".equals(Build.DEVICE);
	}

	public void animateToLocation() {
		// Log.i("animateToLoc","Obtained fix? "+obtainedFix);
		if (locationOverlay.getMyLocation() != null) {
			MapController mc = mapView.getController();
			mc.animateTo(locationOverlay.getMyLocation());
			mc.setZoom(17);
		} else {
			Toast.makeText(this, "Your current location is unavailable",
					Toast.LENGTH_SHORT).show();
		}
	}

}