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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.net.URLEncoder;
import java.util.ArrayList;

import de.itomig.itopenterprise.cmdb.ItopTicket;

import static de.itomig.itopenterprise.ItopConfig.BACKGROUND_INTERVAL_MIN;
import static de.itomig.itopenterprise.ItopConfig.ERROR;
import static de.itomig.itopenterprise.ItopConfig.NOTIFICATION_ID_INCIDENT;
import static de.itomig.itopenterprise.ItopConfig.NOTIFY_NO;
import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;
import static de.itomig.itopenterprise.ItopConfig.itopmobileactivity;

/**
 * BackgroundCheck is a Service which queries itop-server for new tickets
 * either UserRequests(=non ITIL) or Incidents (=ITIL) depending on ITIL setting
 *
 * @author mblank
 */
public class BackgroundCheck extends Service {

    public static final String NEW_INCIDENT_BROADCAST = "new_incident_found";

    // SELECT Incident AS i WHERE (i.start_date > "2011-11-17 12:20:00" ) AND (i.priority >2)
    //                                             letzter Abruf               prio hoch

    //private String lastcalled = "%222011-12-01%2012:20:00%22"; // Datum example for OQL
    Intent intentToBroadcast;
    int counter = 0;
    private String oqlExpr0 = "SELECT UserRequest ";   // will be changed to SELECT Incident for ITIL ticket mgmt
    private String oqlExpr1 = " WHERE status!='closed' " +
            "AND status!='resolved' AND "; //priority=3 AND ";
    private String[] oqlType = {"", "start_date", "last_update"};
    // INTERVAL+1 for OQL expression just to be sure to never miss a new incident
    private String oqlExpr2 = " > DATE_SUB(NOW(),INTERVAL ";
    private String oqlExpr3 = " MINUTE)";
    private Boolean reqRunningFlag = false;
    //private static long lastChecked=SystemClock.elapsedRealtime();
    private Notification ticketNotification;
    private int mNotifyCondition;
    private ArrayList<ItopTicket> bgtickets = new ArrayList<ItopTicket>();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (debug) Log.i(TAG, "BG: Service creating");

        mNotifyCondition = ItopConfig.getItopNotifySetting();

        if (debug) Log.i(TAG, "BG: from settings: mNotifyCondition=" + mNotifyCondition);

        intentToBroadcast = new Intent(NEW_INCIDENT_BROADCAST);
        int icon = R.drawable.logo_itop_only_36;
        String text = getString(R.string.new_prio1_incident);
        ticketNotification = new Notification(icon, text, System.currentTimeMillis());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "BG: Service destroyed.");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mNotifyCondition = ItopConfig.getItopNotifySetting();

        if (mNotifyCondition != NOTIFY_NO) {
            if (debug) Log.i(TAG, "BG: checkforNewTickets()");
            triggerTicketRequestFromServer();
            // Lifetime of service ends when this trigger requests is ending ( PostExecute)
        } else {
            AlarmReceiver.releaseLock();
        }

        return Service.START_NOT_STICKY;
    }


    private void createNewIncidentNotification() {

        if (debug) Log.i(TAG, "BG: newIncNotification (mNotifyCondition=" + mNotifyCondition + ")");
        if (mNotifyCondition != NOTIFY_NO) {
            // notify only when user has set the appropriate notification setting
            // assemble info strings for Notification and for Broadcast Message
            // fire Notification and send Broadcast Intent
            StringBuilder info = new StringBuilder();
            StringBuilder note = new StringBuilder();

            // if more then one new ticket, mark here with n*
            if (bgtickets.size() > 1) {
                note.append(bgtickets.size());
                note.append( "* ");
            }

            // assemble strings for info (toast on UI) and note (notification)
            for (int i = 0; i < bgtickets.size(); i++) {
                info.append("\n");
                info.append(bgtickets.get(i).toShortString());
                note.append(" ");
                note.append(bgtickets.get(i).getTitle());  // display only title in notification
            }

            String expText = note.toString();  // text des incidents
            String title = getString(R.string.itop_prio1_incident); // - "+bgtickets.size()+" neue Incidents";

            Context context = getApplicationContext();
            Intent notifyIntent = new Intent(Intent.ACTION_MAIN);

            notifyIntent.setClass(context, itopmobileactivity);

            PendingIntent launchIntent = PendingIntent.getActivity(context, 0, notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            ticketNotification.setLatestEventInfo(context, title, expText, launchIntent);
            ticketNotification.flags = Notification.FLAG_AUTO_CANCEL;
            ticketNotification.when = java.lang.System.currentTimeMillis();
            //	ticketNotification.number = bgtickets.size();  // TODO this is prone to errors!
            // should be overwritten for every newly received ticket
            // should be counted in principle and only reset tot 0 in the Boadcast Receiver.
            // however, care should be taken to avoid displaying the same ticket 2 times!

            nm.notify(NOTIFICATION_ID_INCIDENT, ticketNotification);

            // send intent with info string
            intentToBroadcast.putExtra("incsText", info.toString());
            sendBroadcast(intentToBroadcast);
        }

    }


    private void triggerTicketRequestFromServer() {
        if (reqRunningFlag) return; // do nothing when already triggered


        // check notification settings first
        mNotifyCondition = ItopConfig.getItopNotifySetting();
        if (debug) Log.i(TAG, "BG: triggerR.. from Settings: mNotifyCondition=" + mNotifyCondition);

        try {
            if (mNotifyCondition != NOTIFY_NO) {
                if (ItopConfig.isEnabledlITILTicket()) {
                    oqlExpr0 = "SELECT Incident ";
                } else {
                    oqlExpr0 = "SELECT UserRequest ";
                }
                RequestTicketsFromServerTask reqServer = new RequestTicketsFromServerTask();
                String expr = oqlExpr0 + oqlExpr1 + oqlType[mNotifyCondition] + oqlExpr2 + (BACKGROUND_INTERVAL_MIN + 1) + oqlExpr3;
                //String expr=oqlExpr1;
                if (debug) Log.i(TAG, "BG: expr=" + expr);
                reqServer.execute(URLEncoder.encode(expr, "UTF-8"));
            }
        } catch (Exception e) {
            Log.e(TAG, "BG: ERROR " + e.toString());
        }

    }

    void mySleep() {
        try {
            Thread.sleep(100);  // wait a few milliseconds before starting the other server request
        } catch (InterruptedException e) {
            Log.e(TAG,"could not sleep");
        }
    }

    private void checkStop() {
        if (!reqRunningFlag) {
            //***************************   END OF BACKGROUND ACTIVITY ******************
            AlarmReceiver.releaseLock();
            if (debug) Log.i(TAG, "BG: checkStop is stopping the service....");
            stopSelf();
        } else {
            if (debug)
                Log.i(TAG, "BG: checkStop cannot stop the service! still active http requests.");
        }
    }

    /*
     *  the http request is done in the background as an AsyncTask
     */
    protected class RequestTicketsFromServerTask extends AsyncTask<String, Void, ArrayList<ItopTicket>> {

        @Override
        protected void onPreExecute() {
            reqRunningFlag = true;
        }

        @Override
        protected ArrayList<ItopTicket> doInBackground(String... expr) {
            ArrayList<ItopTicket> reqTickets = new ArrayList<ItopTicket>();
            try {
                reqTickets = de.itomig.itopenterprise.GetItopData.getTicketsFromItopServer(expr[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return reqTickets;
        }

        @Override
        protected void onPostExecute(ArrayList<ItopTicket> resTickets) {
            // TODO add error checking....
            bgtickets = new ArrayList<ItopTicket>(resTickets);

            if (debug) {
                Log.i(TAG, "BG: " + bgtickets.size() + " tickets received");
                for (int i = 0; i < bgtickets.size(); i++) {
                    Log.i(TAG, "BG: " + bgtickets.get(i).toShortString());
                }
            }
            bgtickets = de.itomig.itopenterprise.ItopUtils.removeRepeatingTickets(bgtickets);
            if (debug) {
                Log.i(TAG, "BG: " + bgtickets.size() + " tickets after rem.dups");
                for (int i = 0; i < bgtickets.size(); i++) {
                    Log.i(TAG, "BG: " + bgtickets.get(i).toShortString());
                }
            }
            if (bgtickets.size() > 0 && (!bgtickets.get(0).getType().equals(ERROR)))
                createNewIncidentNotification();
            reqRunningFlag = false;

            checkStop();  // check if service can be stopped
        }

    }

}