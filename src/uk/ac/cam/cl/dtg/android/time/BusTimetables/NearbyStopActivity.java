package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.util.LinkedList;
import java.util.List;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class NearbyStopActivity extends ListActivity implements LocationListener {

	private LocationManager locationManager;

	String[] mStrings;
	ListView lv;
	ListAdapter adapt;
	List<BusStop> nearbyStops = new LinkedList<BusStop>();
	boolean displayedMessage = false;

	private static final int SHOW_ON_MAP = 0;
	private static final int SHOW_ARRIVALS_DEPS = 1;

	@Override
  protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {

		super.onPause();

		// Don't want any more GPS updates
		locationManager.removeUpdates(this);

	}

	@Override
	protected void onResume() {

		super.onResume();

		/*
		 * LOCATION STUFF
		 * 
		 * Get the best available provider and set ourselves for updates.
		 * 
		 */

		// Get a location manager
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 

		// Choose a provider
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);		
		String provider = locationManager.getBestProvider(crit, true);
		Log.i("Nearby","Chosen provider is: "+provider);

		// Do we have a provider?
		if(provider==null) {

			// We have no providers. Show error dialog.
			setContentView(R.layout.nearbynosignal);

		} else {
			
			// There is a provider, so go ahead with location stuff.
			
			// Layout
			setContentView(R.layout.nearbystopactivity);

			// Register ourselves for updates		
			locationManager.requestLocationUpdates(provider, 1000, 20, this);

			// Try initial location get
			try {
			  newLocation(LocationHelper.getLastKnownLocation(locationManager));
			} catch(Exception e) {
				Log.e("Location","error on lastknown "+ e.getMessage());
			}

			this.getListView().setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Bus stop actions");
					menu.add(0, SHOW_ON_MAP, 0, "Show on map");
					menu.add(0, SHOW_ARRIVALS_DEPS, 1, "Show arrivals / departures");
				}
			}); 
			
			// Set icon in title bar
			this.setTitle("Using location data from "+provider);

		}
	}

  @Override
  public boolean onContextItemSelected(MenuItem item) {

		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo(); 

		switch (item.getItemId()) {
		case SHOW_ON_MAP:

			// To get the id of the clicked item in the list use menuInfo.id
			Log.d("ContextMenu", "Show on map");			

			// Get the item that was clicked
			Object o = this.getListAdapter().getItem(menuInfo.position);
			BusStop stop = (BusStop) o;

			// make an intent and parcel up data
			Intent i = new Intent(MapHighlightOverlay.HIGHLIGHT_INTENT);
			i.putExtra(AppMain.BUSSTOP_INTENT_KEY, stop);
			sendBroadcast(i);

			break;

		case SHOW_ARRIVALS_DEPS:

			Log.d("ContextMenu", "Show arrivals / departures");
			showStopInfo(menuInfo.position);

		default:

			return super.onContextItemSelected(item);

		}
		return true;
	} 

	/**
	 * Called when the location changes and list needs refreshing
	 * @param loc
	 */
	private void newLocation(Location loc) {

		Log.i("Location",loc.getLatitude()+"/"+loc.getLongitude());
		//dialog("Location update",loc.getLatitude()+"/"+loc.getLongitude());

		// Ask the database for new
		DataStore ds = new DataStore(this, false);
		nearbyStops = ds.findNearestStops((int)(loc.getLatitude() * 1E6), (int)(loc.getLongitude() * 1E6), 10);
		ds.close();

		if(nearbyStops.size() > 0) {
			this.setListAdapter(new BusStopAdapter(this, nearbyStops, loc)); 
		} else {

			// something indicating none nearby
			if(!displayedMessage) {
				new AlertDialog.Builder(this)
				.setTitle("Database empty")
				.setMessage("The phone's bus stop database is empty. Once you download a data set, your closest bus stops will appear here.")
				.show();
				displayedMessage = true;
			}

    }
    String provider = loc.getProvider();
    if (provider != null) {
      setLocationProviderStatus(provider);
    }
  }

  private void setLocationProviderStatus(String provider) {
    TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
    txtStatus.setText("Location provided by: " + provider);
  }

	@Override
  public void onLocationChanged(Location loc) {
		newLocation(loc);

	}

	@Override
  public void onProviderDisabled(String provider) {
		Log.d("Location","Provider disabled: "+provider);

	}

	@Override
  public void onProviderEnabled(String provider) {
		Log.d("Location","Provider enabled: "+provider);

	}

	@Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("Location","Provider status change: "+provider+" to "+status);
		
		TextView txtStatus = (TextView)findViewById(R.id.txtStatus);
		ImageView imgStatus = (ImageView)findViewById(R.id.imgStatus);
		
		if(status == LocationProvider.AVAILABLE) {
		  setLocationProviderStatus(provider);
			imgStatus.setBackgroundResource(R.drawable.transmit_green);
			
		} else if(status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			
			txtStatus.setText("Waiting for location from "+provider+"...");
			imgStatus.setBackgroundResource(R.drawable.transmit_yellow);
			
		} else if(status == LocationProvider.OUT_OF_SERVICE) {
			
			txtStatus.setText("No location data unavailable");
			imgStatus.setBackgroundResource(R.drawable.transmit_error);
		}

	}

	private void showStopInfo(int position) {

		// Get the item that was clicked
		Object o = this.getListAdapter().getItem(position);
		BusStop stop = (BusStop) o;

		Log.i("Nearby","Clicked nearby stop: "+stop.getAtcoCode()+" "+stop.getName());

		Intent i = new Intent(this, BusStopActivity.class);
		i.putExtra(AppMain.BUSSTOP_INTENT_KEY,stop);
		this.startActivity(i);

		boolean haptic = Preferences.getBool("haptics", true);
		if(haptic) {
			Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(50);
		}

	} 

	@Override
  protected void onListItemClick(ListView l, View v, int position, long id){
		super.onListItemClick(l, v, position, id);
		showStopInfo(position);
	}


}
