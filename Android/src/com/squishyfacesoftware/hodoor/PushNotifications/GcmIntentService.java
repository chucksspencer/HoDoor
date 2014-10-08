package com.squishyfacesoftware.hodoor.PushNotifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squishyfacesoftware.hodoor.DataAccess.DoorEventDTO;
import com.squishyfacesoftware.hodoor.DataAccess.HoDoorPreferencesManager;
import com.squishyfacesoftware.hodoor.DoorAlertActivity;
import com.squishyfacesoftware.hodoor.DoorState;
import com.squishyfacesoftware.hodoor.R;
import com.squishyfacesoftware.hodoor.Utilities.HoDoorJSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
	}
	public static final String TAG = "GCM Demo";

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				Log.i(TAG, "Send Error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				Log.i(TAG, "Deleted messages on server: " + extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				try
				{
					String eventString = intent.getStringExtra("events");
					JSONObject eventJSONObject = new JSONObject(eventString);
					JSONArray eventJSONArray = eventJSONObject.getJSONArray("events");
					ArrayList<DoorEventDTO> events = HoDoorJSONUtils.getDoorEventsFromJson(eventJSONArray);
					processEvents(events);
				} catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// See if the notifications have been dismissed by the user, and if that dismissal is still valid
	private Boolean HasCurrentDismissal()
	{
		long dismissTime = HoDoorPreferencesManager.GetNotificationsDismissedTime(this).toMillis(true);
		if(dismissTime == 0) return false;

		Time now = new Time();
		now.setToNow();

		int dismissalDuration = HoDoorPreferencesManager.GetNotificationsDismissalDuration(this) - 1;
		long dismissalDurationinMillisecs = dismissalDuration*1000*60;
		long timeSinceDismissal = now.toMillis(true) - dismissTime;
		return (timeSinceDismissal < dismissalDurationinMillisecs );
	}

	private void processEvents(ArrayList<DoorEventDTO> events)
	{
		DoorState currentState = events.get(0).GetDoorState();

		if(currentState == DoorState.OPEN && HoDoorPreferencesManager.GetNotificationsEnabled(this) && !HasCurrentDismissal())
		{
			Date now = new Date();
			Date lastOpened = HoDoorJSONUtils.getMostRecentEventOfType(DoorState.OPEN, events);
			long millisecsDifference = now.getTime() - lastOpened.getTime();
			int minsOpen = (int) Math.floor(millisecsDifference/1000/60);
			if(minsOpen > 15)
			{
				sendNotification(minsOpen);
			}
		}

		// Reset notification dismissal if door is closed
		if(currentState == DoorState.CLOSED){
			HoDoorPreferencesManager.SetNotificationsDismissedTime(this, new Time());
			cancelNotifications();
		}

	}

	private void cancelNotifications(){
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(int minutesOpen)
	{
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent alertIntent = new Intent(this, DoorAlertActivity.class);
		alertIntent.putExtra("minutesOpen", minutesOpen);
		alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),
				alertIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		String msg = "HoDoor! The door has been open for " + minutesOpen + " minutes!";
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle("HoDoor")
						.setContentIntent(contentIntent)
						.setSound(alarmSound)
						.setVibrate(new long[] { 100, 200, 100, 200, 100, 200, 100, 200})
						.setAutoCancel(true)
						.setStyle(new NotificationCompat.BigTextStyle()
								.bigText(msg))
						.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}