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

    public static Preferences getAppSettingsPreferences() {
        return Preferences.getInstance(getInstance());
    }

    public static Preferences getCategoryCardsPreferences() {
        return Preferences.getInstance(getInstance(), "category_cards");
    }

    public static Preferences getCategoryCoverPreferences() {
        return Preferences.getInstance(getInstance(), "category_cover");
    }

    public static Preferences getCategorySettingPreferences() {
        return Preferences.getInstance(getInstance(), "category_setting");
    }

    public static Preferences getCardSettingPreferences() {
        return Preferences.getInstance(getInstance(), "card_setting");
    }
}
