package com.abplus.comebacklog;

import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.abplus.comebacklog.parsers.Comments;
import com.abplus.comebacklog.parsers.Issue;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/10 14:33
 */
public class CommentsAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    Comments comments;
    Issue issue;

    CommentsAdapter(LayoutInflater inflater, Comments comments, Issue issue) {
        super();
        this.inflater = inflater;
        this.comments = comments;
        this.issue = issue;
    }

    @Override
    public int getCount() {
        return comments.count() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position < comments.count()) {
            return comments.get(position);
        } else {
            return issue;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private View getCommentView(int position, View convertView) {
        LinearLayout result = (LinearLayout)convertView;

        if (result == null || result.getTag() instanceof Issue) {
            result = (LinearLayout)inflater.inflate(R.layout.list_item, null);
        }

        Comments.Comment comment = comments.get(position);
        result.setTag(comment);

        result.findViewById(R.id.issue_row).setVisibility(View.GONE);
        result.findViewById(R.id.user_row).setVisibility(View.GONE);

        TextView time = (TextView)result.findViewById(R.id.time);
        TextView content = (TextView)result.findViewById(R.id.content);

        time.setText(dateString(comment.getUpdatedOn()));
        content.setText(comment.getContent());

        time.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);

        ImageView icon = (ImageView)result.findViewById(R.id.icon);
        Drawable drawable = BackLogCache.sharedInstance().iconOf(comment.getCreatedUser().getId());
        if (drawable == null) {
            icon.setImageResource(R.drawable.ic_dummy);
        } else {
            icon.setImageDrawable(drawable);
        }

        return result;
    }

    private View getIssueView(View convertView) {
        LinearLayout result = (LinearLayout)convertView;

        if (result == null || result.getTag() instanceof Comments.Comment) {
            result = (LinearLayout)inflater.inflate(R.layout.issue_item, null);
        }

        result.setTag(issue);

        TextView time = (TextView)result.findViewById(R.id.time);
        TextView user = (TextView)result.findViewById(R.id.user);
        TextView type = (TextView)result.findViewById(R.id.type);
        TextView priority = (TextView)result.findViewById(R.id.priority);
        TextView components = (TextView)result.findViewById(R.id.components);
        TextView status = (TextView)result.findViewById(R.id.status);
        TextView assigner = (TextView)result.findViewById(R.id.assigner);
        TextView description = (TextView)result.findViewById(R.id.description);

        time.setText(dateString(issue.getCreatedOn()));
        user.setText(issue.getCreatedUser().getName());
        type.setText(issue.getIssueType().getName());
        priority.setText(issue.getPriority().getName());
        components.setText(issue.getComponents().getName());
        status.setText(issue.getStatus().getName());
        assigner.setText(issue.getAssigner().getName());
        description.setText(issue.getDescription());

        ImageView icon = (ImageView)result.findViewById(R.id.icon);
        Drawable drawable = BackLogCache.sharedInstance().iconOf(issue.getCreatedUser().getId());
        if (drawable == null) {
            icon.setImageResource(R.drawable.ic_dummy);
        } else {
            icon.setImageDrawable(drawable);
        }

        return result;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position < comments.count()) {
            return getCommentView(position, convertView);
        } else {
            return getIssueView(convertView);
        }
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
