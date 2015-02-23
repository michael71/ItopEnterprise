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

package de.itomig.itopenterprise.cmdb;

import android.util.Log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import de.itomig.itopenterprise.R;

import static de.itomig.itopenterprise.ItopConfig.ERROR;
import static de.itomig.itopenterprise.ItopConfig.INVALID_ID;
import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;

/**
 * Class for holding Itop Tickets.
 */

public class ItopTicket extends CMDBObject implements Serializable {
    // Serializable is needed to "hook it up" on an intent with
    private static final long serialVersionUID = -5998434779602343501L;
    private String type;  // can be UserRequest or Incident
    private String ref;
    private String title;
    private int priority;
    // 1 is highest in standard model
    // 'Class:UserRequest/Attribute:priority/Value:1' => 'critical',
    // 'Class:UserRequest/Attribute:priority/Value:2' => 'high',
    // 'Class:UserRequest/Attribute:priority/Value:3' => 'medium',
    // 'Class:UserRequest/Attribute:priority/Value:4' => 'low',

    private int caller_id;
    private int agent_id;

    private String start_date;    // im ITOP (SQL Format gespeichert, bei der Ausgabe umgewandelt)
    private String tto_escalation_deadline = "";
    private String status = "";
    private String last_update = "";

    private String description;
    private ArrayList<PublicLogEntry> public_log = new ArrayList<>();


    public ItopTicket(int id) {
        super(id);
        type = "UserRequest";
        ref = "";
        title = "no title";
        priority = 3;
        description = "-";
        caller_id = INVALID_ID;
        agent_id = INVALID_ID;
        tto_escalation_deadline = "";
        start_date = "";
        last_update = "";
     }

    public ItopTicket(String t, String r, String ti, String p, String d) {
        super(1);
        // ref,title,priority,description);
        type = t;
        ref = r;
        title = ti;
        priority = Integer.parseInt(p);
        description = d;
        caller_id = INVALID_ID;
        agent_id = INVALID_ID;
        tto_escalation_deadline = "";
        start_date = "";
        last_update = "";

    }

    public void addPublicLogEntry(PublicLogEntry ple) {
        public_log.add(ple);
    }

    public ItopTicket(String t) {
        super(1);
        this.type = t;
        this.public_log.add(new PublicLogEntry());
    }

    public void sortPublic_log() {
        Collections.sort(public_log, new CustomComparator());
    }

    private class CustomComparator implements Comparator<PublicLogEntry> {
        @Override
        public int compare(PublicLogEntry o1, PublicLogEntry o2) {
            return -o1.getDate().compareTo(o2.getDate());
        }
    }

    static String germanDate(String d) {
        // return d in 01-Mar-11 12:00 format.
        // check if there is a d Value
        if ((d == null) || (d.length() < 19)) return "";

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat de = new SimpleDateFormat("dd MMM yy - HH:mm");
        try {
            Date dateIn = df.parse(d);
//			System.out.println("datein  = " + d);
//			System.out.println("dateOut = " + de.format(dateIn));
            return de.format(dateIn);
        } catch (ParseException e) {
            Log.e(TAG, "ItopTicket germanDate - Date parse exception: Input=" + d);
            return null;
        }

    }

    public String getPublic_log() {
        StringBuilder sb = new StringBuilder("");
        for (PublicLogEntry s:public_log) {
            if (!s.message.isEmpty()) {
                sb.append(s.date);
                sb.append(" - ");
                sb.append(s.user_login);
                sb.append("\r\n\r\n");
                sb.append(s.message);
                sb.append("\r\n");
                sb.append("---------------------------------------------------------------------\r\n");
            }
        }
        return sb.toString();
    }

    public void setPublic_log0(String s) {
        public_log.clear();
        public_log.add(new PublicLogEntry(s));
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLast_update() {
        // format for output
        return germanDate(last_update);
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public String getType() {
        return type;
    }

    public void setType(String t) {
        this.type = t;
    }

    public boolean isError() {
        return (type.contains(ERROR));
    }

    public String getStart_date() {
        // format for output
        return germanDate(start_date);
    }

    public void setStart_date(String s) {
        this.start_date = s;
    }

    public int getCaller_id() {
        return caller_id;
    }

    public void setCaller_id(String caller_id) {
        try {
            this.caller_id = Integer.parseInt(caller_id);
        } catch (NumberFormatException e) {
            this.caller_id = INVALID_ID;
        }
    }

    public int getAgent_id() {
        return agent_id;
    }

    public void setAgent_id(String agent_id) {
        try {
            this.agent_id = Integer.parseInt(agent_id);
        } catch (NumberFormatException e) {
            this.agent_id = INVALID_ID;
        }
    }

    public String toShortString() {

        StringBuilder sb = new StringBuilder();

        sb.append(ref);
        if (title.length() != 0)
            sb.append(" - "); // avoid to display the '-' when the ticket is misused for showing errors etc.
        sb.append(title);
        return sb.toString();
    }

    public String toLongString() {
        // only used for debugging.
        StringBuilder sb = new StringBuilder();

        sb.append(ref);
        sb.append(" - Prio ");
        sb.append(priority);
        if (title.length() != 0) sb.append(" - ");  // see above.
        sb.append(title);
        sb.append("\nCaller: ");
        sb.append(caller_id);
        sb.append("\n");
        sb.append(start_date);
        if (tto_escalation_deadline != null) {
            sb.append("\nTTO-Escal: ");
            sb.append(tto_escalation_deadline);
        }
        sb.append("\n");
        sb.append(description);

        return sb.toString();
    }

    public String toMediumString() {

        StringBuilder sb = new StringBuilder();

        sb.append(ref);
        if (title.length() != 0) sb.append(" - ");  // see above.
        sb.append(title);
        sb.append(" - P");
        sb.append(priority);
        sb.append("\n");
        sb.append(description);

        return sb.toString();
    }

    public CharSequence getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        try {
            this.priority = Integer.parseInt(priority);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not convert priority to Integer! input=" + priority);
            this.priority = 2;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getTto_escalation_deadline() {
        // check if there is a tto_escalation_deadline Value
        if (tto_escalation_deadline.length() < 19) return ("");

        return germanDate(tto_escalation_deadline);
    }

    public void setTto_escalation_deadline(String tto_escalation_deadline) {
        this.tto_escalation_deadline = tto_escalation_deadline;
    }

    public Boolean isTtoEscalated() {
        // check if there is a tto_escalation_deadline Value
        if (tto_escalation_deadline.length() < 19) return false;

        // depends on correct itop date format
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateNow = formatter.format(currentDate.getTime());
        return (tto_escalation_deadline.compareTo(dateNow) < 0);
    }

    public int prioImageResource() {
        // return 1..3 star image, depending on priority
        int res;
        //if (debug) Log.d(TAG,"prio="+priority);
        switch (priority) {
            case 1:
                res = R.drawable.star4_on;   // highest
                break;
            case 2:
                res = R.drawable.star4_off_on_on_on;
                break;
            case 3:
                res = R.drawable.star4_off_off_on_on;
                break;
            case 4:
                res = R.drawable.star4_off_off_off_on;   // lowest prio
                break;
            default:
                if (debug) Log.d(TAG, "ItopTicket unknown prio=" + priority);
                res = R.drawable.star4_off;

        }
        return res;
    }

    public String getOQLStartDate() {
        return start_date;
    }

    public String getOQLLastUpdate() {
        return last_update;
    }

}
