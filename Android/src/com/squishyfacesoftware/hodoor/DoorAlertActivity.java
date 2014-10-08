package com.squishyfacesoftware.hodoor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.squishyfacesoftware.hodoor.DataAccess.HoDoorPreferencesManager;
import com.squishyfacesoftware.hodoor.Utilities.Typefaces;

public class DoorAlertActivity extends Activity
{
	NumberPicker mTimePicker;
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dooralertlayout);

		TextView alertTextView = (TextView)findViewById(R.id.alertTextView);
		alertTextView.setTypeface(Typefaces.get(this, "DiplomataSC-Regular"));

		mTimePicker = (NumberPicker)findViewById(R.id.dismissTimePicker);
		mTimePicker.setMinValue(15);
		mTimePicker.setMaxValue(90);
		mTimePicker.setValue(30);

		Intent intent = getIntent();
		int minutesOpen = intent.getIntExtra("minutesOpen", 0);
		String msg = "HoDoor! The door has been open for " + minutesOpen + " minutes!";
		alertTextView.setText(msg);

		Button dismissButton = (Button)findViewById(R.id.dismissButton);

		dismissButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Time now = new Time();
				now.setToNow();
				HoDoorPreferencesManager.SetNotificationsDismissedTime(DoorAlertActivity.this, now);
				HoDoorPreferencesManager.SetNotificationsDismissalDuration(DoorAlertActivity.this, mTimePicker.getValue());

				Intent mainIntent = new Intent(DoorAlertActivity.this, LoginActivity.class);
				DoorAlertActivity.this.startActivity(mainIntent);
			}
		});

	}
}