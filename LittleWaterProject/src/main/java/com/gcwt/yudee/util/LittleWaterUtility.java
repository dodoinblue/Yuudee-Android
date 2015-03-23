/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.pattern.util.PhotoUtil;
import android.text.TextUtils;

import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.model.CardSettings;
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

    public static void setCategoryCardsList(String catetgory, List<CardItem> cardItemList) {
        String curCategoryCardsJson = new Gson().toJson(cardItemList);
        LittleWaterApplication.getCategoryCardsPreferences().putString(catetgory, curCategoryCardsJson);
    }

    public static List<CardItem> getMaterialLibraryCardsList(String library) {
        String curLibraryCardsJson = LittleWaterApplication.getMaterialLibraryCardsPreferences().getString(library);
        return getCardsList(curLibraryCardsJson);
    }

    public static void setMaterialLibraryCardsList(String library, List<CardItem> cardItemList) {
        String cardsJson = new Gson().toJson(cardItemList);
        LittleWaterApplication.getMaterialLibraryCardsPreferences().putString(library, cardsJson);
    }

    private static List<CardItem> getCardsList(String cardsJsonStr) {
        if (TextUtils.isEmpty(cardsJsonStr)) {
            return new ArrayList<CardItem>();
        }
        return new Gson().fromJson(cardsJsonStr, new TypeToken<List<CardItem>>() { }.getType());
    }

    public static void saveDrawable(String imageFolder, String imageName, BitmapDrawable drawable) {
        if (drawable != null && drawable.getBitmap() != null) {
            Bitmap bitmap = drawable.getBitmap();
            PhotoUtil.saveBitmap(imageFolder, imageName, bitmap, true);
//            mLibraryCard.images.add(imageFolder + "/" + imageName);
        }
    }

//    public static CardSettings getCardSettings(String cardName) {
//        String cardSettingsJson = LittleWaterApplication.getCardSettingsPreferences().getString(cardName);
//        if (TextUtils.isEmpty(cardSettingsJson)) {
//            return new CardSettings();
//        }
//
//        return new Gson().fromJson(cardSettingsJson, CardSettings.class);
//    }
//
//    public static void setCardSettings(String cardName, CardSettings cardSettings) {
//        LittleWaterApplication.getCardSettingsPreferences().putString(cardName, new Gson().toJson(cardSettings));
//    }
}
