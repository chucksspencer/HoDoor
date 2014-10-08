package com.squishyfacesoftware.hodoor.DataAccess;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;

import java.util.Date;

public class HoDoorPreferencesManager
{
	private static HoDoorPreferencesManager _instance;
	private static final String PREF_KEY = "com.squishyfacesoftware.hodoor.prefs";
	private static final String NOTIFICATIONS_DISMISSED_KEY = "notifications_dismissed";
	private static final String NOTIFICATIONS_ENABLED_KEY = "notifications_enabled";
	private static final String NOTIFICATIONS_LAST_DISMISSED = "notifications_dismissed_time";
	private static final String NOTIFICATIONS_DISMISS_DURATION = "notifications_dismiss_duration";
	private boolean _notificationsEnabled;

	public static Boolean GetNotificationsEnabled(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		return preferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
	}

	public static void SetNotificationsEnabled(Context context, Boolean value)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(NOTIFICATIONS_ENABLED_KEY, value);
		editor.commit();
	}

	public static Time GetNotificationsDismissedTime(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		long timeInMilliseconds = preferences.getLong(NOTIFICATIONS_LAST_DISMISSED, 0);
		Time dismissedTime = new Time();
		dismissedTime.set(timeInMilliseconds);
		return dismissedTime;
	}

	public static void SetNotificationsDismissedTime(Context context, Time value)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(NOTIFICATIONS_LAST_DISMISSED, value.toMillis(true));
		editor.commit();
	}

	public static int GetNotificationsDismissalDuration(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		int dismissalDuration = preferences.getInt(NOTIFICATIONS_DISMISS_DURATION, 0);
		return dismissalDuration;
	}

	public static void SetNotificationsDismissalDuration(Context context, int value)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(NOTIFICATIONS_DISMISS_DURATION, value);
		editor.commit();
	}
}
