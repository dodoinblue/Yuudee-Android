/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.R;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;

/**
 * Created by peter on 15/3/23.
 */
public class NewCategoryCardActivity extends BaseLittleWaterActivity {
    private String mCurrentCategory;
    private CardItem mCardItem;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_new_category_card);
        initViews();
        initEvents();
    }

    @Override
    protected void initViews() {

    }

    @Override
    protected void initEvents() {
        mCurrentCategory = getIntent().getStringExtra("");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.add_card_from_resource:
                Intent addIntent = new Intent(this, MaterialLibrariesActivity.class);
                startActivityForResult(addIntent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_ADD_MATERIAL_LIBRARY_CARD);
                finish();
                break;
            case R.id.create_a_new_card:
                Intent newIntent = new Intent(this, NewMaterialLibraryCardActivity.class);
                startActivityForResult(newIntent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_MATERIAL_LIBRARY_CARD);
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_ADD_MATERIAL_LIBRARY_CARD:
                break;
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_MATERIAL_LIBRARY_CARD:
                mCardItem = (CardItem) data.getSerializableExtra("library_card");
                setResult(Activity.RESULT_OK, data);
                break;
        }
    }
}