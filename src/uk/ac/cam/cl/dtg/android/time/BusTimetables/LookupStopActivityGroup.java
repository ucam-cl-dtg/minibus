package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LookupStopActivityGroup extends ActivityGroup {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);

	      // Some code

	      View view = getLocalActivityManager()
	                                .startActivity("LookupMain", new
	Intent(this,LookupStopActivity.class)
	                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
	                                .getDecorView();
	      
	      
	       this.setContentView(view);

	} 
	
}
