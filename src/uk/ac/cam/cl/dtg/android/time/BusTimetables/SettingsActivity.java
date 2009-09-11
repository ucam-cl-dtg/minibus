package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	     @Override  
	     protected void onCreate(Bundle savedInstanceState) {  
	    	 
	         super.onCreate(savedInstanceState);  	   
	         addPreferencesFromResource(R.xml.pref); 
	         
	     }  
	
}
