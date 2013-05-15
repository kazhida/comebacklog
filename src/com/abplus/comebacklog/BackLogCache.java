package com.abplus.comebacklog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import com.abplus.comebacklog.parsers.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/08 11:47
 */
public class BackLogCache {
    private TimeLine timeLine = null;
    private Comments comments = null;
    private Issue issue = null;
    private User user = new User();
    private Map<Integer, Drawable> icons = new HashMap<Integer, Drawable>();
    private BacklogIO backlogIO;
    private LayoutInflater inflater;
    private Context context;
    private Handler handler = new Handler();

    public interface OnIssueClickListener {
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

    public interface CacheResponseNotify extends BacklogIO.ResponseNotify {
        void success(BaseAdapter adapter);
        void success(Drawable icon);
    }

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

    public String password() {
        return backlogIO.getPassword();
    }

    /**
     * @return  ユーザIDプロパティ(数値)
     */
    public int userIdAsInt() {
        return user.getId();
    }

    public Drawable iconOf(int userId) {
        return icons.get(userId);
    }

    /**
     * 通信に使うオブジェクトの取得
     * @return  初期化に使ったBacklogIOインスタンス
     */
    public BacklogIO getIO() {
        return backlogIO;
    }

    public TimeLine getTimeLine() {
        return timeLine;
    }

    public Comments getComments() {
        return comments;
    }

    public Issue getIssue() {
        return issue;
    }

    private abstract class Responder implements BacklogIO.ResponseNotify {
        CacheResponseNotify notify;

        Responder(CacheResponseNotify notify) {
            this.notify = notify;
        }

        @Override
        public void failed(int code, String response) {
            notify.failed(code, response);
        }

        @Override
        public void error(Exception e) {
            notify.error(e);
        }
    }

    /**
     * タイムラインの取得
     * @param notify    終了通知インターフェース
     */
    public void getTimeLine(CacheResponseNotify notify) {
        timeLine = new TimeLine();

        backlogIO.getTimeLine(new Responder(notify) {
            @Override
            public void success(int code, final String response) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            timeLine.parse(response);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    notify.success(new TimeLineAdapter(context, inflater, timeLine));
                                }
                            });
                        } catch (final IOException e) {
                            notify.error(e);
                        } catch (final XmlPullParserException e) {
                            notify.error(e);
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * 課題情報の取得
     * そのまま、コメントの取得も行う
     * @param issueId   課題ID
     * @param notify    終了通知インターフェース
     */
    public void getIssue(final int issueId, CacheResponseNotify notify) {
        issue = new Issue();
        backlogIO.getIssue(issueId, new Responder(notify) {
            @Override
            public void success(int code, final String response) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
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
                }).start();
            }
        });
    }

    /**
     * コメントの取得
     * @param issueId   課題ID
     * @param notify    終了通知インターフェース
     */
    private void getComments(int issueId, final CacheResponseNotify notify) {
        comments = new Comments();
        backlogIO.getComments(issueId, new Responder(notify) {
            @Override
            public void success(int code, final String response) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            comments.parse(response);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    notify.success(new CommentsAdapter(inflater, comments, issue));
                                }
                            });
                        } catch (final IOException e) {
                            notify.error(e);
                        } catch (final XmlPullParserException e) {
                            notify.error(e);
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * ユーザ情報の取得
     * @param userId    ユーザID
     * @param notify    終了通知インターフェース
     */
    public void getUser(String userId, CacheResponseNotify notify) {

        backlogIO.getUser(userId, new Responder(notify) {
            @Override
            public void success(final int code, final String response) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            user.parse(response);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    notify.success(code, response);
                                }
                            });
                        } catch (IOException e) {
                            notify.error(e);
                        } catch (XmlPullParserException e) {
                            notify.error(e);
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * ユーザアイコンの取得
     * @param userId    ユーザID
     * @param notify    終了通知インターフェース
     */
    public void getUserIcon(int userId, CacheResponseNotify notify) {
        Drawable drawable = icons.get(userId);

        if (drawable != null) {
            notify.success(drawable);
        } else {
            backlogIO.getUserIcon(userId, new Responder(notify) {
                @Override
                public void success(int code, final String response) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final UserIcon icon = new UserIcon();
                            try {
                                icon.parse(response);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //  Bitmapを作って、キャッシュに入れる
                                        notify.success(putIcon(icon.getId(), icon));
                                    }
                                });
                            } catch (final IOException e) {
                                notify.error(e);
                            } catch (final XmlPullParserException e) {
                                notify.error(e);
                            }
                        }
                    }).start();
                }
            });
        }
    }

    private Drawable createIcon(Bitmap bitmap) {
        if (bitmap == null) {
            return context.getResources().getDrawable(R.drawable.ic_dummy);
        } else {
            BitmapDrawable result = new BitmapDrawable(context.getResources(), bitmap);
            result.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            return result;
        }
    }

    private Drawable putIcon(int userId, UserIcon userIcon) {
        Drawable drawable = icons.get(userId);

        if (drawable == null) {
            Log.d("userIcon", userIcon.getContentType());

            byte[] data = Base64.decode(userIcon.getData(), Base64.DEFAULT);
            InputStream stream = new ByteArrayInputStream(data);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            Log.d("userIcon", "w=" + bitmap.getWidth() + " h=" + bitmap.getHeight());

            drawable = createIcon(bitmap);
            icons.put(userId, drawable);
        }

        return drawable;
    }

    public void loadIcons(final SortedSet<Integer> userIds, final Runnable notify) {
        if (userIds.isEmpty()) return;

        final Integer userId = userIds.first();
        userIds.remove(userId);

        getUserIcon(userId, new CacheResponseNotify() {
            @Override
            public void success(BaseAdapter adapter) {
                loadIcons(userIds, notify);
            }

            @Override
            public void success(Drawable icon) {
                handler.post(notify);
                loadIcons(userIds, notify);
            }

            @Override
            public void success(int code, String response) {
                loadIcons(userIds, notify);
            }

            @Override
            public void failed(int code, String response) {
                loadIcons(userIds, notify);
            }

            @Override
            public void error(Exception e) {
                loadIcons(userIds, notify);
            }
        });
    }
}
