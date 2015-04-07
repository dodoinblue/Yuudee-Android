/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.pattern.util.DialogManager;
import android.pattern.util.FileUtils;
import android.view.View;

import com.gcwt.yudee.R;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;

/**
 * Created by peter on 3/31/15.
 */
public class EditMaterialLibraryCardActivity extends NewMaterialLibraryCardActivity {

    @Override
    public void initViews() {
        super.initViews();
        findViewById(R.id.root_view).setBackgroundResource(R.mipmap.edit_card_bg);
        mLibraryCard = (CardItem) getIntent().getSerializableExtra("library_card");
        mNameEditView.setText(mLibraryCard.getName());
        mCardNameView.setText(mLibraryCard.getName());
        findViewById(R.id.trash).setVisibility(View.VISIBLE);
        if (mLibraryCard.getAudios().size() > 0) {
            mAudioFile = mLibraryCard.getAudios().get(0);
        }

        mCardCoverView.setImageDrawable(LittleWaterUtility.getRoundCornerDrawableFromSdCard(mLibraryCard.getImages().get(0)));
        mChooseCategoryBtn.setText(mLibraryCard.libraryName);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.trash:
                deleteLibraryCard();
                break;
            default:
                super.onClick(view);
        }
    }

    private void deleteLibraryCard() {
        DialogManager.showConfirmDialog(this, null, "确认删除卡片?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                Intent data = new Intent();
                data.putExtra("library_card", mLibraryCard);
                data.putExtra("library_deleted", true);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        }, null);
    }
}
