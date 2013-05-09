package com.abplus.comebacklog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.abplus.comebacklog.caches.StructParser;
import com.abplus.comebacklog.caches.TimeLine;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/08 11:47
 */
public class BackLogCache {
    Handler handler = new Handler();
    TimeLine timeLine = null;

    interface OnIssueClickListener {
        void onClick(View v, String key);
    }

    public interface ParserBuilder {
        StructParser getParser();
    }

    public interface Parseable {
        void parse(XmlPullParser xpp) throws IOException, XmlPullParserException;
    }

    public interface RootParseable {
        void parse(String response) throws IOException, XmlPullParserException;
    }

    private BacklogIO backlogIO;
    private LayoutInflater inflater;
    private Context context;

    private BackLogCache(Activity activity, BacklogIO io) {
        inflater = activity.getLayoutInflater();
        context = activity;
        backlogIO = io;
    }

    static BackLogCache cache = null;

    static public BackLogCache initSharedInstance(Activity activity, BacklogIO io) {
        cache = new BackLogCache(activity, io);
        return cache;
    }

    static public BackLogCache sharedInstance() {
        return cache;
    }

    public String spaceId() {
        return backlogIO.getSpaceId();
    }

    public String userId() {
        return backlogIO.getUserId();
    }

    public void loadTimeLine(final BacklogIO.ResponseNotify notify) {
        backlogIO.loadTimeLine(new BacklogIO.ResponseNotify() {
            @Override
            public void success(final int code, final String response) {
                timeLine = new TimeLine();
                try {
                    timeLine.parse(response);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notify.success(code, response);
                        }
                    });
                } catch (final IOException e) {
                    timeLine = null;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notify.error(e);
                        }
                    });
                } catch (final XmlPullParserException e) {
                    timeLine = null;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notify.error(e);
                        }
                    });
                }
            }

            @Override
            public void failed(final int code, final String response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notify.failed(code, response);
                    }
                });
            }

            @Override
            public void error(final Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notify.error(e);
                    }
                });
            }
        });
    }

    private class TimeLineAdapter extends BaseAdapter {

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
                result = (LinearLayout)inflater.inflate(R.layout.time_line_item, null);
            }

            TimeLine.Item item = timeLine.get(position);

            TextView time = (TextView)result.findViewById(R.id.time);
            TextView user = (TextView)result.findViewById(R.id.user);
            TextView type = (TextView)result.findViewById(R.id.type);
            TextView key = (TextView)result.findViewById(R.id.key);
            TextView summary = (TextView)result.findViewById(R.id.summary);
            TextView content = (TextView)result.findViewById(R.id.content);

            time.setText(item.getUpdatedOn());
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

            return result;
        }
    }

    public BaseAdapter getTimeLineAdapter() {
        if (timeLine == null) {
            return null;
        } else {
            return new TimeLineAdapter();
        }
    }

    public BaseAdapter getCommentsAdapter() {
        //todo:後でちゃんとやる
        return null;
    }
}
