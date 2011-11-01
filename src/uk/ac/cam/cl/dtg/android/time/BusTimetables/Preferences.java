package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Preferences {

	private static SharedPreferences prefs;
	
	static void openPrefs(Context context) {
		
		if(prefs != null) return;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	
	}
	
	static String getString(String key, String defValue) {
		
		return prefs.getString(key, defValue);
	}
	
	static boolean getBool(String key, boolean defValue) {
		
		return prefs.getBoolean(key, defValue);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return whether this succeeded
	 */
	static boolean putBool(String key, boolean value) {
		
		Editor e = prefs.edit();
		e.putBoolean(key, value);
		return e.commit();
		
	}
}
