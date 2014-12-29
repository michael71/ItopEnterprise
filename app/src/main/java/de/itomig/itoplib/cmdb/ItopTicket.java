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

package de.itomig.itoplib.cmdb;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.util.Log;
import de.itomig.itoplib.R;
import static de.itomig.itoplib.ItopConfig.*;

/**
 * Class for holding Itop Tickets.
 * 
 *  
 */

public class ItopTicket implements  Serializable {
	// Serializable is needed to "hook it up" on an intent with 
	private static final long serialVersionUID = -5998434779602343501L;
	private String type;
	private String ref;
	private String title;
	private int priority;
	// 1 is highest in standard model
	// 'Class:UserRequest/Attribute:priority/Value:1' => 'critical',
	// 'Class:UserRequest/Attribute:priority/Value:2' => 'high',
	// 'Class:UserRequest/Attribute:priority/Value:3' => 'medium',
	// 'Class:UserRequest/Attribute:priority/Value:4' => 'low',

	private int callerID;
	private int agentID;


	private String startDate;    // im ITOP (SQL Format gespeichert, bei der Ausgabe umgewandelt)
	private String ttoEscalationDate="";
	private String status="";
	private String lastUpdate="";

	private String description;
	private String ticketLog;

	public ItopTicket(String t, String r, String ti, String p, 	String d) {
		// ref,title,priority,description);
		type = t;
		ref = r; 
		title = ti;
		priority = Integer.parseInt(p);
		description = d;
		callerID=INVALID_ID;
		agentID=INVALID_ID;
		ttoEscalationDate="";
		startDate="";
		lastUpdate="";
		ticketLog="";
	}


	public String getTicketLog() {
		return ticketLog;
	}
	public void setTicketLog(String ticketLog) {
		this.ticketLog = ticketLog;
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public String getLastUpdate() {
		// format for output
		return germanDate(lastUpdate);
	}
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	public String getType() {
		return type;
	}
	
	public boolean isError() {
		return (type.contains(ERROR));
	}
	public String getStartDate() {
		// format for output
		return germanDate(startDate);
	}
	public void setStartDate(String s) {
		this.startDate = s;
	}
 	public void setPriority(String priority) {
 		try {
 			this.priority = Integer.parseInt(priority);
 		} catch (NumberFormatException e) {
 			Log.e(TAG,"could not convert priority to Integer! input="+priority);
			this.priority = 2;
		}
 	}
	public ItopTicket(String t) {
		this.type = t;
	}
	public ItopTicket(String error, String text) {
		this.type = error;
		this.title = text;
	}


	public int getCallerID() {
		return callerID;
	}

	public void setCallerID(String callerID) {
		try {
		    this.callerID = Integer.parseInt(callerID);
		} catch (NumberFormatException e) {
			this.callerID=INVALID_ID;
		}
	}

	public int getAgentID() {
		return agentID;
	}

	public void setAgentID(String agentID) {
		try {
		    this.agentID = Integer.parseInt(agentID);
		} catch (NumberFormatException e) {
			this.agentID=INVALID_ID;
		}
	}

	public String toShortString() {

		StringBuilder sb = new StringBuilder();

		sb.append(ref);
		if (title.length() != 0) sb.append(" - "); // avoid to display the '-' when the ticket is misused for showing errors etc. 
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
		sb.append(callerID);
		sb.append("\n");
		sb.append(startDate);
		if (ttoEscalationDate != null) {
			sb.append("\nTTO-Escal: ");
			sb.append(ttoEscalationDate);
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
	public void setRef(String ref) {
		this.ref = ref;
	}
	
	public void setTitle(String t) {
		this.title = t;
	}
	public void setDescription(String desc) {
		this.description = desc;
	}
	public void setType(String t) {
		this.type = t;
	}
	public CharSequence getDescription() {
		return description;
	}
	public int getPriority() {
		return priority;
	}
	public String getTitle() {
		return title;
	}
	public String getRef() {
		return ref;
	}

	public String getTtoEscalationDate() {
		// check if there is a ttoEscalationDate Value
		if (ttoEscalationDate.length() < 19) return ("");
		
		return germanDate(ttoEscalationDate);
	}
	public void setTtoEscalationDate(String ttoEscalationDate) {
		this.ttoEscalationDate = ttoEscalationDate;
	}
	
	public Boolean isTtoEscalated() {
		// check if there is a ttoEscalationDate Value
	    if (ttoEscalationDate.length() < 19) return false;
		
		// depends on correct itop date format
		Calendar currentDate = Calendar.getInstance(); 
		SimpleDateFormat formatter=  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateNow = formatter.format(currentDate.getTime());	
		return (ttoEscalationDate.compareTo(dateNow) < 0);
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
			if (debug) Log.d(TAG,"ItopTicket unknown prio="+priority);
			res= R.drawable.star4_off;

		} 
		return res;
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
			Log.e(TAG,"ItopTicket germanDate - Date parse exception: Input="+d);
			return null;
		}

	}

	public String getOQLStartDate() {
		return startDate;
	}
	
	public String getOQLLastUpdate() {
		return lastUpdate;
	}

}
