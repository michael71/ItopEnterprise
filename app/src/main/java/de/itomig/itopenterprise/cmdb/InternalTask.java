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

package de.itomig.itopenterprise.cmdb;

import android.util.Log;

import de.itomig.itopenterprise.R;

import static de.itomig.itopenterprise.ItopConfig.INVALID_ID;
import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;

/**
 * Class for holding Itop InternalTasks
 */

public class InternalTask extends CMDBObject {

    private static final long serialVersionUID = -1645221039655675557L;

    public String name;
    public String status; // "in Arbeit", " ...."
    public String priority; // niedrig, mittel, hoch
    public int org_id;
    public String org_name;
    public int team_id;
    public String team_name;
    public int person_id;
    public String person_name;   // =last name
    public String description;
    public String remarks;
    public String friendlyname;
    public String org_id_friendlyname;
    public String team_id_friendlyname;
    public String person_id_friendlyname;

    public InternalTask(int id) {
        super(id);
    }

    public String toShortString() {

        StringBuilder sb = new StringBuilder();

        sb.append(name);
        if (name.length() != 0)
            sb.append(" - "); // avoid to display the '-' when the ticket is misused for showing errors etc.
        sb.append("\nResp: ");
        sb.append(person_id_friendlyname + "\n");
        sb.append(description);
        return sb.toString();
    }

    public int prioImageResource(String[] allowed_prios) {

        // return 1..3 star image, depending on priority
        int res;
        if (debug) Log.d(TAG, "task prio=" + priority);

        int numPrio = INVALID_ID;
        for (int i = 0; i < allowed_prios.length; i++) {
            if (priority.equals(allowed_prios[i])) numPrio = i + 1;  // 1=high, 2=medium, 3=low
        }
        switch (numPrio) {
            case 1:
                res = R.drawable.star3_on_tr1;  // highest
                break;
            case 2:
                res = R.drawable.star3_off_on_on_tr1;
                break;
            case 3:
                res = R.drawable.star3_off_off_on_tr1;
                break;
            default:
                if (debug) Log.d(TAG, "ItopTicket unknown prio=" + priority);
                res = R.drawable.star3_off;
        }
        return res;
    }


}
