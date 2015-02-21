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

package de.itomig.itopenterprise;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.itomig.itopenterprise.cmdb.InternalTask;
import de.itomig.itopenterprise.cmdb.ItopTicket;

import static de.itomig.itopenterprise.ItopConfig.ERROR;
import static de.itomig.itopenterprise.ItopConfig.NOTIFICATION_ID_INCIDENT;
import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;
import static de.itomig.itopenterprise.ItopConfig.itopAppContext;
import static de.itomig.itopenterprise.ItopConfig.personLookup;
import static de.itomig.itopenterprise.ItopConfig.prioStrings;
import static de.itomig.itopenterprise.ItopConfig.tasks;
import static de.itomig.itopenterprise.ItopConfig.tickets;

public class MainActivity extends Activity {

    protected int timerTick = 0;
    protected String expression;
    List<ItopTicket> listOfTickets;
    de.itomig.itopenterprise.TicketAdapter adapter;
    List<InternalTask> listOfTasks;
    de.itomig.itopenterprise.TaskAdapter taskAdapter;
    Comparator<ItopTicket> comperator = new Comparator<ItopTicket>() {
        // compare priority first, then compare "lastUpdate" date of ticket
        public int compare(ItopTicket object1, ItopTicket object2) {
            if (object1.getPriority() == object2.getPriority()) {
                return (-object1.getOQLLastUpdate().compareTo(
                        object2.getOQLLastUpdate()));
            }
            if (object1.getPriority() > object2.getPriority()) {
                return 1;
            } else {
                return -1;
            }
        }
    };
    Comparator<InternalTask> comperatorTask = new Comparator<InternalTask>() {
        // compare priority first, then compare "lastUpdate" date of ticket
        public int compare(InternalTask object1, InternalTask object2) {
            if (ItopConfig.getNumericalPriority(object1.priority) >= ItopConfig
                    .getNumericalPriority(object2.priority)) {
                return 1;
            } else {
                return -1;
            }
        }
    };
    private NotificationManager notificationManager;
    private Handler mTimer = new Handler(); // used for all timer based actions
    private boolean taskRequestRunningFlag = false;
    private boolean ticketRequestRunningFlag = false;
    private ListView lv;
    private TextView emptyView;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String incsText = intent.getStringExtra("incsText");

            if ((incsText != null) && (incsText.length() > 0)) {
                toast("Prio1 Incident(s)!\n" + incsText);
                notificationManager.cancel(NOTIFICATION_ID_INCIDENT);
            }
        }
    };
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long refreshRate = 1000 * ItopConfig.getRefreshRateSeconds();
            update();
            mTimer.removeCallbacks(this);
            mTimer.postDelayed(this, refreshRate);
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (debug)
            Log.i(TAG, this.getLocalClassName() + " - onCreate");
        setContentView(R.layout.ticket);

        initPrioStrings();

        lv = (ListView) findViewById(R.id.listview01);

        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {

                if (debug)
                    Log.d(TAG, "click, my adapter is"
                            + arg0.getAdapter().toString());

                if (arg0.getAdapter().toString().contains("Task")) {
                    Intent myIntent = new Intent(MainActivity.this,
                            de.itomig.itopenterprise.TaskDetailActivity.class);
                    myIntent.putExtra("task", tasks.get(position));
                    startActivity(myIntent);
                } else {
                    // Do not open if tickets[0].type equals the error type.
                    if (!tickets.get(position).getType().equals(ERROR)) {

                        Intent myIntent = new Intent(MainActivity.this,
                                de.itomig.itopenterprise.TicketDetailActivity.class);
                        myIntent.putExtra("ticket", tickets.get(position));
                        startActivity(myIntent);
                    }
                }
            }
        });
        emptyView = (TextView) findViewById(R.id.empty);
        lv.setEmptyView(emptyView);

        if (tickets != null)
            lv.setAdapter(new de.itomig.itopenterprise.TicketAdapter(MainActivity.this, tickets));
        String svcName = Context.NOTIFICATION_SERVICE;
        notificationManager = (NotificationManager) getSystemService(svcName);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (debug)
            Log.i(TAG, this.getLocalClassName() + " - onResume");
        notificationManager.cancel(NOTIFICATION_ID_INCIDENT);

        if (debug)
            Log.i(TAG,
                    this.getLocalClassName() + " lookup(20)="
                            + personLookup.get(20));

        if (this.getLocalClassName().contains("Task")) {
            if (tasks != null) {
                lv.setAdapter(new de.itomig.itopenterprise.TaskAdapter(MainActivity.this, tasks));
                ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
            }

        } else {
            // use data which are persistent in ItopApplication if stored by
            // same activity
            if (tickets != null) {
                lv.setAdapter(new de.itomig.itopenterprise.TicketAdapter(MainActivity.this, tickets));
                ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
            }
        }
        startTimer();
        update();
        registerReceiver(broadcastReceiver, new IntentFilter(
                BackgroundCheck.NEW_INCIDENT_BROADCAST));

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (debug)
            Log.i(TAG, this.getLocalClassName() + " - onPause");
        stopTimer();
        unregisterReceiver(broadcastReceiver);
    }

    private void update() {

        getParent().setProgressBarIndeterminateVisibility(true);
        getParent().setProgressBarVisibility(true);

        if (this.getLocalClassName().contains("Task")) {
            if (taskRequestRunningFlag == false) {
                taskRequestRunningFlag = true;
                if (debug)
                    Log.i(TAG, this.getLocalClassName() + " - update() Tasks");

                if (DataConnection.isConnected(itopAppContext)) {
                    RequestJSONDataFromServerTask reqServer = new RequestJSONDataFromServerTask();
                    reqServer.execute("");
                } else {
                    toast("no dataconnection, cannot reach itop server.");
                }
            }

        } else {
            if (ticketRequestRunningFlag == false) {
                ticketRequestRunningFlag = true;
                if (debug)
                    Log.i(TAG, this.getLocalClassName() + " - update() Tickets");
                RequestTicketsFromServerTask reqServer = new RequestTicketsFromServerTask();
                reqServer.execute(expression);
            }
        }

    }

    private void startTimer() {
        long refreshRate = 1000L * ItopConfig.getRefreshRateSeconds(); // milliseconds
        mTimer.removeCallbacks(mUpdateTimeTask);
        mTimer.postDelayed(mUpdateTimeTask, refreshRate);
    }

    private void stopTimer() {
        mTimer.removeCallbacks(mUpdateTimeTask);
    }

    public void toast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    // language dependent resources string !!!
    public void initPrioStrings() {
        Resources res = getResources();
        prioStrings = res.getStringArray(R.array.priority);
    }

    protected class RequestTicketsFromServerTask extends
            AsyncTask<String, Void, ArrayList<ItopTicket>> {

        @Override
        protected void onPreExecute() {
            emptyView.setText(getString(R.string.loading));
        }

        @Override
        protected ArrayList<ItopTicket> doInBackground(String... expr) {
            ArrayList<ItopTicket> reqTickets = new ArrayList<ItopTicket>();
            try {
                reqTickets = GetItopData.getTicketsFromItopServer(expr[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return reqTickets;
        }

        @Override
        protected void onPostExecute(ArrayList<ItopTicket> resTickets) {
            ticketRequestRunningFlag = false;
            getParent().setProgressBarIndeterminateVisibility(false);
            getParent().setProgressBarVisibility(false);

            if (resTickets == null) {
                Log.e(TAG,
                        "RequestTicketsFromServerTask - postexecute: received null response");
                return;
            }
            // TODO add error checking....
            tickets = new ArrayList<ItopTicket>(resTickets);

            // sort by priority
            Collections.sort(tickets, comperator);

            lv.setAdapter(new de.itomig.itopenterprise.TicketAdapter(MainActivity.this, tickets));
            ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();

            if (debug)
                Log.i(TAG, "RequestTicketsFromServerTask - postexecute");
            emptyView.setText(getString(R.string.no_tickets));
        }

    }

    /**
     * AsyncTask for requesting some JSON data from the itop server
     *
     * @author mblank
     */
    protected class RequestJSONDataFromServerTask extends
            AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            emptyView.setText(getString(R.string.loading));

        }

        @Override
        protected String doInBackground(String... expr) {
            String resp = "";
            try {

                resp = GetItopJSON
                        .postJsonToItopServer(
                                "core/get",
                                "InternalTask",
                                "SELECT InternalTask WHERE person_id = :current_contact_id",
                                "name, priority, person_name, person_id, description,remarks,person_id_friendlyname"); // expr[0]

            } catch (Exception e) {
                e.printStackTrace();
            }

            return resp;
        }

        @Override
        protected void onPostExecute(String resp) {
            taskRequestRunningFlag = false;
            getParent().setProgressBarIndeterminateVisibility(false);
            getParent().setProgressBarVisibility(false);

            if (resp == null) {
                Log.e(TAG, "server response = null.");
                toast("server response = null.");
                return;
            }

            // check for ERROR in resp String
            if ((resp.length() >= 5)
                    && (resp.substring(0, 5).toLowerCase().equals("error"))) {
                Log.e(TAG, "server error =" + resp);
                toast("server error =" + resp);
                return;

            }

            // check for error message in JSON string
            String message = GetItopJSON.getMessage(resp);
            if (message.length() > 0) {
                Log.e(TAG, "server error =" + message);
                if (message.toLowerCase().contains("not a valid class")) {
                    toast("Task Extension not installed on iTop Server, disable Tasks!");
                } else {
                    toast("server error =" + message);
                }
            }

            if (debug)
                Log.d(TAG, "json response - postexecute" + resp);

            Type type = new TypeToken<InternalTask>() {
            }.getType();
            tasks = GetItopJSON.getArrayFromJson(resp, type);

            if (tasks != null) {
                Log.d(TAG, "#tasks= " + tasks.size());

            }

            // sort by priority
            Collections.sort(tasks, comperatorTask);

            lv.setAdapter(new TaskAdapter(MainActivity.this, tasks));
            ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
            emptyView.setText(getString(R.string.no_tasks));
        }
    }

}
