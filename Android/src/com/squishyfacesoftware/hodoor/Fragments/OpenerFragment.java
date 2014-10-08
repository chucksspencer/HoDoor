package com.squishyfacesoftware.hodoor.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squishyfacesoftware.hodoor.DataAccess.*;
import com.squishyfacesoftware.hodoor.DoorState;
import com.squishyfacesoftware.hodoor.R;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class OpenerFragment extends Fragment  implements EventUpdateSubscriber
{
	private HoDoorEventsManager _eventsManager;
	private DoorState _currentDoorState;
	JsonHttpResponseHandler _responseHandler;
	private Button _openButton;
	private final static String PING_URL = "/macros/ping";
	private final static String OPEN_DOOR_URL = "/macros/openRelay";

	public static OpenerFragment getInstance()
	{
		OpenerFragment instance = new OpenerFragment();
		return instance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.opener, container, false);
		_eventsManager = HoDoorEventsManager.getInstance(this.getActivity());
		_eventsManager.subscribe(this);
		_currentDoorState = _eventsManager.getLastKnownDoorState();
		if(_currentDoorState == DoorState.UNKNOWN){
			_eventsManager.refresh(getActivity());
		}


		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Button loginButton = (Button) getActivity().findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				EditText loginField = (EditText)getActivity().findViewById(R.id.loginField);
				EditText passwordField = (EditText)getActivity().findViewById(R.id.passwordField);
				HodoorIntranetWebServiceClient.getInstance(getActivity()).setAuthCookie(loginField.getText().toString(), passwordField.getText().toString());
				HodoorIntranetWebServiceClient.getInstance(getActivity()).authenticate();
				tryTouchingServer();
			}
		});

		_openButton = (Button)getActivity().findViewById(R.id.openButton);
		_openButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				openDoor();
			}
		});

		tryTouchingServer();
	}

	private void tryTouchingServer()
	{
		if(_responseHandler == null){
			_responseHandler = new JsonHttpResponseHandler()
			{
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject response)
				{
					super.onSuccess(statusCode, headers, response);
					showButtonForm();
				}


				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
				{
					super.onFailure(statusCode, headers, throwable, errorResponse);
					showLoginForm();
				}

			};
		}

		showProgressBar();
		HodoorIntranetWebServiceClient.getInstance(getActivity()).authenticate();
		HodoorIntranetWebServiceClient.getInstance(getActivity()).postNoParams(PING_URL, _responseHandler);
	}


	private void openDoor(){
		HodoorIntranetWebServiceClient.getInstance(getActivity()).postNoParams(OPEN_DOOR_URL + "/500", _responseHandler);

	}

	private void showProgressBar()
	{
		LinearLayout buttonForm = (LinearLayout) getActivity().findViewById(R.id.opener_button_form);
		LinearLayout progressView = (LinearLayout) getActivity().findViewById(R.id.openerHeaderProgress);
		LinearLayout loginForm = (LinearLayout)getActivity().findViewById(R.id.opener_login_form);
		progressView.setVisibility(View.VISIBLE);
		loginForm.setVisibility(View.GONE);
		buttonForm.setVisibility(View.GONE);
	}

	private void showLoginForm(){
		try
		{
			LinearLayout buttonForm = (LinearLayout) getActivity().findViewById(R.id.opener_button_form);
			LinearLayout progressView = (LinearLayout) getActivity().findViewById(R.id.openerHeaderProgress);
			LinearLayout loginForm = (LinearLayout)getActivity().findViewById(R.id.opener_login_form);
			progressView.setVisibility(View.GONE);
			loginForm.setVisibility(View.VISIBLE);
			buttonForm.setVisibility(View.GONE);
		}
		catch (Exception e){

		}
	}

	private void showButtonForm()
	{
		LinearLayout buttonForm = (LinearLayout) getActivity().findViewById(R.id.opener_button_form);
		LinearLayout progressView = (LinearLayout) getActivity().findViewById(R.id.openerHeaderProgress);
		LinearLayout loginForm = (LinearLayout)getActivity().findViewById(R.id.opener_login_form);
		progressView.setVisibility(View.GONE);
		loginForm.setVisibility(View.GONE);
		buttonForm.setVisibility(View.VISIBLE);

	}


	@Override
	public void onDestroyView()
	{
		_eventsManager.unsubscribe(this);
		super.onDestroy();
	}

	@Override
	public void eventsUpdated(List<DoorEventDTO> events)
	{
		_currentDoorState = _eventsManager.getLastKnownDoorState();
	}

	@Override
	public void eventsError(String error)
	{

	}
}