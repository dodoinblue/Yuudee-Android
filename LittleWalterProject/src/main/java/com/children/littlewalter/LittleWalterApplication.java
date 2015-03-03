/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter;

import android.pattern.BaseApplication;
import android.pattern.util.Preferences;

/**
 * Created by peter on 3/3/15.
 */
public class LittleWalterApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static LittleWalterApplication getInstance() {
        return (LittleWalterApplication) BaseApplication.getInstance();
    }

    public static Preferences getAppPreferences() {
        return Preferences.getInstance(getInstance());
    }
}
