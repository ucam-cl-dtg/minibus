package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.cl.dtg.android.time.buses.*;
import uk.ac.cam.cl.dtg.android.time.data.LiveMapDataSource;
import uk.ac.cam.cl.dtg.android.time.data.LiveMapException;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataException;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataProvider;

public class DataStore implements Runnable {

	SQLiteDatabase conn;
	DataStoreHelper dsh;
	private ProgressDialog pd;
	private int dataToDownload = 1;
	
	public static final String feedURL = "http://www.cl.cam.ac.uk/research/dtg/transport/omnibus/";
	public static final String apiKey = "[ELIDED]";

	/**
	 * Creates a new DataStore instance
	 * @param c Context the datastore is to be opened under
	 * @param wantWriteAccess Whether write access is required
	 */
	public DataStore(Context c, boolean wantWriteAccess) {

		dsh = new DataStoreHelper(c);

		if(wantWriteAccess) {
			conn = dsh.getWritableDatabase();
		} else {
			conn = dsh.getReadableDatabase();
		}

		Log.i("DataStore","Opening writeable connection... "+conn.getPath()+"/"+conn.getVersion());
	}



	/**
	 * Closes the connection to the underlying database
	 */
	public void finalize() {

		Log.i("DataStore","Closing connection...");
		conn.close();

	}

	/**
	 * Returns the number of bus stops held in the internal database
	 * @return Total number of bus stops
	 */
	public int getBusStopCount() {

		String sql = "SELECT count(*) FROM busstops";
		Cursor c = conn.rawQuery(sql, null);
		c.moveToFirst();
		int val = c.getInt(0);
		c.close();
		return val;
	}

	/**
	 * Marks a bus stop as starred in the database
	 * @param b BusStop to mark as starred. stopRef must be filled in.
	 */
	public void setFavourite(BusStop b) {

		// In case it's already there
		clearFavourite(b);

		String sql = "INSERT INTO favourites (stopRef) VALUES ("
			+ "\"" + b.getStopRef()
			+ "\")";

		Log.i("DataStore","Saving favourite: SQL is: "+sql);

		conn.execSQL(sql);
	}

	/*
	 * TODO: not saving sms code yet
	 * that is something for busstopdialog to worry about
	 */

	/**
	 * Marks a bus stop as not starred.
	 * @param stop Bus stop to be unmarked as starred. stopRef must be set.
	 */
	public void clearFavourite(BusStop b) {

		String sql = "DELETE FROM favourites WHERE stopRef='"+b.getStopRef()+"'";

		Log.i("DataStore","Clearing favourite: SQL is: "+sql);
		conn.execSQL(sql);
	}

	/**
	 * Returns whether a bus stop is starred or not.
	 * @param b Bus stop to return starred status for. stopRef must be set
	 * @return
	 */
	public boolean isFavourite(BusStop b) {

		String sql = "SELECT * FROM favourites WHERE stopRef='"+b.getStopRef()+"'";

		Cursor results = conn.rawQuery(sql, null);
		boolean ret = (results.getCount()==0) ? false : true;

		results.close();

		return ret;
	}

	/**
	 * Empties the internal database of bus stops.
	 */
	public void clearStops() {
		try {
			conn.execSQL("DELETE FROM busstops WHERE 1");
		} catch(SQLiteException e) {
			Log.e("DataStore",e.getMessage());
		}
	}

	/**
	 * Inserts every bus stop held in the given list of stops into the internal database.
	 * @param stops List of bus stops
	 * @param dataSet String representation of the data set the stops came from
	 */
	public void insertStops(List<BusStop> stops, String dataSet) {

		String sql;

		Log.i("UpdateDB","Attempting to insert "+stops.size()+" stops into DB");

		conn.beginTransaction();

		Log.i("UpdateDB","Transation begun");
		try {

			for(BusStop stop : stops) {
				sql = "INSERT INTO busstops (stopRef, stopName,stopSMS, latE6, longE6, dataSet) VALUES ("
					+ "\"" + stop.getStopRef() + "\", "
					+ "\"" + stop.getName() + "\", "
					+ "\"" + stop.getSmsCode() + "\", "
					+ (int)(stop.getLatitude() * 1E6) + ", "
					+ (int)(stop.getLongitude() * 1E6) + ", "
					+ "\"" + dataSet + "\""
					+" )";

				conn.execSQL(sql);	


				//Log.i("InsertStop",sql);
			}

			Log.i("UpdateDB","Finished inserts");

			conn.setTransactionSuccessful();

			Log.i("UpdateDB","Committed.");

		} catch(SQLiteException e) {
			Log.e("DataStore",e.getMessage());
		} finally {
			conn.endTransaction();
		}

		//Cursor c = conn.
	}

	/**
	 * Returns a list of the n closest bus stops to the given position.
	 * @param latE6 Latitude of given position, in degrees * 1E6
	 * @param longE6 Longitude of given position, in degrees * 1E6
	 * @param howMany How many bus stops to return
	 * @return
	 */
	public List<BusStop> findNearestStops(int latE6, int longE6, int howMany) {

		String sql = "SELECT busstops.stopName, busstops.stopRef, busstops.latE6, busstops.longE6, busstops._id, favourites._id as favid, "
			+ "((" + latE6 + "-latE6)*(" + latE6 + "-latE6) + (" + longE6 + "-longE6)*(" + longE6 + "-longE6)) AS dist "
			+ "FROM busstops LEFT OUTER JOIN favourites ON favourites.stopref = busstops.stopref ORDER BY dist ASC LIMIT " + howMany;

		Log.i("DataStore","Querying for nearby: "+sql);

		return queryForStops(sql);

	}

	/**
	 * Queries the database for stops using the SQL query given and returns results as a List<BusStop>
	 * @param sql SQL query
	 * @return
	 */
	private List<BusStop> queryForStops(String sql) {

		Log.i("QueryForStops","SQL is: "+sql);
		Cursor results = conn.rawQuery(sql, null);
		results.moveToFirst();

		ArrayList<BusStop> temp = new ArrayList<BusStop>();
		while(true) {
			if(results.isAfterLast()) break;
			//Log.i("queryForStops","STOP FROM DB: "+results.getString(0)+"/"+results.getString(1));
			BusStop s = new BusStop(results.getString(0), results.getInt(2),results.getInt(3), results.getString(1));
			s.setMeta("_ID", results.getInt(4));
			if(results.getInt(5) > 0) s.setMeta("FavID",results.getInt(5));
			temp.add(s);
			results.moveToNext();
		}
		
		results.close();

		return temp;

	}

	/**
	 * Returns list of with a name similar to the query given.
	 * @param query Name to match against
	 * @return
	 */
	public List<BusStop> findStopsLike(String query) {

		// Less than 2 characters? Return nothing as too expensive
		if(query.length() < 3)
		{
			return new ArrayList<BusStop>();
		}

		// Add wildcards before, after, and in spaces
		query = "%" + query + "%";
		query = query.replace(' ','%');

		// Construct SQL
		String sql = "SELECT busstops.stopName, busstops.stopRef, busstops.latE6, busstops.longE6, busstops._id, favourites._id as favid FROM busstops LEFT OUTER JOIN favourites ON favourites.stopref = busstops.stopref WHERE stopName LIKE \"" + query + "\" ORDER BY stopName";
		Log.i("DataStore","Querying DB with: "+sql);
		return queryForStops(sql);

	}


	/**
	 * Gets a bus stop by its stopRef
	 * @param ref stopRef of desired bus stop
	 * @return The bus stop with matching stopRef
	 * @throws DataStoreException
	 */
	public BusStop getStop(String ref) throws DataStoreException {

		String sql = "SELECT busstops.stopName, busstops.stopRef, busstops.latE6, busstops.longE6, busstops._id, favourites._id as favid  FROM busstops LEFT OUTER JOIN favourites ON favourites.stopref = busstops.stopref WHERE busstops.stopRef=\"" + ref + "\"";
		List<BusStop> r = queryForStops(sql);

		if(r.size() == 0) throw new DataStoreException("No such stop in the database.");

		return(r.get(0));

	}

	/**
	 * Gets a bus stop by internal _ID
	 * @param internalID internal _ID
	 * @return The desired bus stop
	 * @throws DataStoreException
	 */
	public BusStop getStopById(int internalID) throws DataStoreException {

		String sql = "SELECT busstops.stopName, busstops.stopRef, busstops.latE6, busstops.longE6, busstops._id, favourites._id as favid   FROM busstops LEFT OUTER JOIN favourites ON favourites.stopref = busstops.stopref WHERE busstops._id=" + internalID;
		List<BusStop> r = queryForStops(sql);

		if(r.size() == 0) throw new DataStoreException("No such stop in the database.");

		return(r.get(0));

	}


	/**
	 * 
	 * Clears the current version of the bus stop database held and re-downloads.
	 */
	public void updateDatabase(Context con) {

		// Clear database first
		clearStops();

		Log.i("UpdateDB","Beginning database update");
		pd = ProgressDialog.show(con, "Please wait...", "Downloading database. This may take a few minutes...", true, false);
		pd.setIcon(0);
		Thread thread = new Thread(this);
		thread.start();


	}

	public void run() {

		// Load in new bus stops
		List<BusStop> downloadedStops;
		
		// Create new TransportDataProvider
		TransportDataProvider tdp = new TransportDataProvider(apiKey, feedURL);

		Log.i("UpdateDB","Hello from thread. Trying to DL set "+dataToDownload);

		try {

			// insert the markers into the database

			for(int i = 1; i < 4; i++) {
				
				Message m;
				m = Message.obtain();
				m.arg1 = 1;
				m.obj = "Downloading and unpacking definitions ("+i+"/3)...";
				handler.sendMessage(m);

				downloadedStops = tdp.getBusStops(i);

				m = Message.obtain();
				m.arg1 = 1;
				m.obj = "Installing definitions ("+downloadedStops.size()+" new)...";
				handler.sendMessage(m);

				Log.i("UpdateDB","Got "+downloadedStops.size()+" definitions");

				insertStops(downloadedStops, Integer.toString(i));

			}


		} catch (TransportDataException e) {

			Log.e("AppMain","Error downloading stop db: "+e.getMessage());
		}

		handler.sendEmptyMessage(0);

	}



	private Handler handler = new Handler() {

		//@Override
		public void handleMessage(Message msg) {

			if(msg.arg1 == 0) pd.dismiss();

			if(msg.arg1 == 1) {
				pd.setMessage((String)msg.obj);
			}

		}

	};
}