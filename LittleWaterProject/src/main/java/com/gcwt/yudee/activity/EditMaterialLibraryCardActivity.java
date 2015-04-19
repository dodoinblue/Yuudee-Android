/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.content.DialogInterface;
import android.pattern.util.DialogManager;
import android.text.TextUtils;
import android.view.View;

import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        DialogManager.showConfirmDialog(this, null, getString(R.string.confirm_to_delete_card), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                List<CardItem> cardItemList =  LittleWaterUtility.getMaterialLibraryCardsList(mLibraryCard.libraryName);
                cardItemList.remove(mLibraryCard);
                LittleWaterUtility.setMaterialLibraryCardsList(mLibraryCard.libraryName, cardItemList);

                Set<String> categorySet = LittleWaterApplication.getCategoryCardsPreferences().getAll().keySet();
                for (String category : categorySet) {
                    ArrayList<CardItem> itemList = LittleWaterUtility.getCategoryCardsList(category);
                    removeLibraryCardInCategory(itemList);
                    LittleWaterUtility.setCategoryCardsList(category, itemList);
                }
//                Intent data = new Intent();
//                data.putExtra("library_card", mLibraryCard);
//                data.putExtra("library_deleted", true);
//                setResult(Activity.RESULT_OK, data);
                finish();
            }
        }, null);
    }

    private void removeLibraryCardInCategory(ArrayList<CardItem> itemList) {
        itemList.remove(mLibraryCard);
        for (CardItem eachItem : itemList) {
            if (eachItem.isLibrary) {
                removeLibraryCardInCategory(eachItem.childCardList);
            }
        }
    }
}
