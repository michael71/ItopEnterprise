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


import android.os.Bundle;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;

public class HelpdeskActivity extends de.itomig.itopenterprise.MainActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (debug) Log.i(TAG, "HelpdeskActivity - onCreate");
        String getUserRequests = "SELECT UserRequest WHERE status!='closed' AND status!='resolved'";


        serverExpression[0] = "core/get";
        serverExpression[1] = "UserRequest";
        serverExpression[2] = "SELECT UserRequest WHERE status!='closed' AND status!='resolved'";
        serverExpression[3] = "ref, title, priority, start_date, tto_escalation_deadline, caller_id, agent_id, status, last_update, description, public_log";

    }
}