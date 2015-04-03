/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.pattern.util.BitmapUtil;
import android.pattern.util.PhotoUtil;
import android.text.TextUtils;

import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.activity.LittleWaterActivity;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.model.CardSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by peter on 3/19/15.
 */
public class LittleWaterUtility {
    private static final int ROUND_PX = 5;

    public static ArrayList<CardItem> getCategoryCardsList(String catetgory) {
        String curCategoryCardsJson = LittleWaterApplication.getCategoryCardsPreferences().getString(catetgory);
        return getCardsList(curCategoryCardsJson);
    }

    public static void setCategoryCardsList(String catetgory, List<CardItem> cardItemList) {
        String curCategoryCardsJson = new Gson().toJson(cardItemList);
        LittleWaterApplication.getCategoryCardsPreferences().putString(catetgory, curCategoryCardsJson);
    }

    public static ArrayList<CardItem> getMaterialLibraryCardsList(String library) {
        String curLibraryCardsJson = LittleWaterApplication.getMaterialLibraryCardsPreferences().getString(library);
        ArrayList<CardItem> libraryCardList = getCardsList(curLibraryCardsJson);

        curLibraryCardsJson = LittleWaterApplication.getCategoryCardsPreferences().getString(library);
        libraryCardList.addAll(getCardsList(curLibraryCardsJson));
        return libraryCardList;
    }

    public static void setMaterialLibraryCardsList(String library, List<CardItem> cardItemList) {
        String cardsJson = new Gson().toJson(cardItemList);
        LittleWaterApplication.getMaterialLibraryCardsPreferences().putString(library, cardsJson);
    }

    private static ArrayList<CardItem> getCardsList(String cardsJsonStr) {
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

    public static int getCardPosition(String cardName) {
        String[] cardInfoArray = cardName.split("-");
        if (cardInfoArray.length >= 2) {
            return Integer.valueOf(cardInfoArray[0]);
        }
        return 0;
    }

    public static String getCardDisplayName(String cardName) {
        String[] cardInfoArray = cardName.split("-");
        if (cardInfoArray.length >= 2) {
            return cardInfoArray[1];
        }
        return cardName;
    }

    public static Bitmap getBitmapFromSdCard(String imageFilePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        return BitmapFactory.decodeFile(imageFilePath, options);
    }

    public static Drawable getRoundCornerDrawableFromSdCard(String imageFilePath) {
        return new BitmapDrawable(BitmapUtil.getRoundedCornerBitmap(getBitmapFromSdCard(imageFilePath), ROUND_PX));
    }

    /**
     * If haven't sorted before, sort the list and rename the card name by deleting No. example '01-'
     * @param cardItemList
     */
    public static void sortCardList(List<CardItem> cardItemList) {
        if (cardItemList.size() > 0 && cardItemList.get(0).getName() != null
                && cardItemList.get(0).getName().contains("-")) {
            Collections.sort(cardItemList, new LittleWaterActivity.CardItemComparator());
            for (CardItem item : cardItemList) {
                if (!item.getIsEmpty()) {
                    item.setName(LittleWaterUtility.getCardDisplayName(item.getName()));
                }
            }
        }
    }
}
