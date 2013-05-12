package com.abplus.comebacklog.billing;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import com.android.vending.billing.IInAppBillingService;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/05/12 16:51
 */
public class BillingService extends Service implements ServiceConnection {
    IInAppBillingService billingService;

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        billingService =  IInAppBillingService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        billingService = null;
    }

    private boolean bind() {
        Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        boolean result = bindService(intent, this, Context.BIND_AUTO_CREATE);
        return result;
    }

    protected Bundle makeRequestBundle(String method) {
        Bundle result = new Bundle();

        result.putString("BILLING_REQUEST", method);
        result.putString("PACKAGE_NAME", getPackageName());
        result.putInt("API_VERSION", 3);

        return result;
    }
}


