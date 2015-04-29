/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;

import java.util.ArrayList;

/**
 * Created by peter on 15/3/31.
 */
public class SubFolderLittleWaterActivity extends LittleWaterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mRctivityLayout = R.layout.activity_sub_folder_little_walter;
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        super.initViews();
        setTitle("");
        findViewById(R.id.unlock_parent_ui).setVisibility(View.GONE);
        findViewById(R.id.root_container).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        findViewById(R.id.back_to_main_ui).setVisibility(View.VISIBLE);
    }

    @Override
    protected void initEvents() {
        mCurrentCategory = LittleWaterApplication.getAppSettingsPreferences().getString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY);
        mCurrentCategoryCardLayoutSetting = LittleWaterApplication.getCategorySettingsPreferences().getInt(mCurrentCategory, LAYOUT_TYPE_2_X_2);
        restoreSubFolderList();
        displayCards();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_to_main_ui:
                backupSubFolderList();
                finish();
                break;
            default:
                super.onClick(view);
                break;
        }
    }

    @Override
    protected void backupSubFolderList() {
        mSubFolderItem.childCardList = mContainer.getAllMoveItems();
        ArrayList<CardItem> itemList = LittleWaterUtility.getCategoryCardsList(mCurrentCategory);
        itemList.set(itemList.indexOf(mSubFolderItem), mSubFolderItem);
        LittleWaterUtility.setCategoryCardsList(mCurrentCategory, itemList);
    }

    private void restoreSubFolderList() {
        mSubFolderItem = (CardItem) getIntent().getSerializableExtra("library");
        Log.d("zheng", "subfolder name:" + mSubFolderItem.name);
        mCardItemList.clear();
        mCardItemList.addAll(mSubFolderItem.childCardList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        backupSubFolderList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreSubFolderList();
        mContainer.refreshView();
        mContainer.showEdit(mIsInParentMode);
    }
}
