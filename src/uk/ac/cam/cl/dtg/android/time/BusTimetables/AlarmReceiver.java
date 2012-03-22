package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import uk.ac.cam.cl.dtg.android.time.buses.BusArrival;
import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.Log;

/**
 * Receives intents broadcast from alarms set by the user, and decides
 * what to do with them.
 * 
 * @author dt316
 *
 */

public class AlarmReceiver extends BroadcastReceiver {

	// Intent actions
	public static final String BUS_ARRIVAL_REMINDER = "uk.ac.cam.cl.dtg.android.time.BusTimetables.BUS_ARRIVAL_REMINDER";
	public static final String BUS_ARRIVAL_REMINDER_CLICKED = "uk.ac.cam.cl.dtg.android.time.BusTimetables.BUS_ARRIVAL_REMINDER_CLICKED";
	public static final String BUS_ARRIVAL_FOLLOWUP = "uk.ac.cam.cl.dtg.android.time.BusTimetables.BUS_ARRIVAL_FOLLOWUP";
	public static final int NOTIFICATION_ARRIVAL_REMINDER = 0;

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i("AlarmReceiver","======= Got intent: "+intent.getAction());

		Preferences.openPrefs(context);// So that we can use them later

		/*
		 * A bus arrival reminder is delivered
		 */
		if(intent.getAction().equals(BUS_ARRIVAL_REMINDER)) {

			Log.i("AlarmReceiver","We have a bus arrival reminder");

			// Get the Bus Arrival object out of intent
			BusArrival arrival = (BusArrival) intent
					.getSerializableExtra("arrival");
			BusStop stop = (BusStop) intent.getSerializableExtra(BusStop.INTENT_KEY);

			Log.d("AlarmReceiver","Received alarm for: "+arrival.getDestination());

			// Get reference to notification manager
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			// Create notification
			Notification notification = new Notification(R.drawable.bussmall, arrival.getDueTime().getArrivalTime() + " towards " + arrival.getDestination(), System.currentTimeMillis());
			Intent i = new Intent(AlarmReceiver.BUS_ARRIVAL_REMINDER_CLICKED);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
			notification.setLatestEventInfo(context, arrival.getServiceID()+" towards "+arrival.getDestination(), arrival.getDueTime()+ " " + stop.getName(), pendingIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.vibrate = new long[] { 10, 400, 200, 400, 200, 400, 1000, 400, 200, 400, 200, 400 };

			// Show notification
			manager.notify(NOTIFICATION_ARRIVAL_REMINDER, notification);

			// Play ringtone
			String tone = Preferences.getString("reminder_alarmtone","");
			Log.d("GetAlarm","Tone file: ["+tone+"]");
			MediaPlayer mp = new MediaPlayer();

			try {
				
				// Prepare the media player
				mp.setDataSource(context, Uri.parse(tone));
				mp.prepare();
				mp.setLooping(false);
				mp.setOnCompletionListener(new OnCompletionListener() {
					
					// So we tidily exit
					public void onCompletion(MediaPlayer mp) {
						mp.release();
					}
				});
				mp.start();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		/* Deal with a follow-up alarm
		 * (ask the user if the bus was on time)
		 */
		} else if(intent.getAction().equals(BUS_ARRIVAL_FOLLOWUP)) {

			Log.i("AlarmReceiver","We have a follow-up reminder");

			// Get the Bus Arrival object out of intent
			BusArrival arrival = (BusArrival) intent
					.getSerializableExtra("arrival");

			// Use this line to get the bus stop information out of the intent
			// BusStop stop = (BusStop) intent.getSerializableExtra(BusStop.INTENT_KEY);
			
			// Get reference to notification manager
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			// Create notification
			Notification notification = new Notification(R.drawable.bussmall, "Was the bus on time?", System.currentTimeMillis());
			Intent i = new Intent(AlarmReceiver.BUS_ARRIVAL_REMINDER_CLICKED);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
			notification.setLatestEventInfo(context, "Was the bus on time?", "Was the "+arrival.getDueTime().getArrivalTime()+" to "+arrival.getDestination()+" on time?", pendingIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.vibrate = new long[] { 10, 400, 200, 400, 200, 400, 1000, 400, 200, 400, 200, 400 };

			// Show notification
			manager.notify(NOTIFICATION_ARRIVAL_REMINDER, notification);


		} else if(intent.getAction().equals(BUS_ARRIVAL_REMINDER_CLICKED)) {



		}

	}

}
