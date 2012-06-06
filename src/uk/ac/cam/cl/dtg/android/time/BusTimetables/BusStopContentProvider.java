package uk.ac.cam.cl.dtg.android.time.BusTimetables;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.LiveFolders;
import android.util.Log;


public class BusStopContentProvider extends ContentProvider {

	// Connection to our database
	private SQLiteDatabase db;
	
	// Define URI constants
	public static final String AUTHORITY = "uk.ac.cam.cl.dtg.android.time.BusStopApp";
	public static final String PATH_SINGLE_STARRED = "starred/*";
	public static final String PATH_SINGLE = "stop/*";
	public static final String PATH_MULTIPLE = "starred";
	public static final String PATH_LIVE_FOLDER_STARRED = "live_folders/starred";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"+ PATH_MULTIPLE);
	public static final int MULTIPLE_STOP = 0;
	public static final int SINGLE_STOP = 1;
	public static final int LIVE_FOLDER_STARRED = 2;
	public static final int SEARCH_SUGGESTIONS = 3;
	
	// Define MIME type stuff
	public static final String MIME_DIR_PREFIX = "vnd.android.cursor.dir";
	public static final String MIME_ITEM_PREFIX = "vnd.android.cursor.item";
	public static final String MIME_ITEM = "uk.ac.cam.cl.dtg.android.time.buses.BusStop";
	public static final String MIME_TYPE_SINGLE = MIME_ITEM_PREFIX + "/" + MIME_ITEM;
	public static final String MIME_TYPE_MULTIPLE = MIME_DIR_PREFIX + "/" + MIME_ITEM;
	
	// URI matcher and projection information
	private static UriMatcher URI_MATCHER = null;
	private static HashMap<String, String> PROJECTION_MAP;	
	private static final HashMap<String, String> LIVE_FOLDER_PROJECTION_MAP;
	
	static {
			
		// Set up the URI matcher
		BusStopContentProvider.URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		BusStopContentProvider.URI_MATCHER.addURI(AUTHORITY, PATH_MULTIPLE, MULTIPLE_STOP);
		BusStopContentProvider.URI_MATCHER.addURI(AUTHORITY, PATH_SINGLE, SINGLE_STOP);
		BusStopContentProvider.URI_MATCHER.addURI(AUTHORITY, PATH_LIVE_FOLDER_STARRED, LIVE_FOLDER_STARRED);
		BusStopContentProvider.URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY+"/*", SEARCH_SUGGESTIONS);
		
		// Set up hash map
		PROJECTION_MAP = new HashMap<String, String>();
		PROJECTION_MAP.put(BaseColumns._ID,"busstops._id");
		PROJECTION_MAP.put("stopName", "stopName");
		PROJECTION_MAP.put("stopRef", "busstops.stopRef");
		PROJECTION_MAP.put("latE6", "latE6");
		PROJECTION_MAP.put("longE6", "longE6");
		
		// Projection map for using when live folder
	    LIVE_FOLDER_PROJECTION_MAP = new HashMap<String, String>();
	    LIVE_FOLDER_PROJECTION_MAP.put(LiveFolders._ID,"busstops._id");
	    LIVE_FOLDER_PROJECTION_MAP.put(LiveFolders.NAME, "stopName AS "+LiveFolders.NAME);
	    LIVE_FOLDER_PROJECTION_MAP.put("latE6", "latE6");
	    LIVE_FOLDER_PROJECTION_MAP.put("longE6", "longE6");
	    LIVE_FOLDER_PROJECTION_MAP.put("stopRef", "busstops.stopRef");

	}
	
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch(URI_MATCHER.match(uri)) {
		
		case SINGLE_STOP:
			
			return MIME_TYPE_SINGLE;
			
		case MULTIPLE_STOP:
			
			return MIME_TYPE_MULTIPLE;
			
		case SEARCH_SUGGESTIONS:
			
			return MIME_TYPE_MULTIPLE;
			
		default:
			
			throw new IllegalArgumentException("Unknown URI: "+uri);
		}
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		return null;
	}

	@Override
	public boolean onCreate() {
		
		// Try to open database
		db = (new DataStoreHelper(getContext()).getWritableDatabase());
		
		return (db == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		// New query builder
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		// Debug
		Log.d("BSCP","The URI is: "+uri);
		
		
		// Are we constructing a live folder?
		boolean isLiveFolder = false;
		
		switch(URI_MATCHER.match(uri)) {
		case SINGLE_STOP:
			qb.setTables("busstops");
			qb.setProjectionMap(PROJECTION_MAP);
			break;
			
		case MULTIPLE_STOP:
			
			qb.setTables("favourites LEFT OUTER JOIN busstops ON busstops.stopRef = favourites.stopRef");
			qb.setProjectionMap(PROJECTION_MAP);
			break;
			
		case LIVE_FOLDER_STARRED:
			
			qb.setTables("favourites LEFT OUTER JOIN busstops ON busstops.stopRef = favourites.stopRef");
			qb.setProjectionMap(LIVE_FOLDER_PROJECTION_MAP);
			isLiveFolder = true;
			break;
			
		case SEARCH_SUGGESTIONS:
			
			return getSuggestions(uri.getLastPathSegment());
			
		default:
			
			throw new IllegalArgumentException("URI "+uri+" not supported yet or invalid.");
		
		}
		
		Cursor c = qb.query(this.db, projection, selection, selectionArgs, null, null, "stopName ASC");
		
		if(isLiveFolder) {			
			String[] newcols = new String[]{LiveFolders._ID, LiveFolders.NAME,LiveFolders.INTENT};
			MatrixCursor mc = new MatrixCursor(newcols);
			
			int stopNameCol = c.getColumnIndexOrThrow(LiveFolders.NAME);
			int stopLatCol = c.getColumnIndexOrThrow("latE6");
			int stopLongCol = c.getColumnIndexOrThrow("longE6");
			int stopRefCol = c.getColumnIndexOrThrow("stopRef");
			
			c.moveToFirst();
			
			while(true) {
				if(c.isAfterLast()) break;						
				
				BusStop stop = new BusStop(c.getString(stopNameCol), c.getDouble(stopLatCol), c.getDouble(stopLongCol), c.getString(stopRefCol));
				
				Log.i("CP","Starred stop: "+stop);
				
				Intent i = new Intent(this.getContext(),BusStopActivity.class);
				i.setAction("uk.ac.cam.cl.dtg.android.time.STOPINFO");
				i.putExtra("stop",(Serializable)stop);
				
				Object[] row = new Object[]{ c.getString(c.getColumnIndex(LiveFolders._ID)), stop.getName(), "content://uk.ac.cam.cl.dtg.android.time.BusStopApp/" + stop.getAtcoCode()};
						
				mc.addRow(row);
				c.moveToNext();
			}			
			return mc;
		}
		
		c.setNotificationUri(this.getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
	
	private Cursor getSuggestions(String q) {
		
		// Construct cursor
		String[] cols = new String[]{
				BaseColumns._ID,
				SearchManager.SUGGEST_COLUMN_TEXT_1,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
				SearchManager.SUGGEST_COLUMN_ICON_1,
				SearchManager.SUGGEST_COLUMN_ICON_2
				};
		
		MatrixCursor mc = new MatrixCursor(cols);		
		
		// If query is not long enough, return the empty cursor
		// avoids getting hundreds of results
		if(q.length() < 3) return mc;
		
		// Otherwise, query datastore for similar bus stops
		DataStore ds = new DataStore(getContext(), false);
		List<BusStop> suggestions = ds.findStopsLike(q);
		ds.close();
		
		// Add to cursor
		for(BusStop suggestion : suggestions) {
			
			Object[] row = new Object[5];
			row[0] = suggestion.getMeta("_ID");
			row[1] = suggestion.getName();
			row[2] = suggestion.getAtcoCode();
			
			Log.i("Suggestions","FavID meta is: "+suggestion.getMeta("FavId"));
			Object favid = (Object)suggestion.getMeta("FavID");
			
			row[3] = (favid==null) ? R.drawable.flag_red : R.drawable.star2;
			row[4] = null;
			
			mc.addRow(row);
			
		}
		
		return mc;
	}

}
