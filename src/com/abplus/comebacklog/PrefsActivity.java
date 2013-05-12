package com.abplus.comebacklog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/08 12:09
 */
public class PrefsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs);

        findViewById(R.id.save_prefs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getPreferences();
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(getString(R.string.key_space_id), getEditText(R.id.space_id));
                editor.putString(getString(R.string.key_user_id),  getEditText(R.id.user_id));
                editor.putString(getString(R.string.key_password), getEditText(R.id.password));

                editor.commit();

                //  閉じる
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setEditText(R.id.space_id, prefs.getString(getString(R.string.key_space_id), ""));
        setEditText(R.id.user_id, prefs.getString(getString(R.string.key_user_id), ""));
        setEditText(R.id.password, prefs.getString(getString(R.string.key_password), ""));
    }

    private void setEditText(int id, String text) {
        EditText edit = (EditText)findViewById(id);
        edit.setText(text);
    }

    private String getEditText(int id) {
        EditText edit = (EditText)findViewById(id);
        return edit.getText().toString().trim();
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
