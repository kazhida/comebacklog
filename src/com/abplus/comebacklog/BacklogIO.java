package com.abplus.comebacklog;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/04/06 13:52
 */
public class BacklogIO {
    private String space_id;
    private String user_id;
    private String password;
    private Handler handler = new Handler();

    public static final String DEBUG_TAG = "*backlog.io";

    interface ResponseNotify {
        void success(int code, String response);
        void failed(int code, String response);
        void error(Exception e);
    }

    public interface IdHolder {
        int getId();
    }

    public interface KeyHolder {
        String getKey();
    }

    public interface NameHolder {
        String getName();
    }


    BacklogIO(String space_id, String user_id, String password) {
        this.space_id = space_id;
        this.user_id = user_id;
        this.password = password;
    }

    public String getSpaceId() {
        return space_id;
    }

    public String getUserId() {
        return user_id;
    }

    public String getPassword() {
        return password;
    }

    public void post(final String request, final ResponseNotify notify) {
        final HttpPost httpPost = new HttpPost("https://" + space_id + ".backlog.jp/XML-RPC");
        final DefaultHttpClient http = new DefaultHttpClient();

        httpPost.addHeader("Content-Type", "text/xml");

        http.getCredentialsProvider().setCredentials(
                new AuthScope(httpPost.getURI().getHost(), httpPost.getURI().getPort()),
                new UsernamePasswordCredentials(user_id, password)
        );

        Log.d(DEBUG_TAG + ".request", request);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);   //ProgressDialogのために、ちょっとだけスリープ
                    httpPost.setEntity(new StringEntity(request, HTTP.UTF_8));
                    HttpResponse response = http.execute(httpPost);

                    for (Header header : response.getAllHeaders()) {
                        Log.d(DEBUG_TAG + ".response_header", header.toString());
                    }

                    final int code = response.getStatusLine().getStatusCode();
                    final String entity = EntityUtils.toString(response.getEntity());

                    if (200 <= code && code < 400) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                notify.success(code, entity);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                notify.failed(code, entity);
                            }
                        });
                    }
                } catch (Exception e) {
                    final Exception exception = e;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notify.error(exception);
                        }
                    });
                }
            }
        }).start();
    }

    public void getTimeLine(ResponseNotify notify) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xml.append("<methodCall>");
        xml.append("<methodName>backlog.getTimeline</methodName>");
        xml.append("<params />");
        xml.append("</methodCall>");

        post(xml.toString(), notify);
    }

    public void getComments(int issueId, ResponseNotify notify) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xml.append("<methodCall>");
        xml.append("<methodName>backlog.getComments</methodName>");
        xml.append("<params>");
        xml.append("<param>");
        xml.append("<value>");
        xml.append("<int>");
        xml.append(issueId);
        xml.append("</int>");
        xml.append("</value>");
        xml.append("</param>");
        xml.append("</params>");
        xml.append("</methodCall>");

        post(xml.toString(), notify);
    }

    public void getIssue(int issueId, ResponseNotify notify) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xml.append("<methodCall>");
        xml.append("<methodName>backlog.getIssue</methodName>");
        xml.append("<params>");
        xml.append("<param>");
        xml.append("<value>");
        xml.append("<int>");
        xml.append(issueId);
        xml.append("</int>");
        xml.append("</value>");
        xml.append("</param>");
        xml.append("</params>");
        xml.append("</methodCall>");

        post(xml.toString(), notify);
    }

    public void getUser(String userId, ResponseNotify notify) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xml.append("<methodCall>");
        xml.append("<methodName>backlog.getUser</methodName>");
        xml.append("<params>");
        xml.append("<param>");
        xml.append("<value>");
        xml.append("<string>");
        xml.append(userId);
        xml.append("</string>");
        xml.append("</value>");
        xml.append("</param>");
        xml.append("</params>");
        xml.append("</methodCall>");

        post(xml.toString(), notify);
    }

    public void addComment(String key, String content, ResponseNotify notify) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xml.append("<methodCall>");
        xml.append("<methodName>backlog.addComment</methodName>");
        xml.append("<params>");
        xml.append("<param>");
        xml.append("<value>");
        xml.append("<struct>");

        xml.append("<member>");
        xml.append("<name>key</name>");
        xml.append("<value><string>").append(TextUtils.htmlEncode(key)).append("</string></value>");
        xml.append("</member>");

        xml.append("<member>");
        xml.append("<name>content</name>");
        xml.append("<value><string>").append(TextUtils.htmlEncode(content)).append("</string></value>");
        xml.append("</member>");

        xml.append("</struct>");
        xml.append("</value>");
        xml.append("</param>");
        xml.append("</params>");
        xml.append("</methodCall>");

        post(xml.toString(), notify);
    }
}
