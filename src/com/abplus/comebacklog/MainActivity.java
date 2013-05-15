package com.abplus.comebacklog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.abplus.comebacklog.billing.BillingHelper;
import com.abplus.comebacklog.parsers.TimeLine;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends Activity {
    private BaseAdapter timeLineAdapter;
    private BillingHelper billingHelper;
    private boolean noPrefs = true;
    private AdView adView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        adView = appendAdView();

        ListView list = (ListView)findViewById(R.id.time_line_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TimeLine.Item item = (TimeLine.Item) view.getTag();
                if (item != null) {
                    showComments(item.getIssue());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String DEBUG_TAG = "*backlog.no_ad.billing";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String spaceId = prefs.getString(getString(R.string.key_space_id), "");
        final String userId = prefs.getString(getString(R.string.key_user_id), "");
        final String password = prefs.getString(getString(R.string.key_password), "");

        boolean checkInventory = false;
        noPrefs = false;

        if (spaceId.length() == 0 || userId.length() == 0 || password.length() == 0) {
            //  未登録なら設定画面
            noPrefs = true;
        } else {
            switch (prefs.getInt(getString(R.string.key_no_ad), 0)) {
                case 0:
                    //  確認
                    checkInventory = true;
                    Log.d(DEBUG_TAG, "check");
                    break;
                case 1:
                    //  adView非表示
                    hideAd();
                    Log.d(DEBUG_TAG, "no_ad");
                    break;
                default:
                    //  adView表示
                    showAd();
                    Log.d(DEBUG_TAG, "show_ad");
                    break;
            }
        }

        //  広告関連の処理
        final boolean needQuery = checkInventory;

        if (billingHelper == null) {
            billingHelper = new BillingHelper(this);
            Log.d(DEBUG_TAG, "Setup start.");
            billingHelper.startSetup(new BillingHelper.OnSetupFinishedListener() {
                @Override
                public void onSetupFinished(BillingHelper.Result result) {
                    Log.d(DEBUG_TAG, "Setup finished.");

                    if (result.isFailure()) {
                        Log.d(DEBUG_TAG, "Setup failed.");
                    } else if (needQuery) {
                        Log.d(DEBUG_TAG, "Setup successful. Querying inventory.");
                        try {
                            BillingHelper.Inventory inventory = billingHelper.queryInventory(false);
                            Log.d(DEBUG_TAG, "Query inventory was successful.");
                            boolean no_ad = inventory.hasPurchase(getString(R.string.sku_no_ad));
                            if (no_ad) {
                                billingHelper.savePurchaseForNoAd(1);
                                hideAd();
                            } else {
                                billingHelper.savePurchaseForNoAd(-1);
                                showAd();
                            }
                        } catch (BillingHelper.BillingException e) {
                            billingHelper.savePurchaseForNoAd(-1);
                            showAd();
                        }
                    }
                }
            });
        }
        showInit(spaceId, userId, password);
    }

    public void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
        if (billingHelper != null) {
            billingHelper.dispose();
            billingHelper = null;
        }
    }

    private boolean samePrefs(String spaceId, String userId, String password) {
        BackLogCache cache = BackLogCache.sharedInstance();

        if (cache == null) {
            BackLogCache.initSharedInstance(this, new BacklogIO(spaceId, userId, password));
            return false;
        } else if(spaceId.equals(cache.spaceId()) &&
                userId.equals(cache.userId()) &&
                password.equals(cache.password()) &&
                cache.getTimeLine() != null) {
            return true;
        } else {
            BackLogCache.initSharedInstance(this, new BacklogIO(spaceId, userId, password));
            return false;
        }
    }

    private void showInit(String spaceId, String userId, String password) {
        if (noPrefs) {
            showPreferences();
        } else {
            showTimeLine(samePrefs(spaceId, userId, password));
        }
    }

    private void showAd() {
        adView.setVisibility(View.VISIBLE);
        adView.loadAd(new AdRequest());
    }

    private void hideAd() {
        adView.stopLoading();
        adView.setVisibility(View.GONE);
    }

    /**
     * 広告ビューを作って、アクティビティに追加する
     */
    private AdView appendAdView() {
        AdView result = adView;

        if (result == null) {
            result = new AdView(this, AdSize.BANNER, getString(R.string.publisher_id));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            result.setLayoutParams(params);

            FrameLayout frame = (FrameLayout)findViewById(R.id.ad_frame);
            frame.addView(result);
        }

        return result;
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

        if (keep && timeLineAdapter != null) {
            timeLineAdapter.notifyDataSetChanged();
        } else {
            final ProgressDialog waitDialog = showWait(getString(R.string.loading));
            cache.getTimeLine(new BackLogCache.CacheResponseNotify() {
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
                    timeLineAdapter = adapter;
                    list.setAdapter(timeLineAdapter);
                    loadIcons();
                }

                @Override
                public void success(Drawable icon) {
                    waitDialog.dismiss();
                    if (timeLineAdapter != null) timeLineAdapter.notifyDataSetChanged();
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

    private void loadIcons() {
        SortedSet<Integer> userIds = new TreeSet<Integer>();

        for (int i = 0; i < timeLineAdapter.getCount(); i++) {
            TimeLine.Item item = (TimeLine.Item)timeLineAdapter.getItem(i);
            userIds.add(item.getUser().getId());
        }

        BackLogCache.sharedInstance().loadIcons(userIds, new Runnable() {
            @Override
            public void run() {
                timeLineAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);

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
        }
        return false;
    }
}
