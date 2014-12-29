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

import java.util.ArrayList;
import de.itomig.itoplib.cmdb.ItopTicket;
import de.itomig.itoplib.cmdb.Person;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import static de.itomig.itoplib.ItopConfig.*;

public class TicketDetailActivity extends Activity {
	private TextView tvRef, tvTitle, tvDesc, tvDate, tvStatus, tvLastUpdate;
	private TextView tvTtoEscal, tvLog, tvCaller, tvAgent;
	private ImageView priorityIcon, alarmIcon, callCaller, callAgent;
	private ItopTicket t;

	private String callerPhone;
	private String agentPhone;

	private boolean reqRunningFlag = false; // must avoid double call of Async
											// Task
	NotificationManager notificationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (debug)
			Log.i(TAG, "TicketDetailActivity - onCreate");

		setContentView(R.layout.ticket_details);

		t = (ItopTicket) getIntent().getSerializableExtra("ticket");

		priorityIcon = (ImageView) findViewById(R.id.image);
		alarmIcon = (ImageView) findViewById(R.id.image2);
		// image.setImageResource(R.drawable.star3_off_off_on);
		tvRef = (TextView) findViewById(R.id.refText);

		tvTitle = (TextView) findViewById(R.id.titleText);

		tvDesc = (TextView) findViewById(R.id.descText);
		tvLog = (TextView) findViewById(R.id.logText);
		tvDate = (TextView) findViewById(R.id.dateText);
		tvLastUpdate = (TextView) findViewById(R.id.lastUpdateText);
		tvTtoEscal = (TextView) findViewById(R.id.ttoEscalText);
		tvStatus = (TextView) findViewById(R.id.statusText);
		tvCaller = (TextView) findViewById(R.id.callerText);
		tvAgent = (TextView) findViewById(R.id.agentText);

		callCaller = (ImageView) findViewById(R.id.callCallerImage);
		callAgent = (ImageView) findViewById(R.id.callAgentImage);

		String svcName = Context.NOTIFICATION_SERVICE;
		notificationManager = (NotificationManager) getSystemService(svcName);

		tvCaller.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click: call "caller"
				if (callerPhone != null) {
					call(callerPhone);
				}
			}

		});

		callCaller.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click: call "caller"
				if (callerPhone != null) {
					call(callerPhone);
				}
			}

		});

		tvAgent.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click: call "agent"
				if (agentPhone != null) {
					call(agentPhone);
				}
			}
		});
		callAgent.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click: call "agent"
				if (agentPhone != null) {
					call(agentPhone);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (debug)
			Log.i(TAG, "TicketTicketActivity - onResume");
		registerReceiver(broadcastReceiver, new IntentFilter(
				BackgroundCheck.NEW_INCIDENT_BROADCAST));
		notificationManager.cancel(NOTIFICATION_ID_INCIDENT);
		stopService(new Intent(BackgroundCheck.class.getName()));
		display();
		dispCallerAndAgent(); // look up friendly names and phone numbers,
								// display them.
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (debug)
			Log.i(TAG, "TicketTicketActivity - onPause");
		unregisterReceiver(broadcastReceiver);
		startService(new Intent(BackgroundCheck.class.getName()));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == 0) {
			String result = data.toURI();
			Toast.makeText(this, result, Toast.LENGTH_LONG);
		}
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			notificationManager.cancel(NOTIFICATION_ID_INCIDENT);
			updateUI(intent);
		}
	};

	private void updateUI(Intent intent) {
		String incsText = intent.getStringExtra("incsText");
		toast(" Neue Prio3 Incidents\n" + incsText);
	}

	private void display() {
		priorityIcon.setImageResource(t.prioImageResource());
		tvRef.setText(t.getRef());
		tvTitle.setText(t.getTitle());
		tvDate.setText(t.getStartDate());
		tvLastUpdate.setText(t.getLastUpdate());
		// check if tto escalation

		if (t.isTtoEscalated()) {
			tvTtoEscal.setTextColor(Color.RED);
			tvTtoEscal.setText(t.getTtoEscalationDate());
			alarmIcon.setImageResource(R.drawable.alarm_clock_64);
		} else {
			tvTtoEscal.setTextColor(Color.BLACK);
			tvTtoEscal.setText(t.getTtoEscalationDate());
			alarmIcon.setImageResource(R.drawable.alarm_clock_64_off);
		}

		tvStatus.setText("Status: " + t.getStatus());

		// reset phone numbers and call icons.
		callerPhone = null;
		agentPhone = null;
		callCaller.setImageResource(R.drawable.nothing32);
		callAgent.setImageResource(R.drawable.nothing32);
		if (t.getCallerID() != INVALID_ID) {
			tvCaller.setText("caller# " + t.getCallerID());
		} else {
			tvCaller.setText("");
		}
		if (t.getAgentID() != INVALID_ID) {
			tvAgent.setText("agent# " + t.getAgentID());
		} else {
			tvAgent.setText("");
		}
		tvDesc.setText(t.getDescription());
		if (t.getTicketLog().length() > 1) {
			String log2 = t.getTicketLog().replace("============", "");

			tvLog.setText(log2.replace("========== ", "\n"));
		}

	}

	private void dispCallerAndAgent() {
		// determine friendly name of both caller and agent
		ArrayList<Integer> ids = new ArrayList<Integer>();

		ids.clear();
		if (t.getCallerID() != INVALID_ID) {
			ids.add(t.getCallerID());
		}

		if (t.getAgentID() != INVALID_ID) {
			ids.add(t.getAgentID());
		}

		Person p = personLookup.get(t.getCallerID());

		if (p != null) {
			if (p.getPhonenumber().length() > 7) {
				tvCaller.setText("caller: " + p.getFriendlyname());
				callCaller.setImageResource(R.drawable.call_contact);
				callerPhone = p.getPhonenumber();
			} else {
				tvCaller.setText("caller: " + p.getFriendlyname());
				callerPhone = null;
				callCaller.setImageResource(R.drawable.call_contact_off);
			}
		}

		p = personLookup.get(t.getAgentID());
		if (p != null) {
			if (p.getPhonenumber().length() > 7) {
				tvAgent.setText("agent: " + p.getFriendlyname());
				callAgent.setImageResource(R.drawable.call_contact);
				agentPhone = p.getPhonenumber();
			} else {
				tvAgent.setText("agent: " + p.getFriendlyname());
				callAgent.setImageResource(R.drawable.call_contact_off);
				agentPhone = null;
			}
		}

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
