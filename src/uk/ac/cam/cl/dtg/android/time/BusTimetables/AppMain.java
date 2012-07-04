package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import uk.ac.cam.cl.dtg.android.time.Constants;
import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataException;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataProvider;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.TabActivity;
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

	private static final String APP_MAIN = "AppMain";
	private static final String TAB_STARRED = "tab_starred";
	private static final String TAB_LOOKUP = "tab_lookup";
	private static final String TAB_NEAREST = "tab_nearest";
	private static final String TAB_MAP = "tab_map";
	TabHost mTabHost;
	FrameLayout mFrameLayout;

	// Some constants for the menu items
	private final int MENU_DOWNLOAD = 1;
	private final int MENU_SETTINGS = 2;
	private final int MENU_ABOUT = 3;
	private final int MENU_MY_LOCATION = 4;

	// For receiving search intents
	protected final IntentFilter filter = new IntentFilter(Intent.ACTION_SEARCH);

	public static final String ARRIVAL_INTENT_KEY = "arrival";
	public static final String BUSSTOP_INTENT_KEY = "stop";

	/** Called when the activity is first created. */
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

		// Map
		TabSpec tabSpec = mTabHost.newTabSpec(TAB_MAP);
		tabSpec.setIndicator(getText(R.string.tab_map), getResources()
				.getDrawable(R.drawable.needle));
		tabSpec.setContent(new Intent(this, MapViewActivity.class));
		mTabHost.addTab(tabSpec);

		// Near by stops
		TabSpec tabNearest = mTabHost.newTabSpec(TAB_NEAREST);
		tabNearest.setIndicator(getText(R.string.tab_nearby), getResources()
				.getDrawable(R.drawable.person));
		tabNearest.setContent(new Intent(this, NearbyStopActivity.class));
		mTabHost.addTab(tabNearest);

		// Look up
		TabSpec tabLookup = mTabHost.newTabSpec(TAB_LOOKUP);
		tabLookup.setIndicator(getText(R.string.tab_lookup), getResources()
				.getDrawable(R.drawable.search));
		tabLookup.setContent(new Intent(this, LookupStopActivity.class));
		mTabHost.addTab(tabLookup);

		// Starred stops
		TabSpec tabStarred = mTabHost.newTabSpec(TAB_STARRED);
		tabStarred.setIndicator(getText(R.string.tab_starred), getResources()
				.getDrawable(R.drawable.star2));
		tabStarred.setContent(new Intent(this, StarredStopsActivity.class));
		mTabHost.addTab(tabStarred);

		// Set current tab
		mTabHost.setCurrentTab(0);

		// How many bus stops installed?
		DataStore db = new DataStore(this, false);
		int stopsInstalled = db.getBusStopCount();
    db.close();
		Log.i(APP_MAIN, "Bus stops installed: " + stopsInstalled);

		// Should we display help to the user?
		if (Preferences.getBool(Preferences.SHOW_MAP_HELP, true)) {
			new AlertDialog.Builder(this)
					.setTitle(getText(R.string.getting_started_title))
					.setMessage(getText(R.string.getting_started_text))
					.setPositiveButton("Close", null).show();
			Preferences.putBool(Preferences.SHOW_MAP_HELP, false);
		}

		// Perform initial check to see if the database is empty. If so, show
		// message.
		if (stopsInstalled == 0) {
			new AlertDialog.Builder(this)
					.setTitle(getText(R.string.database_empty_title))
					.setMessage(getText(R.string.database_empty_text))
					.setPositiveButton(
							getText(R.string.database_empty_downloadnow),
							new OnClickListener() {
								@Override
                public void onClick(DialogInterface arg0,
										int arg1) {
									updateDatabase();
								}
							})
					.setNeutralButton(
							getText(R.string.database_empty_downloadlater),
							null).show();
		} else if (Preferences.shouldUpdate()){
		  new AlertDialog.Builder(this)
		  .setTitle(getText(R.string.database_update_title))
		  .setMessage(getText(R.string.database_update_text))
		  .setPositiveButton(
		      getText(R.string.database_update_downloadnow),
		      new OnClickListener() {
		        @Override
		        public void onClick(DialogInterface arg0,
		            int arg1) {
		          updateDatabase();
		        }
		      })
		      .setNeutralButton(
		          getText(R.string.database_update_downloadlater),
		          null).show();
		}

		// Setup search UI
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

	}

	/* create the menu items */
	public void populateMenu(Menu menu) {

		if (getTabHost().getCurrentTabTag().equals(TAB_MAP)) {
			menu.add(Menu.NONE, MENU_MY_LOCATION, Menu.NONE,
					getText(R.string.menu_my_location)).setIcon(
					android.R.drawable.ic_menu_mylocation);
		}

		menu.add(Menu.NONE, MENU_DOWNLOAD, Menu.NONE,
				getText(R.string.menu_update_stops)).setIcon(
				R.drawable.download);
		menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE,
				getText(R.string.menu_settings)).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, SearchManager.MENU_KEY, Menu.NONE,
				getText(R.string.menu_search)).setIcon(
				android.R.drawable.ic_search_category_default);
		menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, getText(R.string.menu_about))
				.setIcon(android.R.drawable.ic_menu_info_details);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		populateMenu(menu);
		return true;

	}

	/** when menu button option selected */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_DOWNLOAD:
			updateDatabase();
			return true;
		case MENU_SETTINGS:
			showSettings();
			return true;
		case SearchManager.MENU_KEY:
			onSearchRequested();
			return true;
		case MENU_ABOUT:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case MENU_MY_LOCATION:

			sendBroadcast(new Intent(MapViewActivity.MY_LOCATION_INTENT));
			return true;

		default:
			return false;
		}

	}

  private void updateDatabase() {
    DataStore ds = new DataStore(this, true);
    ds.updateDatabase(this, new Runnable() {

      @Override
      public void run() {
        sendBroadcast(new Intent(MapViewActivity.REFRESH_INTENT));
        Preferences.setUpdated();
      }
    });
  }

	private void showSettings() {
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}

	@Override
	/*
	 * Receives intents sent to us. If it's a search intent, try SMS lookup If
	 * that fails try address lookup
	 */
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Log.i(APP_MAIN, "Got a new intent: " + intent.getAction() + " / "
				+ intent.getDataString());

		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			// TODO: check local database rather than online for code validity
			try {
				doSMSLookup(intent.getStringExtra(SearchManager.QUERY));
			} catch (TransportDataException e) {
				Log.i(APP_MAIN,
						"Received a search: "
								+ intent.getStringExtra(SearchManager.QUERY));
				getTabHost().setCurrentTabByTag(TAB_LOOKUP);

				// Pass the intent to the lookup tab to deal with
				// make an intent and parcel up data
				Intent i = new Intent(LookupStopActivity.LOOKUP_ADDR_INTENT);
				i.putExtra(SearchManager.QUERY,
						intent.getStringExtra(SearchManager.QUERY));
				sendBroadcast(i);

			}

		} else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			// We've been asked to show a bus stop's info screen.
			Intent i = new Intent(this, BusStopActivity.class);
			i.setData(intent.getData());
			this.startActivity(i);

		}

	}

	private void doSMSLookup(String smsCode) throws TransportDataException {
		// Lookup online
		TransportDataProvider tdp = new TransportDataProvider(Constants.TRANSPORT_SERVER_APIKEY, Constants.TRANSPORT_SERVER_FEEDURL);
			
		BusStop b = tdp.getStopByNaptan(smsCode);

		Intent i = new Intent(this, BusStopActivity.class);
		i.putExtra(BUSSTOP_INTENT_KEY, b);
		this.startActivity(i);
	}

}
