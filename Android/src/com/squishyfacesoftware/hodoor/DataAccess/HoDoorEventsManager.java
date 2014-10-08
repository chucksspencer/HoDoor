package com.squishyfacesoftware.hodoor.DataAccess;

import android.content.Context;
import android.util.Log;
import com.squishyfacesoftware.hodoor.DoorState;

import java.util.ArrayList;
import java.util.List;

public class HoDoorEventsManager
{
	List<EventUpdateSubscriber> subscribers;
	private static HoDoorEventsManager _instance;
	private DoorEventProvider _provider;
	private List<DoorEventDTO> _events;
	private Boolean _retrieving;

	private HoDoorEventsManager(Context context)
	{
		subscribers = new ArrayList<EventUpdateSubscriber>();
		_events = new ArrayList<DoorEventDTO>();
		_retrieving = false;
		initializeProvider(context);
	}

	public static HoDoorEventsManager getInstance(Context context){
		if(_instance == null){
			_instance = new HoDoorEventsManager(context);
		}
		return _instance;
	}

	public DoorState getLastKnownDoorState(){
		if(_events.isEmpty()) return DoorState.UNKNOWN;
		return _events.get(_events.size() -1).GetDoorState();
	}

	public void subscribe(EventUpdateSubscriber subscriber){
		if(!subscribers.contains(subscriber)){ subscribers.add(subscriber); }
	}

	public void unsubscribe(EventUpdateSubscriber subscriber){
		if(subscribers.contains(subscriber)) subscribers.remove(subscriber);
	}

	public List<DoorEventDTO> getCurrentEvents(){
		return _events;
	}

	public void refresh(Context context){
		_provider.retrieveDoorEvents(context, 10);
	}

	private void initializeProvider(Context context){
		_provider = new DoorEventProvider(){
			@Override
			public void retrieveDoorEvents(Context context, int numEvents)
			{
				if(!_retrieving){
					_retrieving = true;
					super.retrieveDoorEvents(context, numEvents);
				}
			}

			@Override
			public void onPostDoorEventsRetrieval(ArrayList<DoorEventDTO> doorData)
			{
				_retrieving = false;
				super.onPostDoorEventsRetrieval(doorData);
				_events = doorData;
				sendEventsUpdateNotification(doorData);
			}


			@Override
			public void onFailure(String failureMessage, int errorCode)
			{
				_retrieving = false;
				super.onFailure(failureMessage, errorCode);
				Log.e("HoDoor", failureMessage);
				sendErrorNotification(failureMessage);
			}
		};
	}

	private void sendErrorNotification(String error)
	{
		for(EventUpdateSubscriber subscriber : subscribers){
			subscriber.eventsError(error);
		}

	}

	private void sendEventsUpdateNotification(List<DoorEventDTO> events){
		ArrayList<EventUpdateSubscriber> listCopy = new ArrayList<EventUpdateSubscriber>(subscribers);
		for(EventUpdateSubscriber subscriber : listCopy){
			subscriber.eventsUpdated(events);
		}
	}
}
