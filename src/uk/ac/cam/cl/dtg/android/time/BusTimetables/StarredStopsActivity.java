package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.io.Serializable;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class StarredStopsActivity extends ListActivity {

	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		Preferences.openPrefs(this);


	} 	


	protected void onListItemClick(ListView l, View v, int position, long id){

		super.onListItemClick(l, v, position, id);

		// Get the item that was clicked
		Log.i("Starred","Item clicked: "+id);

		DataStore db = new DataStore(this, false);

		try {

			BusStop stop = db.getStopById((int)id);


			Log.i("Nearby","Clicked nearby stop: "+stop.getStopRef()+" "+stop.getName());

			Intent i = new Intent(this, BusStopActivity.class);
			i.putExtra("stop",(Serializable)stop);
			this.startActivity(i);

			boolean haptic = Preferences.getBool("haptics", true);
			if(haptic) {
				Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(50);
			}

		} catch (DataStoreException e) {

			e.printStackTrace();

		}



	} 

	protected void onResume() {

		super.onResume();

		// Get content resolver
		Cursor starred = managedQuery(BusStopContentProvider.CONTENT_URI, null, null, null, null);

		Log.i("CP","Got "+starred.getCount()+" results.");


		ListAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.starredstoprow,
				starred, // Give the cursor to the list adapter
				new String[] {"stopName"}, // Map the stopName column in the
				// stops database to...
				new int[] {R.id.col_stop_name}); // The col_stop_name view defined in
		// the XML template
		setListAdapter(adapter);

	}


}
