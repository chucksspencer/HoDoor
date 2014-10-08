package com.squishyfacesoftware.hodoor.Utilities;

import com.squishyfacesoftware.hodoor.DataAccess.DoorEventDTO;
import com.squishyfacesoftware.hodoor.DoorState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class HoDoorJSONUtils
{

	private static class EventDateComporator implements Comparator<DoorEventDTO>{
		@Override
		public int compare(DoorEventDTO event1, DoorEventDTO event2){

			if(event1.GetEventDate().before(event2.GetEventDate())) return 1;
			if(event1.GetEventDate().after(event2.GetEventDate())) return -1;
			return 0;
		}
	}

	public static ArrayList<DoorEventDTO> getDoorEventsFromJson(JSONArray jsonArray)
	{
		ArrayList<DoorEventDTO> events = new ArrayList<DoorEventDTO>();
		for (int j = 0; j < jsonArray.length(); j++) {
			try
			{
				JSONObject eventObject = jsonArray.getJSONObject(j);
				String dateString = eventObject.getString("date");
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddhh:mm:ss");
				Date eventDate = dateFormat.parse(dateString);
				Integer doorState = eventObject.getInt("state");
				Integer eventId = eventObject.getInt("eventId");
				events.add(new DoorEventDTO(eventDate, doorState, eventId));
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
			catch (ParseException e){

			}
		}
		Collections.sort(events, new EventDateComporator());
		return events;
	}

	public static Date getMostRecentEventOfType(DoorState state, ArrayList<DoorEventDTO> events){
		Collections.sort(events, new EventDateComporator());
		for(DoorEventDTO event : events)
		{
			if(event.GetDoorState() == state)
			{
				return event.GetEventDate();
			}
		}
		return null;
	}
}
