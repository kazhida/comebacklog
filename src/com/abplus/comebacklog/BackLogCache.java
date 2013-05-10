package com.abplus.comebacklog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.abplus.comebacklog.parsers.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    Issue issue = null;
    Map<Integer, Bitmap> icons = new HashMap<Integer, Bitmap>();

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

    /**
     * 共有インスタンスの初期化
     *
     * @param activity  メインアクティビティ
     * @param io        通信に使うオブジェクト
     * @return  共有インスタンス
     */
    static public BackLogCache initSharedInstance(Activity activity, BacklogIO io) {
        cache = new BackLogCache(activity, io);
        return cache;
    }

    /**
     * 共有インスタンスの取得
     * @return  共有インスタンス
     */
    static public BackLogCache sharedInstance() {
        return cache;
    }

    /**
     * @return  スペースIDプロパティ
     */
    public String spaceId() {
        return backlogIO.getSpaceId();
    }

    /**
     * @return  ユーザIDプロパティ
     */
    public String userId() {
        return backlogIO.getUserId();
    }

    /**
     * 通信に使うオブジェクトの取得
     * @return  初期化に使ったBacklogIOインスタンス
     */
    public BacklogIO getIO() {
        return backlogIO;
    }

    /**
     * 日付を整形するユーティリティ
     * @param date  yyyymmddhhnnss形式の日付
     * @return      mm/dd hh:nn形式の日付
     */
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

    /**
     * タイムラインの取得
     * @param notify    終了通知インターフェース
     */
    public void getTimeLine(final BacklogIO.ResponseNotify notify) {
        timeLine = null;

        backlogIO.getTimeLine(new BacklogIO.ResponseNotify() {
            @Override
            public void success(int code, String response) {
                timeLine = new TimeLine();
                try {
                    timeLine.parse(response);
                    notify.success(code, response);
                } catch (final IOException e) {
                    notify.error(e);
                } catch (final XmlPullParserException e) {
                    notify.error(e);
                }
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

    /**
     * タイムラインのリスト表示用アダプタ
     */
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

    /**
     * タイムライン用アダプタの取得
     * @return  タイムライン用アダプタ
     */
    public BaseAdapter getTimeLineAdapter() {
        if (timeLine == null) {
            return null;
        } else {
            return new TimeLineAdapter();
        }
    }

    /**
     * 課題情報の取得
     * そのまま、コメントの取得も行う
     * @param issueId   課題ID
     * @param notify    終了通知インターフェース
     */
    public void getIssue(final int issueId, final BacklogIO.ResponseNotify notify) {
        issue = null;

        backlogIO.getIssue(issueId, new BacklogIO.ResponseNotify() {
            @Override
            public void success(int code, String response) {
                issue = new Issue();
                try {
                    issue.parse(response);
                    Log.d(BacklogIO.DEBUG_TAG, response);
                    //  このままcommentsもとってくる
                    getComments(issueId, notify);
                } catch (final IOException e) {
                    notify.error(e);
                } catch (final XmlPullParserException e) {
                    notify.error(e);
                }
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

    /**
     * コメントの取得
     * @param issueId   課題ID
     * @param notify    終了通知インターフェース
     */
    private void getComments(int issueId, final BacklogIO.ResponseNotify notify) {
        comments = null;

        backlogIO.getComments(issueId, new BacklogIO.ResponseNotify() {
            @Override
            public void success(int code, String response) {
                comments = new Comments();
                try {
                    comments.parse(response);
                    notify.success(code, response);
                } catch (final IOException e) {
                    notify.error(e);
                } catch (final XmlPullParserException e) {
                    notify.error(e);
                }
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

    /**
     * コメントのリスト表示用アダプタ
     * 末尾には課題情報が含まれる
     */
    private class CommentsAdapter extends BaseAdapter {

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
    }

    /**
     * コメント用アダプタの取得
     * @return  コメント用アダプタ
     */
    public BaseAdapter getCommentsAdapter() {
        if (comments == null) {
            return null;
        } else {
            return new CommentsAdapter();
        }
    }

    public void getUserIcon(int userId, final BacklogIO.ResponseNotify notify) {

        backlogIO.getUserIcon(userId, new BacklogIO.ResponseNotify() {

            @Override
            public void success(int code, String response) {
                UserIcon icon = new UserIcon();
                try {
                    icon.parse(response);
                    //  Bitmapを作って、キャッシュに入れる

                    notify.success(code, response);
                } catch (final IOException e) {
                    notify.error(e);
                } catch (final XmlPullParserException e) {
                    notify.error(e);
                }
                notify.success(code, response);
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
}
