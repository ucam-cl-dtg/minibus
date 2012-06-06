package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LookupAddressActivity extends ListActivity {

	EditText editAddressQuery;
	List<BusStop> nearbyStops;
	List<Address> foundAddresses;
	TextView textInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Preferences.openPrefs(this);

		setContentView(R.layout.addresslookupdialog);

		editAddressQuery = (EditText) findViewById(R.id.editAddrInput);
		textInfo = (TextView) findViewById(R.id.textLookupInfo);

		findViewById(R.id.btnSearch).setOnClickListener(new OnClickListener() {

			@Override
      public void onClick(View arg0) {				
				searchForAddress();				
			} });
		
		editAddressQuery.setKeyListener(new KeyListener() {

			@Override
      public void clearMetaKeyState(View arg0, Editable arg1, int arg2) {

			}

			@Override
      public int getInputType() {
				return 1;
			}

			@Override
      public boolean onKeyDown(View view, Editable text, int keyCode,
					KeyEvent event) {
				
				Log.d("KeyDown","Key code: "+event.getDisplayLabel());
				
				if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					
					searchForAddress();
					return true;
					
				}
				
				Log.d("KeyDown","Was not enter. Returning false.");
				return false;
			}

			@Override
      public boolean onKeyOther(View view, Editable text, KeyEvent event) {
				return false;
			}

			@Override
      public boolean onKeyUp(View view, Editable text, int keyCode,
					KeyEvent event) {
				return false;
			}
			
		});

	}


	/*
	 * Bounding box:
	 * 
	 * L -3.559570
	 * R 1.889648
	 * T 53.218752
	 * B 50.450043
	 * 
	 */
	private void searchForAddress() {

		Geocoder gc = new Geocoder(this, Locale.UK);
		String addressInput = editAddressQuery.getText().toString();

		try {
			foundAddresses = gc.getFromLocationName(addressInput, 25, 50.450043, -3.559570, 53.218752, 1.889648); //Search addresses
			Log.i("Geocode","Found addresses: "+foundAddresses);

			// Find no matches?
			if(foundAddresses.size() == 0) {
				Toast.makeText(this, "No matches for that address.", Toast.LENGTH_LONG).show();
				return;
			}

			// Iterate through Addresses, and make a new list to display to user
			List<String> displayAddr = new LinkedList<String>();
			for(Address a : foundAddresses) {
				StringBuilder s = new StringBuilder();
				s.append(a.getAddressLine(0));
				
				Log.i("AddressFormat", a.getAddressLine(1)+" "+a.getAdminArea());
				
				if(a.getAddressLine(1)!=null) s.append(", "+a.getAddressLine(1));
				if(a.getAdminArea()!=null) s.append(", "+a.getAdminArea());
				
				displayAddr.add(s.toString());		    	  
			}

			new AlertDialog.Builder(LookupAddressActivity.this)
			.setTitle("Find stops near...")
			.setItems(displayAddr.toArray(new String[]{ }),
					new DialogInterface.OnClickListener() {
				@Override
        public void onClick(DialogInterface dialog, int which) {
					selectAddress(which);
				}

			})

			.show();
		}
		catch (Exception e) {
			//@todo: Show error message
		} 

	}

	private void selectAddress(int i) {

		Address selectedAddr = foundAddresses.get(i);

		Log.i("Location",selectedAddr.getLatitude()+"/"+selectedAddr.getLongitude());

		Location loc = new Location("gps");
		loc.setLatitude(selectedAddr.getLatitude());
		loc.setLongitude(selectedAddr.getLongitude());


		// Ask the database for new
		DataStore db = new DataStore(this, false);
		nearbyStops = db.findNearestStops((int)(loc.getLatitude() * 1E6), (int)(loc.getLongitude() * 1E6), 10);
		db.close();


		textInfo.setText("Stops close to "+selectedAddr.getAddressLine(0));

		if(nearbyStops.size() > 0){
			this.setListAdapter(new BusStopAdapter(this, nearbyStops, loc)); 
		}
	}

	@Override
  protected void onListItemClick(ListView l, View v, int position, long id){

		super.onListItemClick(l, v, position, id);

		// Get the item that was clicked
		Object o = l.getItemAtPosition(position);
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
}
