package com.squishyfacesoftware.hodoor.DataAccess;
import java.util.List;

public interface EventUpdateSubscriber {
	public void eventsUpdated(List<DoorEventDTO> events);
	public void eventsError(String error);
}
