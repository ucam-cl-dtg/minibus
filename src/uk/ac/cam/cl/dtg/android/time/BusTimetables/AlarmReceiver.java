package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import uk.ac.cam.cl.dtg.android.time.buses.BusArrival;
import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

	private static final String ALARM_RECEIVER = "AlarmReceiver";
	
	// Intent actions
	public static final String BUS_ARRIVAL_REMINDER = "uk.ac.cam.cl.dtg.android.time.BusTimetables.BUS_ARRIVAL_REMINDER";
	public static final String BUS_ARRIVAL_REMINDER_CLICKED = "uk.ac.cam.cl.dtg.android.time.BusTimetables.BUS_ARRIVAL_REMINDER_CLICKED";
	public static final String BUS_ARRIVAL_FOLLOWUP = "uk.ac.cam.cl.dtg.android.time.BusTimetables.BUS_ARRIVAL_FOLLOWUP";
	public static final int NOTIFICATION_ARRIVAL_REMINDER = 0;

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i(ALARM_RECEIVER,"======= Got intent: "+intent.getAction());

		Preferences.openPrefs(context);// So that we can use them later

		/*
		 * A bus arrival reminder is delivered
		 */
		if(intent.getAction().equals(BUS_ARRIVAL_REMINDER)) {

			Log.i(ALARM_RECEIVER,"We have a bus arrival reminder");

			// Get the Bus Arrival object out of intent
			BusArrival arrival = (BusArrival) intent
					.getSerializableExtra(AppMain.ARRIVAL_INTENT_KEY);
			BusStop stop = (BusStop) intent.getSerializableExtra(AppMain.BUSSTOP_INTENT_KEY);

			Log.d(ALARM_RECEIVER,"Received alarm for: "+arrival.getDestination());

			// Get reference to notification manager
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			// Create notification
			
			Resources res = context.getResources();
			String notificationText = String.format(res.getString(R.string.arrival_reminder),arrival.getDueTime(),arrival.getDestination());
			Notification notification = new Notification(R.drawable.bussmall, notificationText, System.currentTimeMillis());
			Intent i = new Intent(AlarmReceiver.BUS_ARRIVAL_REMINDER_CLICKED);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
			
			String notificationTitle = String.format(res.getString(R.string.arrival_reminder_title),arrival.getServiceID(), arrival.getDestination());
			String notificationDesc = String.format(res.getString(R.string.arrival_reminder_desc),arrival.getDueTime(), stop.getName());
			notification.setLatestEventInfo(context, notificationTitle, notificationDesc, pendingIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.vibrate = new long[] { 10, 400, 200, 400, 200, 400, 1000, 400, 200, 400, 200, 400 };

			// Show notification
			manager.notify(NOTIFICATION_ARRIVAL_REMINDER, notification);

			// Play ringtone
			String tone = Preferences.getString(Preferences.REMINDER_ALARMTONE,"");
			
			Log.d(ALARM_RECEIVER,"Tone file: ["+tone+"]");
			
			MediaPlayer mp = new MediaPlayer();

			try {
				
				// Prepare the media player
				mp.setDataSource(context, Uri.parse(tone));
				mp.prepare();
				mp.setLooping(false);
				mp.setOnCompletionListener(new OnCompletionListener() {
					
					// So we tidily exit
					@Override
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

			Log.i(ALARM_RECEIVER,"We have a follow-up reminder");

			// Get the Bus Arrival object out of intent
			BusArrival arrival = (BusArrival) intent
					.getSerializableExtra(AppMain.ARRIVAL_INTENT_KEY);

			// Use this line to get the bus stop information out of the intent
			// BusStop stop = (BusStop) intent.getSerializableExtra(BusStop.INTENT_KEY);
			
			// Get reference to notification manager
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			// Create notification
			Resources res = context.getResources();
			
			
			Notification notification = new Notification(R.drawable.bussmall, res.getString(R.string.question_bus_ontime), System.currentTimeMillis());
			Intent i = new Intent(AlarmReceiver.BUS_ARRIVAL_REMINDER_CLICKED);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
			String notificationText = String.format(res.getString(R.string.question_bus_ontime_detailed),arrival.getDueTime().getArrivalTime(),arrival.getDestination());
			notification.setLatestEventInfo(context, res.getString(R.string.question_bus_ontime), notificationText, pendingIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.vibrate = new long[] { 10, 400, 200, 400, 200, 400, 1000, 400, 200, 400, 200, 400 };

			// Show notification
			manager.notify(NOTIFICATION_ARRIVAL_REMINDER, notification);


		} else if(intent.getAction().equals(BUS_ARRIVAL_REMINDER_CLICKED)) {
			// TODO: this should handle what happens when the user is asked "Was the bus on time?"


		}

	}

}
