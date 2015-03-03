/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by peter on 3/3/15.
 */
public class AssetUtil {
    private static ArrayList<String> mCardCategoryList = new ArrayList<String>();

    public static String getFromAssets(Context context, String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(
                    fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null) {
                if (line.trim().equals("")) continue;
                Result += line + "\r\n";
            }
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static InputStream getAssets(Context context, String fileName) {
        AssetManager am = context.getAssets();
        try {
            return am.open(fileName);
        } catch (IOException e) {
            Log.d("zheng", "getAssets exception:", e);
            e.printStackTrace();
        }
        return null;
    }

    public static void parseAsset(Context context) {
        try {
            String[] cardCategories = context.getAssets().list("cards");
            for (String cardCategory : cardCategories) {
                Log.d("zheng", "cardCategory:" + cardCategory);
                cardCategory = "cards/" + cardCategory;
                mCardCategoryList.add(cardCategory);
                String[] cardItems = context.getAssets().list(cardCategory);
                for (String cardItem : cardItems) {
                    Log.d("zheng", "cardItem:" + cardItem);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
