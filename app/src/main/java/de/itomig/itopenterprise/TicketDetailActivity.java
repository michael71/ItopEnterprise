// Copyright (C) 2011-2013 ITOMIG GmbH
//
//   This file is part of iTopEnterprise.
//
//   iTopEnterprise is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.
//
//   iTopEnterprise is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with iTopEnterprise. If not, see <http://www.gnu.org/licenses/>
package de.itomig.itopenterprise;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import de.itomig.itopenterprise.cmdb.ItopTicket;
import de.itomig.itopenterprise.cmdb.Person;

import static de.itomig.itopenterprise.ItopConfig.INVALID_ID;
import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;

import static de.itomig.itopenterprise.ItopConfig.personLookup;


public class TicketDetailActivity extends Activity {
    // Task
    //NotificationManager notificationManager;
    private TextView tvRef, tvTitle, tvDesc, tvDate, tvStatus, tvLastUpdate;
    private TextView tvTtoEscal, tvLog, tvCaller, tvAgent;
    private ImageView priorityIcon, alarmIcon, callCaller, callAgent;
    private ItopTicket t;
    private String callerPhone;
    private String agentPhone;

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
        display();
        dispCallerAndAgent(); // look up friendly names and phone numbers,
        // display them.
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (debug)
            Log.i(TAG, "TicketTicketActivity - onPause");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            String result = data.toURI();
            Toast.makeText(this, result, Toast.LENGTH_LONG);
        }
    }

    private void display() {
        priorityIcon.setImageResource(t.prioImageResource());
        tvRef.setText(t.getRef());
        tvTitle.setText(t.getTitle());
        tvDate.setText(t.getStart_date());
        tvLastUpdate.setText(t.getLast_update());
        // check if tto escalation

        if (t.isTtoEscalated()) {
            tvTtoEscal.setTextColor(Color.RED);
            tvTtoEscal.setText(t.getTto_escalation_deadline());
            alarmIcon.setImageResource(R.drawable.alarm_clock_64);
        } else {
            tvTtoEscal.setTextColor(Color.BLACK);
            tvTtoEscal.setText(t.getTto_escalation_deadline());
            alarmIcon.setImageResource(R.drawable.alarm_clock_64_off);
        }

        tvStatus.setText("Status: " + t.getStatus());

        // reset phone numbers and call icons.
        callerPhone = null;
        agentPhone = null;
        callCaller.setImageResource(R.drawable.nothing32);
        callAgent.setImageResource(R.drawable.nothing32);
        if (t.getCaller_id() != INVALID_ID) {
            tvCaller.setText("caller# " + t.getCaller_id());
        } else {
            tvCaller.setText("");
        }
        if (t.getAgent_id() != INVALID_ID) {
            tvAgent.setText("agent# " + t.getAgent_id());
        } else {
            tvAgent.setText("");
        }
        tvDesc.setText(t.getDescription());
        if (t.getPublic_log().length() > 1) {
            String log2 = t.getPublic_log().replace("============", "");

            tvLog.setText(log2.replace("========== ", "\n"));
        }

    }

    private void dispCallerAndAgent() {
        // determine friendly name of both caller and agent
        if (debug) Log.d(TAG,"display caller and agent");
        boolean retrieve = false;
        Person p = personLookup.get(t.getCaller_id());

        if (p != null) {
            updateCaller(p);
        } else {
            retrieve = true;
        }

        p = personLookup.get(t.getAgent_id());
        if (p != null) {
            updateAgent(p);
        } else {
            retrieve = true;
        }

        if (retrieve) {
            RefreshPersonsFromServerTask reqPersons = new RefreshPersonsFromServerTask();
            String expr = "SELECT Person WHERE id = " + t.getAgent_id()+ " OR id = "+ t.getCaller_id() ;
            if (debug) Log.d(TAG,"refresh persons = "+expr);
            try {
                reqPersons.execute(URLEncoder.encode(expr, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG,e.getMessage());
            }
        }
    }

    private void updateCaller(Person p) {
        if (p == null) return;
        if (p.getPhonenumber().length() > 3) {
            tvCaller.setText("caller: " + p.getFriendlyname());
            callCaller.setImageResource(R.drawable.call_contact);
            callerPhone = p.getPhonenumber();
        } else {
            tvCaller.setText("caller: " + p.getFriendlyname());
            callerPhone = null;
            callCaller.setImageResource(R.drawable.call_contact_off);
        }
    }

    private void updateAgent(Person p) {
        if (p == null) return;
        if (p.getPhonenumber().length() > 3) {
            tvAgent.setText("agent: " + p.getFriendlyname());
            callAgent.setImageResource(R.drawable.call_contact);
            agentPhone = p.getPhonenumber();
        } else {
            tvAgent.setText("agent: " + p.getFriendlyname());
            callAgent.setImageResource(R.drawable.call_contact_off);
            agentPhone = null;
        }
    }
    public void toast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    private void call(String num) {
        boolean hasTelephony = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        if (!hasTelephony) return;

        try {
            // using DIAL instead of ACTION_CALL to avoid need of
            // <uses-permission android:name="android.permission.CALL_PHONE" />
            // to be have the app listed also for tablets
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + num));
            startActivity(callIntent);

        } catch (ActivityNotFoundException activityException) {
            Log.e(TAG, "dialing - Call to " + num + " failed. ",
                    activityException);
        }
    }

    class RefreshPersonsFromServerTask extends
            AsyncTask<String, Void, ArrayList<Person>> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<Person> doInBackground(String... expr) {
            ArrayList<Person> persons = new ArrayList<Person>();
            if (debug) Log.d(TAG,"RefreshPersonsFromServer="+expr[0]);
            try {
                persons = GetItopData.getPersonsFromItopServer(expr[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return persons;
        }

        @SuppressLint("UseSparseArrays") // sparse arrays cannot be iterated.
        @Override
        protected synchronized void onPostExecute(ArrayList<Person> persons) {

            if (debug)
                Log.i(TAG,
                        "onPostExecute - RefreshPersonsFromServer");

            if (persons == null) {
                Log.e(TAG, "empty response when req. Person List. - RefreshPersonsFromServer");
                return;
            }


            for (Person p : persons) {
                personLookup.put(p.getId(), p);
                if (debug)
                    Log.d(TAG, "PersonRefresh - setting person id=" + p.getId() + " to name=" + p.getFriendlyname() + " org_id=" + p.getOrg_id());
            }
            Person p = personLookup.get(t.getCaller_id());
            updateCaller(p);

            p = personLookup.get(t.getAgent_id());
            updateAgent(p);

        }
    }

}
