/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.pattern.util.BitmapUtil;
import android.pattern.util.PhotoUtil;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.activity.LittleWaterActivity;
import com.gcwt.yudee.activity.ShowCardActivity;
import com.gcwt.yudee.model.CardItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 3/19/15.
 */
public class LittleWaterUtility {
    public static final int ROUND_PX = 8;
    private static MediaPlayer sMediaPlayer;

    public static synchronized ArrayList<CardItem> getCategoryCardsList(String catetgory) {
        String curCategoryCardsJson = LittleWaterApplication.getCategoryCardsPreferences().getString(catetgory);
        return getCardsList(curCategoryCardsJson);
    }

    public static synchronized void setCategoryCardsList(String catetgory, List<CardItem> cardItemList) {
        String curCategoryCardsJson = new Gson().toJson(cardItemList);
        LittleWaterApplication.getCategoryCardsPreferences().putString(catetgory, curCategoryCardsJson);
    }

    public static synchronized  ArrayList<CardItem> getMaterialLibraryCardsList(String library) {
        String curLibraryCardsJson = LittleWaterApplication.getMaterialLibraryCardsPreferences().getString(library);
        return getCardsList(curLibraryCardsJson);
    }

    public static synchronized void setMaterialLibraryCardsList(String library, List<CardItem> cardItemList) {
        String cardsJson = new Gson().toJson(cardItemList);
        LittleWaterApplication.getMaterialLibraryCardsPreferences().putString(library, cardsJson);
    }

    private static ArrayList<CardItem> getCardsList(String cardsJsonStr) {
        if (TextUtils.isEmpty(cardsJsonStr)) {
            return new ArrayList<CardItem>();
        }
        return new Gson().fromJson(cardsJsonStr, new TypeToken<List<CardItem>>() { }.getType());
    }

    public static synchronized void removeCardFromCategory(CardItem item) {
        ArrayList<CardItem> itemList = getCategoryCardsList(item.category);
        Log.d("zheng", "removeCardFromCategory:" + item.category);
        if (itemList.remove(item)) {
            Log.d("zheng", "removeCardFromCategory: remove" + item.category);
        } else {
            for (CardItem eachItem : itemList) {
                if (eachItem.isLibrary) {
                    eachItem.childCardList.remove(item);
                }
            }
        }
        LittleWaterUtility.setCategoryCardsList(item.category, itemList);
    }

    public static synchronized void updateCardToCategory(CardItem item) {
        ArrayList<CardItem> itemList = getCategoryCardsList(item.category);
        int position = itemList.indexOf(item);
        if (position != -1) {
            itemList.set(position, item);
        } else {
            for (CardItem eachItem : itemList) {
                if (eachItem.isLibrary) {
                    position = eachItem.childCardList.indexOf(item);
                    if (position != -1) {
                        eachItem.childCardList.set(position, item);
                    }
                }
            }
        }
        LittleWaterUtility.setCategoryCardsList(item.category, itemList);
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

//    public static int getCardPosition(String cardName) {
//        String[] cardInfoArray = cardName.split("-");
//        if (cardInfoArray.length >= 2) {
//            return Integer.valueOf(cardInfoArray[0]);
//        }
//        return 0;
//    }

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
//        return new BitmapDrawable(getBitmapFromSdCard(imageFilePath));
        Log.d("zheng", "getRoundCornerDrawableFromSdCard imageFilePath:" + imageFilePath);
        if (TextUtils.isEmpty(imageFilePath)) {
            return null;
        }
        Bitmap bitmap = getBitmapFromSdCard(imageFilePath);
        if (bitmap == null) {
            return null;
        }
        return new BitmapDrawable(BitmapUtil.getRoundedCornerBitmap(bitmap, ROUND_PX));
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

    public static void playCardByFlippingAnimation(final Context context, View view, CardItem moveItem) {
        final ImageView iv = (ImageView) view.findViewById(R.id.content_iv);
        final ViewFlipper flipper = (ViewFlipper) view.findViewById(R.id.content_show);
        if (flipper.isFlipping()) {
            return;
        }
        flipper.setVisibility(View.VISIBLE);
        iv.setVisibility(View.GONE);
        if (moveItem.getAudios().size() > 0 && !moveItem.getCardSettings().getMute()) {
            // will add back later for develop silently
            playAudio(moveItem.getAudios().get(0));
        }
        List<String> images = moveItem.getImages();
        flipper.removeAllViews();
        for (String image : images) {
            ImageView imageView = (ImageView) LayoutInflater.from(context).inflate(R.layout.layout_image, flipper, false);
            imageView.setImageDrawable(LittleWaterUtility.getRoundCornerDrawableFromSdCard(image));
            flipper.addView(imageView);
        }
        flipper.startFlipping();
        flipper.postDelayed(new Runnable() {
            @Override
            public void run() {
                flipper.stopFlipping();
                flipper.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
                flipper.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (context instanceof ShowCardActivity) {
                            ShowCardActivity activity = (ShowCardActivity) context;
                            activity.setResult(Activity.RESULT_OK);
                            activity.finish();
                        }
                    }
                }, 800);
            }
        }, 800 * moveItem.getImages().size());
    }

    private static void playAudio(String audioPath) {
        playAudio(audioPath, null);
    }

    public static synchronized void playAudio(String audioPath, MediaPlayer.OnCompletionListener listener) {
        stopAudio();
        try {
            sMediaPlayer = new MediaPlayer();
            sMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            sMediaPlayer.setDataSource(audioPath);
            sMediaPlayer.prepare();
            if (listener != null) {
                sMediaPlayer.setOnCompletionListener(listener);
            }
            sMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void stopAudio() {
        if (sMediaPlayer != null) {
            if (sMediaPlayer.isPlaying()) {
                sMediaPlayer.stop();
            }
            sMediaPlayer.release();
            sMediaPlayer = null;
        }
    }

    public static void playCardByRotateAndFlippingAnimation(final Context context, final View view, final CardItem cardItem) {
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.rotate);
        shake.setFillAfter(false);
        view.startAnimation(shake);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LittleWaterUtility.playCardByFlippingAnimation(context, view, cardItem);
            }
        }, 400);
    }

    public static void parseCardCategory(File categoryFolder, ArrayList<CardItem> cardList, HashMap<String, String> mCategoryCoverMap, String category) {
        File[] cardItemFolders = categoryFolder.listFiles();
        for (File cardItemFolder : cardItemFolders) {
            if (cardItemFolder.isDirectory()) {
                CardItem item = new CardItem();
                cardList.add(item);
//                    item.setName(cardItemFolder.getName().split("-")[1].split("\\.")[0]);
//                item.setName(cardItemFolder.getName().split("\\.")[0]);
                String name = cardItemFolder.getName().substring(0, cardItemFolder.getName().indexOf("."));
                item.setName(name);
                item.category = category;
                File[] mediaFolders = cardItemFolder.listFiles();
                for (File mediaFolder : mediaFolders) {
                    if (mediaFolder.getPath().contains("audio")) {
                        File[] audioFiles = mediaFolder.listFiles();
                        List<String> audios = new ArrayList<String>();
                        if (audioFiles != null && audioFiles.length > 0) {
                            for (File audioFile : audioFiles) {
                                Log.d("zheng", "audio:" + audioFile.getAbsolutePath());
                                audios.add(audioFile.getAbsolutePath());
                            }
                        }
                        item.setAudios(audios);
                    } else if (mediaFolder.getPath().contains("image")) {
                        File[] imageFiles = mediaFolder.listFiles();
                        List<String> images = new ArrayList<String>();
                        if (imageFiles != null && imageFiles.length > 0) {
                            for (File imageFile : imageFiles) {
                                Log.d("zheng", "image:" + imageFile.getAbsolutePath());
                                images.add(imageFile.getAbsolutePath());
                            }
                        }
                        item.setImages(images);
                    }
                }
            } else {
                mCategoryCoverMap.put(category, cardItemFolder.getAbsolutePath());
            }
        }
    }

    public static void hideSoftInput(Activity activity) {
        if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (activity.getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void showSoftInput(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    public static synchronized void deleteLibraryInCategory(CardItem cardItem) {
        Set<String> categorySet = LittleWaterApplication.getCategoryCardsPreferences().getAll().keySet();
        for (String category : categorySet) {
            ArrayList<CardItem> itemList = LittleWaterUtility.getCategoryCardsList(category);
            int position = itemList.indexOf(cardItem);
            if (position != -1) {
                CardItem emptyItem = new CardItem();
                emptyItem.isEmpty = true;
                itemList.set(position, emptyItem);
                LittleWaterUtility.setCategoryCardsList(category, itemList);
            }
        }
    }

    public static int getLastNotEmptyCardPosition(List<CardItem> cardItemList) {
        CardItem lastNotEmptyItem = null;
        for (CardItem item : cardItemList) {
            if (!item.getIsEmpty()) {
                lastNotEmptyItem = item;
            }
        }
        if (lastNotEmptyItem != null) {
            return cardItemList.lastIndexOf(lastNotEmptyItem);
        }
        return -1;
    }
}
