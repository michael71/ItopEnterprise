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

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.itomig.itopenterprise.cmdb.ItopTicket;

public class TicketAdapter extends BaseAdapter {
    private Context context;
    private List<ItopTicket> listItopTicket;

    public TicketAdapter(Context context, List<ItopTicket> listItopTicket) {
        this.context = context;
        this.listItopTicket = listItopTicket;
    }

    public int getCount() {
        return listItopTicket.size();
    }

    public Object getItem(int position) {
        return listItopTicket.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ItopTicket entry = listItopTicket.get(position);
        if (convertView == null) {    // (re-)using the "convertView" is important for performance
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.itop_ticket_row, null);
        }
        TextView tvRef = (TextView) convertView.findViewById(R.id.tv_ticket);
        tvRef.setText(entry.toShortString());
        if (entry.isError()) {
            tvRef.setBackgroundColor(Color.argb(100, 255, 100, 100));
        } else {
            tvRef.setBackgroundColor(Color.argb(100, 255, 255, 255));
        }

        ImageView prImage = (ImageView) convertView.findViewById(R.id.imagev);

        prImage.setImageResource(entry.prioImageResource());
        prImage.setFocusableInTouchMode(false);
        prImage.setFocusable(false);

        ImageView ttoImage = (ImageView) convertView.findViewById(R.id.imagev2);
        if (entry.isTtoEscalated()) ttoImage.setImageResource(R.drawable.alarm_clock);

        return convertView;
    }
}
