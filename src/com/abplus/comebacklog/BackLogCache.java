package com.abplus.comebacklog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.abplus.comebacklog.caches.Comments;
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
    Comments comments = null;

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

    private String dateString(String date) {
        if (date == null) {
            return null;
        } else {
//        String y = date.substring(0, 4);
            String m = date.substring(4, 6);
            String d = date.substring(6, 8);
            String h = date.substring(8, 10);
            String n = date.substring(10, 12);
//        String s = date.substring(12);

            return m + "/" + d + " " + h + ":" + n;
        }
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

    public void loadComments(int issueId, final BacklogIO.ResponseNotify notify) {

        backlogIO.loadComments(issueId, new BacklogIO.ResponseNotify() {
            @Override
            public void success(final int code, final String response) {
                comments = new Comments();
                try {
                    comments.parse(response);
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

    private class CommentsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return comments.count();
        }

        @Override
        public Object getItem(int position) {
            return comments.get(position);
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

            Comments.Comment comment = comments.get(position);
            result.setTag(comment);

            result.findViewById(R.id.issue_row).setVisibility(View.GONE);
            result.findViewById(R.id.user_row).setVisibility(View.GONE);

            TextView time = (TextView)result.findViewById(R.id.time);
            TextView content = (TextView)result.findViewById(R.id.content);

            time.setText(dateString(comment.getUpdatedOn()));
            content.setText(comment.getContent());

            time.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);

            return result;
        }
    }

    public BaseAdapter getCommentsAdapter() {
        if (timeLine == null) {
            return null;
        } else {
            return new CommentsAdapter();
        }
    }


}
