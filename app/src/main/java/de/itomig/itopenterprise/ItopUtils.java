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

import android.util.Log;

import java.util.ArrayList;

import de.itomig.itopenterprise.cmdb.ItopTicket;

import static de.itomig.itopenterprise.ItopConfig.TAG;

public class ItopUtils {

    static ArrayList<ItopTicket> stored = new ArrayList<ItopTicket>();

    /**
     * removes repeating ItopTickets from a list (defined by same "ref" and same
     * "lastUpdate" string as the last from the last call)
     * not persistent over restart of service ! TODO
     *
     * @param in input with arraylist of itop tickets
     * @return output list of itop tickets
     */
    protected static ArrayList<ItopTicket> removeRepeatingTickets(ArrayList<ItopTicket> in) {
        ArrayList<ItopTicket> out = new ArrayList<ItopTicket>();
        boolean foundDuplicate;

        if (in.isEmpty()) {
            // nothing to do in this case
            stored.clear();
            return in;
        }

        if (stored.isEmpty()) {
            // out = in
            // store in ->stored for later use
            for (int i = 0; i < in.size(); i++) {
                stored.add(in.get(i));   // copy out list entries into stored list
            }
            return in;
        }

        // else: check for duplicates ( in(i)==stored(j) ) and remove them from out list
        for (int i = 0; i < in.size(); i++) {
            // check for identical entries in stored -> then remove
            foundDuplicate = false;
            for (int j = 0; j < stored.size(); j++) {
                if (in.get(i).getRef().equals(stored.get(j).getRef())
                        && in.get(i).getLast_update().equals(stored.get(j).getLast_update())) {
                    // duplicate found, do not add to out list
                    foundDuplicate = true;
                    break;
                }
            }
            if (!foundDuplicate) {
                out.add(in.get(i));
            }
        }
        // out -> stored
        for (int i = 0; i < out.size(); i++) {
            stored.add(out.get(i));   // copy out list entries into stored list
        }
        return out;

    }

    /**
     * utility method to check the JSON response from iTop server for returning an error
     *
     * @param resp
     * @return error message (isEmpty() => no error)
     */
    static public String getItopError(String resp) {
        if (resp == null) {
            Log.e(TAG, "SERVER_ERROR: response = null.");
            return "SERVER_ERROR: response = null";
        }

        // check for ERROR in resp String
        if ((resp.length() >= 12)
                && (resp.substring(0, 12).equals("SERVER_ERROR"))) {
            Log.e(TAG, resp);
            return resp;

        }

        // check for iTop appplication error message in JSON string
        String message = GetItopJSON.getMessage(resp);
        if (message.length() > 0) {
            Log.e(TAG, "SERVER_ERROR: " +message);
            return "SERVER_ERROR: " + message;
          }
        return "";
    }
}
