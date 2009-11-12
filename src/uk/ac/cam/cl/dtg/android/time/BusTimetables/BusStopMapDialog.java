package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import uk.ac.cam.cl.dtg.android.time.buses.*;
import uk.ac.cam.cl.dtg.android.time.data.*;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class BusStopMapDialog extends Dialog implements Runnable {
	
			BusStop currentStop;
			BusArrivalData nextBuses;
			Activity context;
			ListView list;
			DataStore db;
	
     public BusStopMapDialog(Activity context, BusStop stop) {
	    	 
	          super(context);
	          this.context = context;
	          currentStop = stop;
	          
	          nextBuses = new BusArrivalData(); 
	          currentStop.setSmsCode(nextBuses.smsCode);
	          
	          Log.i("BSMD",currentStop.toString());
	          Log.i("BSMD",nextBuses.toString());
	         // nextBuses.NextBuses.add(new BusArrival("a","b","c"));
	
	          
	     }

	     @Override
	     public void onCreate(Bundle savedInstanceState) {
	          super.onCreate(savedInstanceState);
	          this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	          
	          getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
	        	        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	        	       // WindowManager.LayoutParams lp = getWindow().getAttributes();
	        	       // lp. = 0x60000820;
	        	       // getWindow().setAttributes(lp); 
	        	        
	        	        
	          setContentView(R.layout.busstopmapoverlay);

	         // setTitle(currentStop.getName());
	          
	          db = new DataStore(context, true);

	         	          
	          Button buttonOK = (Button) findViewById(R.id.buttonOK);
	          buttonOK.setOnClickListener(new OKListener());
	          
	          TextView textStopName = (TextView) findViewById(R.id.textStopName);
	          textStopName.setText(currentStop.getName());
	          
	          CheckBox favouriteCheckbox = (CheckBox) findViewById(R.id.FavouriteCheckbox);
	          favouriteCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
	          {
	        	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	        	    {
	                    Log.i("Fav checked!", "Should we save? "+isChecked);
	                    
	                    favouriteCheckChanged(isChecked);
	    	        	

	        	    }
	        	});
	          
	          // Are we already a favourite?
	          boolean isFav = db.isFavourite(currentStop);
	          favouriteCheckbox.setChecked(isFav);
	          
	          
	  		  try {
				nextBuses = LiveMapDataSource.getBusArrivalData(currentStop, 4);
			} catch (LiveMapException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
				Log.e("LiveMap",e.getMessage());
			}
			
	          
	          list = (ListView) findViewById(R.id.NextBuses);
	          
	          list.setAdapter(new BusArrivalAdapter(context, nextBuses));
	          
	          
	          // Set a listener so we can catch the dismiss
	          setOnDismissListener(new
	        		  OnDismissListener() {
										public void onDismiss(
												DialogInterface arg0) {
											Log.i("BusStopDialog","I've been dismissed!");
											
											// Close the database connection to prevent problems
											cleanUp();
											
										}
	        		                  }); 
	          
	           
 
	     }
	     
	     private void favouriteCheckChanged(boolean favourited) {
	    	 if(favourited) {
	    		 Log.i("BSMD",currentStop.toString());
	    		 db.setFavourite(currentStop);
	    	 } else {
	    		 db.clearFavourite(currentStop);
	    	 }
	    	 
				boolean haptic = Preferences.getBool("haptics", true);
				if(haptic) {
					Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
					vibrator.vibrate(50);
				}
	     }
	     
	     private void cleanUp() {
	    	 
	    	 db.finalize();
	    	 
	     }
	     
	     // Repopulate the data view
	     public void RefreshArrivals() {

	        // pd = ProgressDialog.show(context, "", "", true, false);
	        // Thread thread = new Thread(this);
	        // thread.start();
	     }

	     private class OKListener implements android.view.View.OnClickListener {

	          public void onClick(View v) {
	               BusStopMapDialog.this.dismiss();
	          }
	     }

	     
	     public void run() {
	    	 TransportDataProvider tdp = new TransportDataProvider(DataStore.apiKey, DataStore.feedURL);
	         try {
				nextBuses = LiveMapDataSource.getBusArrivalData(currentStop, 5);
			} catch (LiveMapException e) {
				e.printStackTrace();
			}
	         handler.sendEmptyMessage(0);
	     }
	     
		private Handler handler = new Handler() {
		 public void handleMessage(Message msg) {
		//pd.dismiss();
			 list.invalidate();
		
		}};

}
