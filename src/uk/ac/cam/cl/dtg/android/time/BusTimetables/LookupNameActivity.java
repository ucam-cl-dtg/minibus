package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.util.List;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class LookupNameActivity extends ListActivity {

	DataStore db;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Preferences.openPrefs(this);

		setContentView(R.layout.namelookupdialog);
		getListView().setClickable(true);

		EditText editName = (EditText) findViewById(R.id.editNameInput);
		editName.addTextChangedListener(new TextWatcher() {
			@Override
      public void afterTextChanged(Editable s) {
				// Log.i("TextChanged","after changed");
			}
			@Override
      public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				// Log.i("TextChanged","before changed");
			}
			@Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
				searchTextChanged(s.toString());
			}

		}); 


		db = new DataStore(this, false);
	}

	private void searchTextChanged(String q) {


		List<BusStop> results = db.findStopsLike(q);
		Log.i("Lookup","Got "+results.size()+" matches");

		TextView info = (TextView) findViewById(R.id.textLookupInfo);

		int numResults = results.size();
		switch(numResults) {
		case 0: info.setText("No matches found."); break;
		case 1: info.setText("1 match found."); break;
		default: info.setText(results.size() + " matches found."); break;
		}

		getListView().setAdapter(new StopSearchResultAdapter(this, results));

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
