package com.abplus.comebacklog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.abplus.comebacklog.caches.TimeLine;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setTheme(R.style.app_theme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ListView list = (ListView)findViewById(R.id.time_line_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TimeLine.Item item = (TimeLine.Item)view.getTag();
                if (item != null) {
                    showComments(item.getIssue());
                }
            }
        });
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
        } else if(spaceId.equals(cache.spaceId()) && userId.equals(cache.userId()) && cache.getTimeLineAdapter() != null) {
            return true;
        } else {
            BackLogCache.initSharedInstance(this, new BacklogIO(spaceId, userId, password));
            return false;
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

    private void showTimeLine(boolean keep) {
        final ListView list = (ListView)findViewById(R.id.time_line_list);
        final BackLogCache cache = BackLogCache.sharedInstance();

        if (keep) {
            list.setAdapter(cache.getTimeLineAdapter());
        } else {
            final ProgressDialog waitDialog = showWait(getString(R.string.loading));
            cache.getTimeLine(new BacklogIO.ResponseNotify() {
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
        }
    }

    private void showPreferences() {
        Intent intent = new Intent(this, PrefsActivity.class);

        intent.setAction(Intent.ACTION_VIEW);

        startActivity(intent);
    }

    private void showComments(TimeLine.Issue issue) {
        Intent intent = new Intent(this, CommentsActivity.class);

        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(getString(R.string.key_issue_id), issue.getId());
        intent.putExtra(getString(R.string.key_issue_key), issue.getKey());
        intent.putExtra(getString(R.string.key_issue_summary), issue.getSummary());

        startActivity(intent);
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
                showTimeLine(false);
                return true;
            case R.id.menu_post:
                return true;
        }
        return false;
    }
}
