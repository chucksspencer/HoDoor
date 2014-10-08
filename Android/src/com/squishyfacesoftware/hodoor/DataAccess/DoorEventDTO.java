package com.squishyfacesoftware.hodoor.DataAccess;
import com.squishyfacesoftware.hodoor.DoorState;

import java.util.Date;

public class DoorEventDTO
{
	private Date _eventDate;
	private DoorState _doorState;
	private int _eventId;

	public DoorEventDTO(Date eventDate, int doorState, int eventId)
	{
		this._eventDate = eventDate;
		this._eventId = eventId;
		if(doorState == 0){
			_doorState = DoorState.CLOSED;
		}
		else {
			_doorState = DoorState.OPEN;
		}
	}

	public Date GetEventDate(){
		return _eventDate;
	}

	public DoorState GetDoorState(){
		return _doorState;
	}

	public Integer GetEventId()	{
		return _eventId;
	}
}
