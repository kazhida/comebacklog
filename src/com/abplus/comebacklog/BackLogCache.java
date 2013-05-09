package com.abplus.comebacklog;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import com.abplus.comebacklog.caches.StructParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/08 11:47
 */
public class BackLogCache {

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
    private User currentUser;

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

    public int userIdAsInt() {
        return currentUser.id;
    }

    public void loadSummaries(final BacklogIO.ResponseNotify notify) {
        backlogIO.loadSummaries(new BacklogIO.ResponseNotify() {
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

    public class User {
        int     id;
        String  name;
        String  lang;
        String  updated_on;
    }

    public void loadUser(final BacklogIO.ResponseNotify notify) {

        backlogIO.loadUser(backlogIO.getUserId(), new BacklogIO.ResponseNotify() {

            @Override
            public void success(int code, String response) {
//                UserParser parser = new UserParser();
//                try {
//                    parser.parse(response);
//                    currentUser = parser.user;
//                    notify.success(code, response);
//                } catch (XmlPullParserException e) {
//                    notify.error(e);
//                } catch (IOException e) {
//                    notify.error(e);
//                }
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

    public BaseAdapter getSummariesAdapter() {
        //todo:後でちゃんとやる
        return null;
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
