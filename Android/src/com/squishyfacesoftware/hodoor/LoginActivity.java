package com.squishyfacesoftware.hodoor;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.squishyfacesoftware.hodoor.DataAccess.*;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements EventUpdateSubscriber
{

	private HoDoorEventsManager _eventsManager;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.loginlayout);

		_eventsManager = HoDoorEventsManager.getInstance(getApplicationContext());

		Button loginButton = (Button)findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
			String login = ((EditText)findViewById(R.id.loginField)).getText().toString();
			String password = ((EditText)findViewById(R.id.passwordField)).getText().toString();
			if(login.length() > 0 && password.length() > 0)
			{
				HodoorWebServiceClient.getInstance(LoginActivity.this).setAuthCookie(login, password);
				HodoorWebServiceClient.getInstance(LoginActivity.this).authenticate();
				showProgressBar();
				_eventsManager.refresh(LoginActivity.this);
			}
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		showProgressBar();
		HodoorWebServiceClient.getInstance(this).authenticate();
		_eventsManager.subscribe(this);
		_eventsManager.refresh(this);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	private void tryGettingEvents(){
		DoorEventProvider doorEventProvider = new DoorEventProvider(){
			@Override
			public void onPostDoorEventsRetrieval(ArrayList<DoorEventDTO> doorData)
			{
				// Got events, so we're authenticated. Move on to main activity.
				hideProgressBar();
				Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
				LoginActivity.this.startActivity(mainIntent);
			}

			@Override
			public void onFailure(String failureMessage, int errorCode)
			{
				// Couldn't get events - hide progress bar and show login form
				hideProgressBar();
			}
		};
		HodoorWebServiceClient.getInstance(this).authenticate();
		doorEventProvider.retrieveDoorEvents(this, 1);
	}

	private void showProgressBar()
	{
		LinearLayout progressView = (LinearLayout) findViewById(R.id.loginHeaderProgress);
		LinearLayout mLoginForm = (LinearLayout)findViewById(R.id.login_form);
		progressView.setVisibility(View.VISIBLE);
		mLoginForm.setVisibility(View.GONE);
	}

	private void hideProgressBar()
	{
		LinearLayout progressView = (LinearLayout) findViewById(R.id.loginHeaderProgress);
		LinearLayout mLoginForm = (LinearLayout)findViewById(R.id.login_form);
		progressView.setVisibility(View.GONE);
		mLoginForm.setVisibility(View.VISIBLE);
	}

	@Override
	public void eventsUpdated(List<DoorEventDTO> events)
	{
		_eventsManager.unsubscribe(this);
		hideProgressBar();
		Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
		LoginActivity.this.startActivity(mainIntent);

	}

	@Override
	public void eventsError(String error)
	{
		hideProgressBar();
	}
}