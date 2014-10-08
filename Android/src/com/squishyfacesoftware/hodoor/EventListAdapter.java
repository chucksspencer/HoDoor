package com.squishyfacesoftware.hodoor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.squishyfacesoftware.hodoor.DataAccess.DoorEventDTO;
import com.squishyfacesoftware.hodoor.Utilities.Typefaces;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventListAdapter extends ArrayAdapter<DoorEventDTO>
{
	private final Context context;
	private final List<DoorEventDTO> values;

	public EventListAdapter(Context context, List<DoorEventDTO> values) {
		super(context, R.layout.rowlayout, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.rowText);
		DoorEventDTO event = values.get(position);
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
		SimpleDateFormat dayFormat = new SimpleDateFormat("MM/dd");
		Date eventDate = event.GetEventDate();

		if(event.GetDoorState() == DoorState.OPEN){
			textView.setText("The door was OPENED at " + timeFormat.format(eventDate) + " on " + dayFormat.format(eventDate));
		}
		else {
			textView.setText("The door was CLOSED at " + timeFormat.format(eventDate) + " on " + dayFormat.format(eventDate));
		}

		return rowView;
	}
}
