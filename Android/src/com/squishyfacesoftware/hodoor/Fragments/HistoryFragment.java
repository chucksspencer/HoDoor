package com.squishyfacesoftware.hodoor.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.squishyfacesoftware.hodoor.DataAccess.DoorEventDTO;
import com.squishyfacesoftware.hodoor.DataAccess.EventUpdateSubscriber;
import com.squishyfacesoftware.hodoor.DataAccess.HoDoorEventsManager;
import com.squishyfacesoftware.hodoor.DataAccess.ImageUrlProvider;
import com.squishyfacesoftware.hodoor.DoorState;
import com.squishyfacesoftware.hodoor.EventListAdapter;
import com.squishyfacesoftware.hodoor.ImageViewerActivity;
import com.squishyfacesoftware.hodoor.R;
import com.squishyfacesoftware.hodoor.Utilities.Typefaces;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment implements EventUpdateSubscriber
{
	private List<DoorEventDTO> _events;
	private HoDoorEventsManager _eventsManager;
	private ListView _historyList;
	private EventListAdapter _listAdapter;
	ImageUrlProvider _imageUrlProvider;


	public static HistoryFragment getInstance()
	{
		return new HistoryFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.history, container, false);
		TextView title = (TextView)v.findViewById(R.id.historyTitle);
		title.setTypeface(Typefaces.get(getActivity(), "GermaniaOne-Regular"));

		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		_eventsManager = HoDoorEventsManager.getInstance(this.getActivity());
		_eventsManager.subscribe(this);
		_events = _eventsManager.getCurrentEvents();
		eventsUpdated(_events);

		_historyList = (ListView)getActivity().findViewById(R.id.historyList);
		_listAdapter = new EventListAdapter(getActivity(), _events);
		_historyList.setAdapter(_listAdapter);
		_historyList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				DoorEventDTO event = (DoorEventDTO)_historyList.getItemAtPosition(position);
				if(event.GetDoorState() == DoorState.OPEN){
					showEventImages(event.GetEventId().toString());
				}
			}
		});
	}

	private void showEventImages(String eventId){
		if(_imageUrlProvider == null){
			_imageUrlProvider = new ImageUrlProvider(){

				@Override
				public void onImagesUrlRetrieved(ArrayList<String> urls)
				{
					Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
					intent.putExtra("IMAGE_LOCATIONS", urls);
					startActivity(intent);
					hideProgressIndicator();
					super.onImagesUrlRetrieved(urls);
				}

				@Override
				public void onFailure(String error)
				{
					super.onFailure(error);
					Log.e("Hodor", error);
					hideProgressIndicator();
				}
			};
		}

		showProgressIndicator("Retrieving image locations");
		_imageUrlProvider.GetImagesForEvent(getActivity(), eventId);
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

	@Override
	public void eventsUpdated(List<DoorEventDTO> events)
	{
		try
		{
			hideProgressIndicator();
			_events = events;
			_listAdapter = new EventListAdapter(getActivity(), _events);
			_historyList.setAdapter(_listAdapter);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void eventsError(String error)
	{

	}

	private void showProgressIndicator(String statusText){
		LinearLayout progressView = (LinearLayout) getActivity().findViewById(R.id.historyProgress);
		progressView.setVisibility(View.VISIBLE);
		TextView statusTextView = (TextView)getActivity().findViewById(R.id.history_progress_text);
		statusTextView.setText(statusText);
	}

	private void hideProgressIndicator(){
		LinearLayout progressView = (LinearLayout) getActivity().findViewById(R.id.historyProgress);
		progressView.setVisibility(View.GONE);
	}
}