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

public class InternalTaskActivity extends de.itomig.itopenterprise.MainActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String getTasks = "SELECT InternalTask WHERE status!='closed' AND status!='resolved'";

            //expression = URLEncoder.encode(getTasks, "UTF-8");
            serverExpression[0] =  "core/get";
            serverExpression[1] =  "InternalTask";
            serverExpression[2] =  "SELECT InternalTask WHERE person_id = :current_contact_id";
            serverExpression[3] = "name, priority, person_name, person_id, description, remarks, person_id_friendlyname";

    }

}

