package com.squishyfacesoftware.hodoor.DataAccess;

import android.content.Context;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squishyfacesoftware.hodoor.Utilities.HoDoorJSONUtils;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DoorEventProvider
{
	protected JsonHttpResponseHandler mResponseHandler;
	private final static String GET_DOOR_RECORDS_URL = "/macros/getRecords";

	public DoorEventProvider()
	{
		mResponseHandler = new JsonHttpResponseHandler()
		{

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response)
			{
				super.onSuccess(statusCode, headers, response);
				ArrayList<DoorEventDTO> events = HoDoorJSONUtils.getDoorEventsFromJson(response);
				onPostDoorEventsRetrieval(events);
			}


			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response)
			{
				super.onSuccess(statusCode, headers, response);
				try
				{
					JSONArray jsonEvents = response.getJSONArray("events");
					ArrayList<DoorEventDTO> events = HoDoorJSONUtils.getDoorEventsFromJson(jsonEvents);
					onPostDoorEventsRetrieval(events);
				} catch (JSONException e)
				{
					e.printStackTrace();
					DoorEventProvider.this.onFailure(e.getMessage(), statusCode);
				}
			}


			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse)
			{
				super.onFailure(statusCode, headers, e, errorResponse);
				DoorEventProvider.this.onFailure(e.getMessage(), statusCode);
			}


			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse)
			{
				super.onFailure(statusCode, headers, e, errorResponse);
				DoorEventProvider.this.onFailure(e.getMessage(), statusCode);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable e)
			{
				super.onFailure(statusCode, headers, responseBody, e);
				DoorEventProvider.this.onFailure(e.getMessage(), statusCode);
			}

		};
	}

	public void retrieveDoorEvents(Context context, int numEvents)
	{
		HodoorWebServiceClient.getInstance(context).postNoParams(GET_DOOR_RECORDS_URL + "/" + numEvents, mResponseHandler);
	}

	public void onPostDoorEventsRetrieval(ArrayList<DoorEventDTO> doorData){}

	public void onFailure(String failureMessage, int statusCode){}

}
