package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.io.Serializable;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataException;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataProvider;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class AppMain extends TabActivity {

	TabHost mTabHost;
	FrameLayout mFrameLayout;
	
	// Some constants for the menu items
	private final int MENU_DOWNLOAD = 1;
	private final int MENU_SETTINGS = 2;
	private final int MENU_ABOUT = 3;
	private final int MENU_MY_LOCATION = 4;
	
	// For receiving search intents
	protected final IntentFilter filter = new IntentFilter(Intent.ACTION_SEARCH);  


	/** Called when the activity is first created.*/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Open preferences file
		Preferences.openPrefs(this); // ready to use

		// Hide the title for more screen space
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Load layout with tabs
		setContentView(R.layout.main);		

		// Grab tab host + create some tabs.
		mTabHost = getTabHost();
		TabSpec tabSpec = mTabHost.newTabSpec("tab_map");
		tabSpec.setIndicator("Map",getResources().getDrawable(R.drawable.needle));

		// Load map activity into map tab
		Context ctx = this.getApplicationContext();
		Intent i = new Intent(ctx, MapViewActivity.class); 
		tabSpec.setContent(i);

		mTabHost.addTab(tabSpec);

		// Nearest
		TabSpec tabNearest = mTabHost.newTabSpec("tab_nearest");
		tabNearest.setIndicator("Nearby",getResources().getDrawable(R.drawable.person));
		
		// Load nearby listactivity into nearby tab
		Intent i2 = new Intent(ctx, NearbyStopActivity.class); 
		tabNearest.setContent(i2);
		mTabHost.addTab(tabNearest);

		// Look up
		TabSpec tabLookup = mTabHost.newTabSpec("tab_lookup");
		tabLookup.setIndicator("Look up",getResources().getDrawable(R.drawable.search));
		
		// Load lookup activity into map tab
		Intent i3 = new Intent(ctx, LookupStopActivity.class); 
		tabLookup.setContent(i3);		
		mTabHost.addTab(tabLookup);

		// Starred
		TabSpec tabStarred = mTabHost.newTabSpec("tab_starred");
		tabStarred.setIndicator("Starred",getResources().getDrawable(R.drawable.star2));
		
		// Load starred activity into starred tab
		Intent i4 = new Intent(ctx, StarredStopsActivity.class); 
		tabStarred.setContent(i4);
		mTabHost.addTab(tabStarred);
		
		// Set current tab
		mTabHost.setCurrentTab(0);
		
		// How many bus stops installed?
		DataStore db = new DataStore(this, false);
		int stopsInstalled = db.getBusStopCount();
		Log.i("AppMain","Bus stops installed: "+stopsInstalled);
		db.finalize();
		
		// Should we display help to the user?
		if(Preferences.getBool("show_map_help", true)) {
			new AlertDialog.Builder(this)
			.setTitle("Help")
			.setMessage("Click red flags on the map to view information for a specific bus stop.")
			.setPositiveButton("Close", null)					
			.show();
			
			Preferences.putBool("show_map_help", false);
		}
		
		// Perform initial check to see if the database is empty. If so, show message.
		if(stopsInstalled == 0) {
			
			new AlertDialog.Builder(this)
			.setTitle("Database empty")
			.setMessage("The phone's bus stop database is empty. Do you want to download the latest bus stop information now?")
			.setPositiveButton("Download now", new OnClickListener() {

				public void onClick(DialogInterface arg0, int arg1) {
					updateDatabase();					
				}
				
			})
			.setNeutralButton("Later", null)
			.show();
		}
		
		// Setup search UI
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

	}
	
	/* create the menu items */
	public void populateMenu(Menu menu) {
		
		if(getTabHost().getCurrentTabTag().equals("tab_map")) {
			menu.add(Menu.NONE, MENU_MY_LOCATION, Menu.NONE, "My Location").setIcon(android.R.drawable.ic_menu_mylocation);
		}
		
		menu.add(Menu.NONE, MENU_DOWNLOAD, Menu.NONE, "Update stops").setIcon(R.drawable.download);
		menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, SearchManager.MENU_KEY, Menu.NONE, "Search").setIcon(android.R.drawable.ic_search_category_default);
		menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, "About").setIcon(android.R.drawable.ic_menu_info_details);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		populateMenu(menu);
		return true;
		//return populateMenu(menu);

	}
	
	/** when menu button option selected */
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
			case MENU_DOWNLOAD: updateDatabase(); return true;
			case MENU_SETTINGS: showSettings(); return true;
			case SearchManager.MENU_KEY:  onSearchRequested(); return true;
			case MENU_ABOUT: startActivity(new Intent(this,AboutActivity.class)); return true;
			case MENU_MY_LOCATION:
				
				sendBroadcast(new Intent(MapViewActivity.MY_LOCATION_INTENT));
				return true;
				
			default: return false;
			}
		
	}
	
	private void updateDatabase() {
		
		// Ask the data store to update database
		DataStore ds = new DataStore(this, true);
		ds.updateDatabase(this);
		//ds.finalize();
	}
	
	private void showSettings() {
		
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}
	
	@Override
	/*
	 * Receives intents sent to us. If it's a search intent, try SMS lookup
	 * If that fails try address lookup
	 */
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Log.i("IntentHandler","Got a new intent: "+intent.getAction()+" / "+intent.getDataString());
		
		if(intent.getAction().equals(Intent.ACTION_SEARCH)) {
			
			
			// TODO: check local database rather than online for code validity
			try {
				
				doSMSLookup(intent.getStringExtra(SearchManager.QUERY));
				
			} catch(TransportDataException e) {
				
				Log.i("SearchIntentReceiver","Received a search: "+intent.getStringExtra(SearchManager.QUERY));
				getTabHost().setCurrentTabByTag("tab_lookup");
				
				// Pass the intent to the lookup tab to deal with
				// make an intent and parcel up data
				Intent i = new Intent(LookupStopActivity.LOOKUP_ADDR_INTENT);
				i.putExtra(SearchManager.QUERY, intent.getStringExtra(SearchManager.QUERY));
				sendBroadcast(i);
				
			}
			
		} else if(intent.getAction().equals(Intent.ACTION_VIEW)) {
			
			// We've been asked to show a bus stop's info screen.
			// TODO: this is kinda messy
			
			// get the stopref out of intent
		//	String stopRef = intent.getData().getLastPathSegment();
			
			Intent i = new Intent(this, BusStopActivity.class);
			i.setData(intent.getData());
			this.startActivity(i);
			
		}

	}

	private void doSMSLookup(String smsCode) throws TransportDataException {
		
		// Lookup online
		TransportDataProvider tdp = new TransportDataProvider(DataStore.apiKey, DataStore.feedURL);
		
		BusStop b = tdp.getStopBySMS(smsCode);
		
		Intent i = new Intent(this, BusStopActivity.class);		
		i.putExtra("stop",(Serializable)b);		
		this.startActivity(i);
		
		
	}
	
}

