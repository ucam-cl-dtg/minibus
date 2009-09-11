package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataStoreHelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "busstopapp.db";
	private final static int DATABASE_VERSION = 2;
	private final static String FAVOURITES_TABLE_NAME = "favourites";
	private final static String STOPS_TABLE_NAME = "busstops";

	public DataStoreHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	
    	// Create table for storing stops
        db.execSQL("CREATE TABLE " + STOPS_TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "stopRef TEXT, stopName TEXT, stopSMS TEXT, latE6 INTEGER, longE6 INTEGER, dataSet TEXT"
                + ");");
    	
    	// Create table for favourites
        db.execSQL("CREATE TABLE " + FAVOURITES_TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "stopRef TEXT"
                + ");");
        
        db.execSQL("CREATE INDEX lat_index ON busstops (latE6);");
        db.execSQL("CREATE INDEX long_index ON busstops (longE6);");
        db.execSQL("CREATE INDEX sset_index ON busstops (dataSet);");
        
        Log.i("DataStoreHelper","Created new database! :) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DataStoreHelper", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS favourites");
        onCreate(db);
    }
}