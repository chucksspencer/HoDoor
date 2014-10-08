package com.squishyfacesoftware.hodoor.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squishyfacesoftware.hodoor.DataAccess.DoorEventDTO;
import com.squishyfacesoftware.hodoor.DataAccess.EventUpdateSubscriber;
import com.squishyfacesoftware.hodoor.DataAccess.HoDoorEventsManager;
import com.squishyfacesoftware.hodoor.DataAccess.ImageUrlProvider;
import com.squishyfacesoftware.hodoor.DoorState;
import com.squishyfacesoftware.hodoor.ImageViewerActivity;
import com.squishyfacesoftware.hodoor.R;
import com.squishyfacesoftware.hodoor.Utilities.Typefaces;

import java.util.ArrayList;
import java.util.List;

public class StatusFragment extends Fragment implements EventUpdateSubscriber
{
	private List<DoorEventDTO> _events;
	TextView _statusText;
	private HoDoorEventsManager _eventsManager;
	ImageUrlProvider _imageUrlProvider;

	public static StatusFragment getInstance()
	{
		return new StatusFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.status, container, false);

		_statusText = (TextView)v.findViewById(R.id.doorStatusLabel);
		_statusText.setTypeface(Typefaces.get(getActivity(), "DiplomataSC-Regular"));

		final ImageButton viewNowButton = (ImageButton) v.findViewById(R.id.viewNowButton);
		viewNowButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ShowCurrentPicture();
			}
		});

		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		_eventsManager = HoDoorEventsManager.getInstance(this.getActivity());
		_eventsManager.subscribe(this);
		_events = _eventsManager.getCurrentEvents();
		updateStatusDisplay();
	}

	private void ShowCurrentPicture()
	{
		if(_imageUrlProvider == null){
			_imageUrlProvider = new ImageUrlProvider(){
				@Override
				public void onImageUrlRetrieved(String url)
				{
					super.onImageUrlRetrieved(url);
					hideProgressIndicator();
					Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
					ArrayList<String> urls = new ArrayList<String>();
					urls.add(url);
					intent.putExtra("IMAGE_LOCATIONS", urls);
					startActivity(intent);
				}

				@Override
				public void onFailure(String error)
				{
					super.onFailure(error);
					hideProgressIndicator();
				}
			};
		}

		showProgressIndicator("Getting current image");
		_imageUrlProvider.GetCurrentImage(getActivity());
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

	private void updateStatusDisplay(){
		if(_events.size() > 0){
			DoorState state = _events.get(0).GetDoorState();
			if(state == DoorState.CLOSED)
			{
				_statusText.setText("The door\r\nis\r\nclosed");
			}
			else if(state == DoorState.OPEN)
			{
				_statusText.setText("The door\r\nis\r\nopen");
			}
			else
			{
				_statusText.setText("The door\r\nis\r\nunknown");
			}
		}
	}

	@Override
	public void eventsUpdated(List<DoorEventDTO> events)
	{
		try
		{
			hideProgressIndicator();
			_events = events;
			updateStatusDisplay();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void eventsError(String error)
	{
		_statusText.setText("The door\r\nis\r\nunknown");
	}

	private void showProgressIndicator(String statusText){
		LinearLayout progressView = (LinearLayout) getActivity().findViewById(R.id.statusProgress);
		progressView.setVisibility(View.VISIBLE);
		TextView statusTextView = (TextView)getActivity().findViewById(R.id.status_progress_text);
		statusTextView.setText(statusText);
	}

	private void hideProgressIndicator(){
		LinearLayout progressView = (LinearLayout) getActivity().findViewById(R.id.statusProgress);
		progressView.setVisibility(View.GONE);
	}
}