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
	
	static void putBool(String key, boolean Value) {
		
		Editor e = prefs.edit();
		e.putBoolean(key, Value);
		e.commit();
		
	}
}
