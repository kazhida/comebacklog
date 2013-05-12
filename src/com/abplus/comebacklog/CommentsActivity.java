package com.abplus.comebacklog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.abplus.comebacklog.parsers.Comments;
import com.abplus.comebacklog.parsers.Issue;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/09 14:00
 */
public class CommentsActivity extends Activity {
    private int issueId;
    private String issueKey;
    private BaseAdapter commentsAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setTheme(R.style.app_theme);
        }
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.comments);

        Intent intent = getIntent();
        issueId = intent.getIntExtra(getString(R.string.key_issue_id), -1);
        issueKey = intent.getStringExtra(getString(R.string.key_issue_key));

        TextView key = (TextView)findViewById(R.id.key);
        TextView summary = (TextView)findViewById(R.id.summary);

        key.setText(issueKey);
        summary.setText(intent.getStringExtra(getString(R.string.key_issue_summary)));

        loadMyIcon();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (issueId >= 0 && commentsAdapter == null) {
            showComments();
        }
    }

    private void showComments() {
        final ListView list = (ListView)findViewById(R.id.comments_list);
        final BackLogCache cache = BackLogCache.sharedInstance();
        final ProgressDialog waitDialog = showWait(getString(R.string.loading));

        commentsAdapter = null;
        list.setAdapter(commentsAdapter);

        cache.getIssue(issueId, new BackLogCache.CacheResponseNotify() {
            @Override
            public void success(int code, String response) {
                waitDialog.dismiss();
            }

            @Override
            public void failed(int code, String response) {
                waitDialog.dismiss();
                showError(R.string.cant_load, "ERROR STATUS = " + code);
            }

            @Override
            public void error(Exception e) {
                waitDialog.dismiss();
                showError(R.string.cant_load, "Error: " + e.getLocalizedMessage());
            }

            @Override
            public void success(BaseAdapter adapter) {
                waitDialog.dismiss();
                commentsAdapter = adapter;
                list.setAdapter(commentsAdapter);
                loadIcons();
            }

            @Override
            public void success(Drawable icon) {
                waitDialog.dismiss();
                if (commentsAdapter != null) commentsAdapter.notifyDataSetChanged();
            }
        });
    }

    private ProgressDialog showWait(String msg) {
        ProgressDialog result = new ProgressDialog(this);
        result.setMessage(msg);
        result.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        result.show();
        return result;
    }

    private void showError(int msg_id, String msg) {
        Toast.makeText(this, getString(msg_id) + "  " + msg, Toast.LENGTH_LONG).show();
    }

    private void showMessage(int msg_id) {
        Toast.makeText(this, getString(msg_id), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comments, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_reload:
                showComments();
                return true;
            case R.id.menu_post:
                postComment();
                return true;
        }
        return false;
    }

    private void postComment() {
        BacklogIO io = BackLogCache.sharedInstance().getIO();

        final EditText edit = (EditText)findViewById(R.id.comment);
        String content = edit.getText().toString();

        if (content.isEmpty()) {
            showComments();
        } else {
            final ProgressDialog waitDialog = showWait(getString(R.string.sending));

            io.addComment(issueKey, content, new BacklogIO.ResponseNotify() {
                @Override
                public void success(int code, String response) {
                    waitDialog.dismiss();
                    showMessage(R.string.done_send);
                    //  うまくいったからクリア
                    edit.setText(null);
                    //  キーボードも隠す
                    if (edit.hasFocus()) {
                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
                    //  読込直し
                    showComments();
                }

                @Override
                public void failed(int code, String response) {
                    waitDialog.dismiss();
                    showError(R.string.cant_send, "ERROR STATUS = " + code);
                }

                @Override
                public void error(Exception e) {
                    waitDialog.dismiss();
                    showError(R.string.cant_send, "Error: " + e.getLocalizedMessage());
                }
            });
        }
    }

    private void loadMyIcon() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = prefs.getString(getString(R.string.key_user_id), "");
        final BackLogCache cache = BackLogCache.sharedInstance();

        if (! userId.isEmpty()) {
            //  まず、ユーザ情報を読み込む
            cache.getUser(userId, new BackLogCache.CacheResponseNotify() {

                @Override
                public void success(int code, String response) {
                    //  うまくいったら、アイコンを取り出す
                    cache.getUserIcon(cache.userIdAsInt(), new BackLogCache.CacheResponseNotify() {
                        @Override
                        public void success(BaseAdapter adapter) {
                            //  なにもしない
                        }

                        @Override
                        public void success(Drawable icon) {
                            ImageView view = (ImageView)findViewById(R.id.icon);
                            view.setImageDrawable(icon);
                        }

                        @Override
                        public void success(int code, String response) {
                            //  なにもしない
                        }

                        @Override
                        public void failed(int code, String response) {
                            showError(R.string.cant_load, "ERROR STATUS = " + code);
                        }

                        @Override
                        public void error(Exception e) {
                            showError(R.string.cant_load, e.getLocalizedMessage());
                        }
                    });
                }

                @Override
                public void failed(int code, String response) {
                    showError(R.string.cant_load, "ERROR STATUS = " + code);
                }

                @Override
                public void error(Exception e) {
                    showError(R.string.cant_load, e.getLocalizedMessage());
                }

                @Override
                public void success(BaseAdapter adapter) {
                    //  なにもしない
                }

                @Override
                public void success(Drawable icon) {
                    //  なにもしない
                }
            });
        }
    }

    private void loadIcons() {
        SortedSet<Integer> userIds = new TreeSet<Integer>();
        int n = commentsAdapter.getCount() - 1;
        for (int i = 0; i < n; i++) {
            Comments.Comment comment = (Comments.Comment)commentsAdapter.getItem(i);
            userIds.add(comment.getCreatedUser().getId());
        }
        Issue issue = (Issue)commentsAdapter.getItem(n);
        userIds.add(issue.getCreatedUser().getId());

        BackLogCache.sharedInstance().loadIcons(userIds, new Runnable() {
            @Override
            public void run() {
                commentsAdapter.notifyDataSetChanged();
            }
        });
    }
}
