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

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.itomig.itopenterprise.cmdb.InternalTask;

import static de.itomig.itopenterprise.ItopConfig.prioStrings;

public class TaskAdapter extends BaseAdapter {
    private Context context;
    private List<InternalTask> listTasks;

    public TaskAdapter(Context context, List<InternalTask> listTasks) {
        this.context = context;
        this.listTasks = listTasks;
    }

    public int getCount() {
        return listTasks.size();
    }

    public Object getItem(int position) {
        return listTasks.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        InternalTask entry = listTasks.get(position);
        if (convertView == null) {    // (re-)using the "convertView" is important for performance
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.itop_task_row, null);
        }
        if (position % 2 == 0) {
            convertView.setBackgroundColor(Color.rgb(255, 255, 255));
        } else {
            convertView.setBackgroundColor(Color.rgb(235, 235, 235));
        }

        TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
        TextView tvDesc = (TextView) convertView.findViewById(R.id.tv_desc);
        tvName.setText(entry.name);
        tvDesc.setText("(" + entry.person_id_friendlyname + ")   " + entry.description);

        ImageView prImage = (ImageView) convertView.findViewById(R.id.imagev);
        prImage.setImageResource(entry.prioImageResource(prioStrings));
        prImage.setFocusableInTouchMode(false);
        prImage.setFocusable(false);

        return convertView;
    }
}
