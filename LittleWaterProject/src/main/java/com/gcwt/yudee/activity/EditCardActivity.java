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
import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;

/**
 * Created by peter on 3/10/15.
 */
public class EditCardActivity extends BaseLittleWaterActivity {
    private View mNoNanimationChecked;
    private View mZoomInAnimationChecked;
    private View mZoomInAndRotateAnimationChecked;

    private CardItem mCardItem;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_edit_card);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
        mNoNanimationChecked = findViewById(R.id.no_animation_checked);
        mZoomInAnimationChecked = findViewById(R.id.zoomin_animation_checked);
        mZoomInAndRotateAnimationChecked = findViewById(R.id.zoomin_and_rotate_animation_checked);
    }

    protected void initEvents() {
        mCardItem = (CardItem) getIntent().getSerializableExtra("card_item");
        switch (mCardItem.cardSettings.animationType) {
            case LittleWaterConstant.ANIMATION_NONE:
                setNoNanimationChecked();
                break;
            case LittleWaterConstant.ANIMATION_ZOOM_IN:
                setZoomInAnimationChecked();
                break;
            case LittleWaterConstant.ANIMATION_ZOOM_IN_AND_ROTATE:
                setZoomInAndRotateAnimationChecked();
                break;
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.confirm:
                Intent data = new Intent();
                data.putExtra("card_item", mCardItem);
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
            case R.id.no_animation_container:
                setNoNanimationChecked();
                break;
            case R.id.animation_zoom_in_container:
                setZoomInAnimationChecked();
                break;
            case R.id.animation_zoomin_and_rotate_container:
                setZoomInAndRotateAnimationChecked();
                break;
        }
    }

    private void setNoNanimationChecked() {
        mNoNanimationChecked.setVisibility(View.VISIBLE);
        mZoomInAnimationChecked.setVisibility(View.INVISIBLE);
        mZoomInAndRotateAnimationChecked.setVisibility(View.INVISIBLE);
        mCardItem.cardSettings.animationType = LittleWaterConstant.ANIMATION_NONE;
    }

    private void setZoomInAnimationChecked() {
        mNoNanimationChecked.setVisibility(View.INVISIBLE);
        mZoomInAnimationChecked.setVisibility(View.VISIBLE);
        mZoomInAndRotateAnimationChecked.setVisibility(View.INVISIBLE);
        mCardItem.cardSettings.animationType = LittleWaterConstant.ANIMATION_ZOOM_IN;
    }

    private void setZoomInAndRotateAnimationChecked() {
        mNoNanimationChecked.setVisibility(View.INVISIBLE);
        mZoomInAnimationChecked.setVisibility(View.INVISIBLE);
        mZoomInAndRotateAnimationChecked.setVisibility(View.VISIBLE);
        mCardItem.cardSettings.animationType = LittleWaterConstant.ANIMATION_ZOOM_IN_AND_ROTATE;
    }
}
