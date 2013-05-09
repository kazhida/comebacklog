package com.abplus.comebacklog.caches;

import android.util.Log;
import com.abplus.comebacklog.BackLogCache;
import com.abplus.comebacklog.BacklogIO;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/09 8:07
 */
public class TimeLine implements BackLogCache.RootParseable {
    List<Item> items = new ArrayList<Item>();

    @Override
    public void parse(String response) throws IOException, XmlPullParserException {
        new ItemsParser().parse(response);
    }

    public class Item {
        IdNamePair type = new IdNamePair();
        String content;
        String updated_on;
        IdNamePair user = new IdNamePair();
        Issue issue = new Issue();
    }

    public class Issue implements BacklogIO.IdHolder, BacklogIO.KeyHolder, BackLogCache.Parseable {
        //  TimeLine用の簡易版Issue
        private int id;
        private String key;
        private String summary;
        private String description;
        private IdNamePair priority = new IdNamePair();

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getKey() {
            return key;
        }

        public String getSummary() {
            return summary;
        }

        public IdNamePair getPriority() {
            return priority;
        }

        @Override
        public void parse(XmlPullParser xpp) throws IOException, XmlPullParserException {
            new IssueParser(this).parseStruct(xpp);
        }
    }

    private class ItemsParser extends StructParser {
        Item item = null;

        @Override
        public void parseStruct(XmlPullParser xpp) throws IOException, XmlPullParserException {
            item = new Item();
            super.parseStruct(xpp);
            items.add(item);
            item = null;
        }

        @Override
        protected void parseValueStartTag(String name, XmlPullParser xpp) throws IOException, XmlPullParserException {
            if (item != null && xpp.getName().equals("struct")) {
                if (name.equals("type")) {
                    item.type.parse(xpp);
                } else if (name.equals("user")) {
                    item.user.parse(xpp);
                } else if (name.equals("issue")) {
                    item.issue.parse(xpp);
                }
            }
        }

        @Override
        protected void parseValueEndTag(String name, XmlPullParser xpp) throws IOException, XmlPullParserException {
            //なにもしない
        }

        @Override
        protected void parseValueText(String name, XmlPullParser xpp) throws IOException, XmlPullParserException {
            if (item != null) {
                if (name.equals("content")) {
                    item.content = xpp.getText();
                } else if (name.equals("name")) {
                    item.updated_on = xpp.getText();
                }
            }
        }
    }

    private class IssueParser extends StructParser {
        Issue issue;

        IssueParser(Issue issue) {
            super();
            this.issue = issue;
        }

        @Override
        protected void parseValueStartTag(String name, XmlPullParser xpp) throws IOException, XmlPullParserException {
            if (xpp.getName().equals("struct")) {
                if (name.equals("priority")) issue.priority.parse(xpp);
            }
        }

        @Override
        protected void parseValueEndTag(String name, XmlPullParser xpp) throws IOException, XmlPullParserException {
            //なにもしない
        }

        @Override
        protected void parseValueText(String name, XmlPullParser xpp) throws IOException, XmlPullParserException {
            if (name.equals("id")) {
                issue.id = Integer.parseInt(xpp.getText());
            } else if (name.equals("key")) {
                issue.key = xpp.getText();
            } else if (name.equals("summary")) {
                issue.summary = xpp.getText();
            } else if (name.equals("description")) {
                issue.description = xpp.getText();
            }
        }
    }
}

