// Copyright (C) 2011-2013 ITOMIG GmbH
//
//   This file is part of iTopMobile.
//
//   iTopMobile is free software; you can redistribute it and/or modify	
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.
//
//   iTopMobile is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with iTopMobile. If not, see <http://www.gnu.org/licenses/>

package de.itomig.itoplib;

import de.itomig.itoplib.cmdb.InternalTask;
import de.itomig.itoplib.cmdb.Person;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import static de.itomig.itoplib.ItopConfig.*;

public class TaskDetailActivity extends Activity {
	private TextView tvTitle, tvDesc, tvStatus, tvRemark;
	private TextView tvCaller;
	private ImageView priorityIcon, callCaller;
	private InternalTask t;
	private Button btn;

	private String callerPhone;

	NotificationManager notificationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (debug)
			Log.i(TAG, "TaskDetailActivity - onCreate");

		setContentView(R.layout.task_details);

		btn = (Button) findViewById(R.id.button1);

		t = (InternalTask)getIntent().getSerializableExtra("task");

		priorityIcon = (ImageView) findViewById(R.id.image);

		tvTitle = (TextView) findViewById(R.id.titleText); // name

		tvDesc = (TextView) findViewById(R.id.descText);
		tvRemark = (TextView) findViewById(R.id.remarkText);

		tvStatus = (TextView) findViewById(R.id.statusText);
		tvCaller = (TextView) findViewById(R.id.callerText); // responsible

		callCaller = (ImageView) findViewById(R.id.callCallerImage);

		callCaller.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click: call "caller"
				if (callerPhone != null) {
					call(callerPhone);
				}
			}
		});

		btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
		           toast("edit not yet implemented.");				
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (debug)
			Log.i(TAG, "TaskDetailActivity - onResume");
		display();

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (debug)
			Log.i(TAG, "TaskTicketActivity - onPause");
		startService(new Intent(BackgroundCheck.class.getName()));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == 0) {
			String result = data.toURI();
			Toast.makeText(this, result, Toast.LENGTH_LONG).show();
		}
	}

	private void display() {
		if (debug)
			Log.i(TAG, "TaskTicketActivity - display()");
	    priorityIcon.setImageResource(t.prioImageResource(prioStrings));
		tvTitle.setText(t.name);
		tvStatus.setText("Status: " + t.status);

		// reset phone numbers and call icons.
		callerPhone = null;
		callCaller.setImageResource(R.drawable.nothing32);

		tvCaller.setText("resp.: " + t.person_id_friendlyname);

			Person p = personLookup.get(t.person_id);
			if (p != null) {
				if (p.getPhonenumber().length() > 7) {
					callCaller.setImageResource(R.drawable.call_contact);
					callerPhone = p.getPhonenumber();
				} else {
					callerPhone = null;
					callCaller.setImageResource(R.drawable.call_contact_off);
				}
			}

		tvDesc.setText(t.description);
		tvRemark.setText(t.remarks);

	}

	public void toast(String string) {
		Toast.makeText(this, string, Toast.LENGTH_LONG).show();
	}

	private void call(String num) {
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + num));
			startActivity(callIntent);

		} catch (ActivityNotFoundException activityException) {
			Log.e(TAG, "dialing - Call to " + num + " failed. ",
					activityException);
		}
	}

}
