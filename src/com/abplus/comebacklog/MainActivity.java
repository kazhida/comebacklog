package com.abplus.comebacklog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setTheme(R.style.app_theme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String spaceId = prefs.getString(getString(R.string.key_space_id), "");
        String userId = prefs.getString(getString(R.string.key_user_id), "");
        String password = prefs.getString(getString(R.string.key_password), "");

        if (spaceId.length() == 0 || userId.length() == 0 || password.length() == 0) {
            //  未登録なら設定画面
            showPreferences();
        } else {
            showTimeLine(samePrefs(spaceId, userId, password));
        }
    }

    private boolean samePrefs(String spaceId, String userId, String password) {
        BackLogCache cache = BackLogCache.sharedInstance();

        if (cache == null) {
            BackLogCache.initSharedInstance(this, new BacklogIO(spaceId, userId, password));
            return false;
        } else {
            return spaceId.equals(cache.spaceId()) && userId.equals(cache.userId()) && cache.getTimeLineAdapter() != null;
        }
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

    private void showPreferences() {
        Intent intent=new Intent(this, PrefsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
    }

    private void showTimeLine(boolean reload) {
        final ListView list = (ListView)findViewById(R.id.list_view);
        final BackLogCache cache = BackLogCache.sharedInstance();

        if (reload) {
            final ProgressDialog waitDialog = showWait(getString(R.string.loading));
            cache.loadTimeLine(new BacklogIO.ResponseNotify() {
                @Override
                public void success(int code, String response) {
                    waitDialog.dismiss();
                    list.setAdapter(cache.getTimeLineAdapter());
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
        } else {
            list.setAdapter(cache.getTimeLineAdapter());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);

        menu.findItem(R.id.menu_post).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_config:
                showPreferences();
                return true;
            case R.id.menu_reload:
                showTimeLine(true);
                return true;
            case R.id.menu_post:
                return true;
        }
        return false;
    }
}
