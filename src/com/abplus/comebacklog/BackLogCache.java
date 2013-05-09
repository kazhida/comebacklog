package com.abplus.comebacklog;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import com.abplus.comebacklog.caches.StructParser;
import com.abplus.comebacklog.caches.TimeLine;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Time;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/08 11:47
 */
public class BackLogCache {
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
            public void success(int code, String response) {

            }

            @Override
            public void failed(int code, String response) {
                notify.failed(code, response);
            }

            @Override
            public void error(Exception e) {
                notify.error(e);
            }
        });
    }

    public BaseAdapter getTimeLineAdapter() {
        //todo:後でちゃんとやる
        return null;
    }

    public BaseAdapter getCommentsAdapter() {
        //todo:後でちゃんとやる
        return null;
    }
}
