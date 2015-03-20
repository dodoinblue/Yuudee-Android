/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.util;

import android.text.TextUtils;

import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.model.CardItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 3/19/15.
 */
public class LittleWaterUtility {
    public static List<CardItem> getCategoryCardsList(String catetgory) {
        String curCategoryCardsJson = LittleWaterApplication.getCategoryCardsPreferences().getString(catetgory);
        return getCardsList(curCategoryCardsJson);
    }

    public static List<CardItem> getMaterialLibraryCardsList(String library) {
        String curLibraryCardsJson = LittleWaterApplication.getMaterialLibraryCardsPreferences().getString(library);
        return getCardsList(curLibraryCardsJson);
    }

    private static List<CardItem> getCardsList(String cardsJsonStr) {
        if (TextUtils.isEmpty(cardsJsonStr)) {
            return new ArrayList<CardItem>();
        }
        return new Gson().fromJson(cardsJsonStr, new TypeToken<List<CardItem>>() { }.getType());
    }
}
