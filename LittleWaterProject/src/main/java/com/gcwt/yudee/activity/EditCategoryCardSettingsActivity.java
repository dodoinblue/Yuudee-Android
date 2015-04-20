/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.R;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;

/**
 * Created by peter on 3/10/15.
 */
public class EditCategoryCardSettingsActivity extends BaseLittleWaterActivity {
    private View mNoNanimationChecked;
    private View mZoomInAnimationChecked;
    private View mZoomInAndRotateAnimationChecked;
    private Switch mMuteSwitch;
    private CardItem mCardItem;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_edit_category_card_settings);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
        mNoNanimationChecked = findViewById(R.id.no_animation_checked);
        mZoomInAnimationChecked = findViewById(R.id.zoomin_animation_checked);
        mZoomInAndRotateAnimationChecked = findViewById(R.id.zoomin_and_rotate_animation_checked);
        mMuteSwitch = (Switch) findViewById(R.id.mute_switch);
    }

    protected void initEvents() {
        mCardItem = (CardItem) getIntent().getSerializableExtra("card_item");
        if (mCardItem.editable) {
            TextView editCardView = (TextView) findViewById(R.id.edit_card);
            editCardView.setText(R.string.enter_material_libray_to_edit_card);
        }
        switch (mCardItem.cardSettings.animationType) {
            case LittleWaterConstant.ANIMATION_NONE:
                setNoNanimationChecked();
                break;
            case LittleWaterConstant.ANIMATION_ZOOM_IN_AND_ROTATE:
                setZoomInAndRotateAnimationChecked();
                break;
            case LittleWaterConstant.ANIMATION_ZOOM_IN:
            default:
                setZoomInAnimationChecked();
                break;
        }
        mMuteSwitch.setChecked(mCardItem.getCardSettings().getMute());
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.confirm:
                mCardItem.getCardSettings().setMute(mMuteSwitch.isChecked());
                LittleWaterUtility.updateCardToCategory(mCardItem);
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
            case R.id.edit_card:
                if (!mCardItem.editable) {
                    return;
                }

                Intent intent = new Intent(EditCategoryCardSettingsActivity.this, EditMaterialLibraryCardActivity.class);
                intent.putExtra("library_card", mCardItem);
                startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY_CARD);
                findViewById(R.id.root_view).setVisibility(View.GONE);
                break;
            case R.id.delete_card:
                LittleWaterUtility.removeCardFromCategory(mCardItem);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED && requestCode == LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY_CARD) {
            finish();
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY_CARD:
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
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
