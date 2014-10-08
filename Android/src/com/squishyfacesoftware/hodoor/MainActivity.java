package com.squishyfacesoftware.hodoor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squishyfacesoftware.hodoor.DataAccess.DoorEventDTO;
import com.squishyfacesoftware.hodoor.DataAccess.EventUpdateSubscriber;
import com.squishyfacesoftware.hodoor.DataAccess.HoDoorEventsManager;
import com.squishyfacesoftware.hodoor.DataAccess.HodoorWebServiceClient;
import com.squishyfacesoftware.hodoor.Fragments.HoDoorPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MainActivity extends FragmentActivity implements EventUpdateSubscriber
{
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	static final String TAG = "HODOOR";
	GoogleCloudMessaging gcm;
	String regid;
	private HoDoorEventsManager _eventsManager;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "817493094391";

	HoDoorPagerAdapter mAdapter;
	ViewPager mPager;
	PageIndicator mIndicator;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mAdapter = new HoDoorPagerAdapter(getSupportFragmentManager());

		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);

		mPager.setCurrentItem(1);

		final Button refreshButton = (Button) findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showProgressIndicator();
				_eventsManager.refresh(MainActivity.this);
			}
		});
	}

	// You need to do the Play Services APK check here too.
	@Override
	protected void onResume() {
		super.onResume();
		HodoorWebServiceClient.getInstance(this).authenticate();
		// Check device for Play Services APK.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(this);

			if (regid.isEmpty()) {
				registerInBackground();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}
		_eventsManager = HoDoorEventsManager.getInstance(this);
		_eventsManager.subscribe(this);
		_eventsManager.refresh(MainActivity.this);

	}

	@Override
	public void onPause()
	{
		try
		{
			_eventsManager.unsubscribe(this);
		}
		catch(Exception e)
		{
			Log.e("Hodoor", e.getMessage());
		}
		super.onPause();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i("HODOOR", "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			String packageName = context.getPackageName();
			PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend();

					// Persist the regID - no need to register again.
					storeRegistrationId(MainActivity.this, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {

			}
		}.execute(null, null, null);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
	 * messages to your app. Not needed for this demo since the device sends upstream messages
	 * to a server that echoes back the message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		// Your implementation here.
		TextHttpResponseHandler responseHandler = new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response)
			{
				super.onSuccess(statusCode, headers, response);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response)
			{
				super.onSuccess(statusCode, headers, response);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
			{
				super.onFailure(statusCode, headers, throwable, errorResponse);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
			{
				super.onFailure(statusCode, headers, throwable, errorResponse);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable)
			{
				super.onFailure(statusCode, headers, responseString, throwable);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString)
			{
				super.onSuccess(statusCode, headers, responseString);
			}
		};

		HodoorWebServiceClient.getInstance(this).postNoParamsSync("/macros/registerDevice/" + regid, responseHandler);
	}

	@Override
	public void eventsUpdated(List<DoorEventDTO> events)
	{
		hideProgressIndicator();
	}

	@Override
	public void eventsError(String error)
	{
		if(error == "Unauthorized")
		{
			Intent mainIntent = new Intent(this, LoginActivity.class);
			this.startActivity(mainIntent);
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Error");
			builder.setMessage("There was an error retrieving data from the server: " + error);
			builder.setPositiveButton("Ok", null);
			builder.show();
		}
	}

	private void showProgressIndicator(){
		ProgressBar progressView = (ProgressBar) findViewById(R.id.progressBar);
		progressView.setVisibility(View.VISIBLE);
		Button refreshButton = (Button)findViewById(R.id.refreshButton);
		refreshButton.setVisibility(View.GONE);
	}

	private void hideProgressIndicator(){
		ProgressBar progressView = (ProgressBar) findViewById(R.id.progressBar);
		progressView.setVisibility(View.GONE);
		Button refreshButton = (Button)findViewById(R.id.refreshButton);
		refreshButton.setVisibility(View.VISIBLE);
	}


}
