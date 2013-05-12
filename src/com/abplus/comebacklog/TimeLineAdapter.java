package com.abplus.comebacklog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.abplus.comebacklog.parsers.TimeLine;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/10 14:26
 */
public class TimeLineAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private TimeLine timeLine;

    TimeLineAdapter(Context context, LayoutInflater inflater, TimeLine timeLine) {
        super();
        this.context = context;
        this.inflater = inflater;
        this.timeLine = timeLine;
    }

    @Override
    public int getCount() {
        return timeLine.count();
    }

    @Override
    public Object getItem(int position) {
        return timeLine.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout result = (LinearLayout)convertView;

        if (result == null) {
            result = (LinearLayout)inflater.inflate(R.layout.list_item, null);
        }

        TimeLine.Item item = timeLine.get(position);
        result.setTag(item);

        TextView time = (TextView)result.findViewById(R.id.time);
        TextView user = (TextView)result.findViewById(R.id.user);
        TextView type = (TextView)result.findViewById(R.id.type);
        TextView key = (TextView)result.findViewById(R.id.key);
        TextView summary = (TextView)result.findViewById(R.id.summary);
        TextView content = (TextView)result.findViewById(R.id.content);

        time.setText(dateString(item.getUpdatedOn()));
        user.setText(item.getUser().getName());
        type.setText(item.getType().getName());
        key.setText(item.getIssue().getKey());
        summary.setText(item.getIssue().getSummary());
        content.setText(item.getContent());

        switch (item.getType().getId()) {
            case 1:
                type.setBackgroundColor(context.getResources().getColor(R.color.type_subject));
                break;
            case 2:
                type.setBackgroundColor(context.getResources().getColor(R.color.type_update));
                break;
            default:
                type.setBackgroundColor(context.getResources().getColor(R.color.type_comment));
                break;
        }

        ImageView icon = (ImageView)result.findViewById(R.id.icon);
        Drawable drawable = BackLogCache.sharedInstance().iconOf(item.getUser().getId());
        if (drawable == null) {
            icon.setImageResource(R.drawable.ic_dummy);
        } else {
            icon.setImageDrawable(drawable);
        }

        return result;
    }

    private String dateString(String date) {
        if (date == null) {
            return null;
        } else {
//        String y = date.substring(0, 4);
            String m = date.length() <  6 ? "" : date.substring( 4,  6);
            String d = date.length() <  8 ? "" : date.substring( 6,  8);
            String h = date.length() < 10 ? "" : date.substring( 8, 10);
            String n = date.length() < 12 ? "" : date.substring(10, 12);
//        String s = date.substring(12);

            return m + "/" + d + " " + h + ":" + n;
        }
    }
}
