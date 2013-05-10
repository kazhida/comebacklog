package com.abplus.comebacklog.parsers;

import android.util.Log;
import android.util.Xml;
import com.abplus.comebacklog.BacklogIO;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/09 8:23
 */
public abstract class StructParser {

    public void parse(String source) throws XmlPullParserException, IOException {
        XmlPullParser xpp = Xml.newPullParser();
        xpp.setInput(new StringReader(source));
        parse(xpp);
    }

    public void parse(XmlPullParser xpp) throws XmlPullParserException, IOException {
        for (int et = xpp.getEventType(); et != XmlPullParser.END_DOCUMENT; et = xpp.next()) {
            if (et == XmlPullParser.START_DOCUMENT) {
                //  ドキュメントの開始
                Log.d(BacklogIO.DEBUG_TAG, "Document start.");
            } else if (et == XmlPullParser.START_TAG) {
                //  開始タグ
                Log.d(BacklogIO.DEBUG_TAG, "Start tag " + xpp.getName());
                if (xpp.getName().equals("struct")) {
                    parseStruct(xpp);
                }
            } else if (et == XmlPullParser.END_TAG) {
                //  終了タグ
                Log.d(BacklogIO.DEBUG_TAG, "End tag " + xpp.getName());
            } else if (et == XmlPullParser.TEXT) {
                //  タグに挟まれたテキスト
                Log.d(BacklogIO.DEBUG_TAG, "Text " + xpp.getText());
            }
        }
        Log.d(BacklogIO.DEBUG_TAG, "Document end.");
    }

    public void parseStruct(XmlPullParser xpp) throws IOException, XmlPullParserException {

        for (int et = xpp.next(); et != XmlPullParser.END_DOCUMENT; et = xpp.next()) {
            if (et == XmlPullParser.START_TAG) {
                //  開始タグ
                Log.d(BacklogIO.DEBUG_TAG + ".parseStruct", "Start tag " + xpp.getName());
                if (xpp.getName().equals("member")) {
                    parseMember(xpp);
                }
            } else if (et == XmlPullParser.END_TAG) {
                //  終了タグ
                Log.d(BacklogIO.DEBUG_TAG + ".parseStruct", "End tag " + xpp.getName());
                if (xpp.getName().equals("struct")) break;
            } else if (et == XmlPullParser.TEXT) {
                //  タグに挟まれたテキスト
                Log.d(BacklogIO.DEBUG_TAG + ".parseStruct", "Text " + xpp.getText());
            }
        }
    }

    protected void parseMember(XmlPullParser xpp) throws IOException, XmlPullParserException {
        String tag = "";
        String name = null;

        //  名前、値の順に並んでいることを想定している

        for (int et = xpp.next(); et != XmlPullParser.END_DOCUMENT; et = xpp.next()) {
            if (et == XmlPullParser.START_TAG) {
                Log.d(BacklogIO.DEBUG_TAG + ".parseMember", "Start tag " + xpp.getName());
                //  開始タグ
                tag = xpp.getName();
                if (tag.equals("value")) {
                    //  値をパース
                    parseValue(name, xpp);
                }
            } else if (et == XmlPullParser.END_TAG) {
                //  終了タグ
                Log.d(BacklogIO.DEBUG_TAG + ".parseMember", "End tag " + xpp.getName());
                if (xpp.getName().equals("member")) break;
            } else if (et == XmlPullParser.TEXT) {
                //  タグに挟まれたテキスト
                Log.d(BacklogIO.DEBUG_TAG + ".parseMember", "Text " + xpp.getText());
                if (tag.equals("name")) {
                    //  名前を保持
                    name = xpp.getText();
                }
            }
        }
    }

    protected void parseValue(String name, XmlPullParser xpp) throws IOException, XmlPullParserException {
        for (int et = xpp.next(); et != XmlPullParser.END_DOCUMENT; et = xpp.next()) {
            if (et == XmlPullParser.START_TAG) {
                //  開始タグ
                Log.d(BacklogIO.DEBUG_TAG + ".parseValue", "Start tag " + xpp.getName());
                if (xpp.getName().equals("array")) {
                    parseArray(name, xpp);
                } else {
                    parseValueStartTag(name, xpp);
                }
            } else if (et == XmlPullParser.END_TAG) {
                //  終了タグ
                Log.d(BacklogIO.DEBUG_TAG + ".parseValue", "End tag " + xpp.getName());
                parseValueEndTag(name, xpp);
                if (xpp.getName().equals("value")) break;
            } else if (et == XmlPullParser.TEXT) {
                //  タグに挟まれたテキスト
                Log.d(BacklogIO.DEBUG_TAG + ".parseValue", "Text " + xpp.getText());
                parseValueText(name, xpp);
            }
        }
    }

    abstract protected void parseValueStartTag(String name, XmlPullParser xpp) throws IOException, XmlPullParserException;
    abstract protected void parseValueText(String name, XmlPullParser xpp) throws IOException, XmlPullParserException;
    abstract protected void parseValueEndTag(String name, XmlPullParser xpp) throws IOException, XmlPullParserException;

    protected void parseArray(String name, XmlPullParser xpp) throws IOException, XmlPullParserException {

        for (int et = xpp.next(); et != XmlPullParser.END_DOCUMENT; et = xpp.next()) {
            if (et == XmlPullParser.START_TAG) {
                //  開始タグ
                Log.d(BacklogIO.DEBUG_TAG + ".parseArray", "Start tag " + xpp.getName());
                if (xpp.getName().equals("value")) {
                    parseValue(name, xpp);
                }
            } else if (et == XmlPullParser.END_TAG) {
                //  終了タグ
                Log.d(BacklogIO.DEBUG_TAG + ".parseArray", "End tag " + xpp.getName());
                if (xpp.getName().equals("array")) break;
            } else if (et == XmlPullParser.TEXT) {
                //  タグに挟まれたテキスト
                Log.d(BacklogIO.DEBUG_TAG + ".parseArray", "Text " + xpp.getText());
            }
        }
    }
}
