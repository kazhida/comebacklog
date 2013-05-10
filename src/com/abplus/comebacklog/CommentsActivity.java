package com.abplus.comebacklog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/09 14:00
 */
public class CommentsActivity extends Activity {
    private int issueId;
    private String issueKey;
    private BaseAdapter adapter = null;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        if (issueId >= 0 && adapter == null) {
            showComments();
        }
    }

    private void showComments() {
        final ListView list = (ListView)findViewById(R.id.comments_list);
        final BackLogCache cache = BackLogCache.sharedInstance();
        final ProgressDialog waitDialog = showWait(getString(R.string.loading));

        adapter = null;
        list.setAdapter(adapter);

        cache.getIssue(issueId, new BacklogIO.ResponseNotify() {
            @Override
            public void success(int code, String response) {
                waitDialog.dismiss();
                adapter = cache.getCommentsAdapter();
                list.setAdapter(adapter);
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
}
