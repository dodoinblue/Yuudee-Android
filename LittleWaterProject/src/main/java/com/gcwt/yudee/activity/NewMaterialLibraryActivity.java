/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.pattern.util.DialogManager;
import android.pattern.util.FileUtils;
import android.pattern.util.PhotoUtil;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;

import java.io.File;
import java.util.Set;

/**
 * Created by peter on 3/10/15.
 */
public class NewMaterialLibraryActivity extends BaseLittleWaterActivity implements TextWatcher {
    protected EditText mNameEditView;
    protected TextView mLibraryNameView;
    protected CardItem mCardItem = new CardItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_material_library);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");

        mCardCoverView = (ImageView) findViewById(R.id.content_iv);
        updateToRoundImageDrawable(mCardCoverView);
        mRootView = (ViewGroup) findViewById(R.id.root_view);
        mNameEditView = (EditText) findViewById(R.id.edit_name);
        mLibraryNameView = (TextView) findViewById(R.id.library_name);
    }

    protected void initEvents() {
        mNameEditView.addTextChangedListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_cover:
                showAvatarPop();
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.confirm:
                saveMaterialLibrary();
                break;
            case R.id.trash:
                deleteLibrary();
                break;
        }
    }

    private void deleteLibrary() {
        DialogManager.showConfirmDialog(this, null, getString(R.string.confirm_to_delete_category), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FileUtils.delFolder(LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + mCardItem.getName());
                dialogInterface.dismiss();

                // Delete these here in case they mixed with EditCategoryFolderActivity's category delete.
                LittleWaterApplication.getMaterialLibraryCardsPreferences().remove(mCardItem.getName());
                LittleWaterApplication.getMaterialLibraryCoverPreferences().remove(mCardItem.getName());

                Intent data = new Intent();
                data.putExtra("library_card", mCardItem);
                data.putExtra("library_deleted", true);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        }, null);
    }

    private void saveMaterialLibrary() {
        if (TextUtils.isEmpty(mLibraryNameView.getText().toString())) {
            showCustomToast(R.string.enter_category_name);
            return;
        }
        String newLibraryName = mLibraryNameView.getText().toString();
        String oldLibraryName = mCardItem.name;
        if (this instanceof EditMaterialLibraryActivity) {
            if (isMaterialLibraryNameChanged(oldLibraryName, newLibraryName)) {
                Set<String> librarySet = LittleWaterApplication.getMaterialLibraryCoverPreferences().getAll().keySet();
                for (String libraryName : librarySet) {
                    if (TextUtils.equals(libraryName, newLibraryName)) {
                        showCustomToast(R.string.category_name_already_exists);
                        return;
                    }
                }

//                File oldLibFile = new File(LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + oldLibraryName);
//                File newLibFile = new File(LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + newLibraryName);
//                oldLibFile.renameTo(newLibFile);
//            ArrayList<CardItem> libraryCardList = LittleWaterUtility.getMaterialLibraryCardsList(mCardItem.getName());
//            LittleWaterApplication.getMaterialLibraryCardsPreferences().remove(mCardItem.getName());
//            LittleWaterUtility.setMaterialLibraryCardsList(newLibraryName, libraryCardList);
            }
        } else {
            File libFile = new File(LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + newLibraryName);
            if (libFile.exists()) {
                showCustomToast(R.string.category_name_already_exists);
                return;
            }
            libFile.mkdirs();
            LittleWaterApplication.getMaterialLibraryCardsPreferences().putString(newLibraryName, "");
        }

        if (mGotACardCover || mCardItem.cover == null) {
            BitmapDrawable drawable = (BitmapDrawable) mCardCoverView.getDrawable();
            String finalLibraryName = newLibraryName;
            if (isMaterialLibraryNameChanged(oldLibraryName, newLibraryName)) {
                finalLibraryName = oldLibraryName;
            }
            if (drawable != null && drawable.getBitmap() != null) {
                Bitmap bitmap = drawable.getBitmap();
                String coverFolder = LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + finalLibraryName;
                String coverName = "cover.png";
                PhotoUtil.saveBitmap(coverFolder, coverName, bitmap, true);
                String cover = coverFolder + "/" + coverName;
                LittleWaterApplication.getMaterialLibraryCoverPreferences().putString(finalLibraryName, cover);
                mCardItem.cover = cover;
            }
        }

        mCardItem.name = newLibraryName;
        mCardItem.editable = true;
        mCardItem.isLibrary = true;
        Intent data = new Intent();
        data.putExtra("library_card", mCardItem);
        if (isMaterialLibraryNameChanged(oldLibraryName, newLibraryName)) {
            data.putExtra("old_library_name", oldLibraryName);
        }
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private boolean isMaterialLibraryNameChanged(String oldLibraryName, String newLibraryName) {
        return !TextUtils.isEmpty(oldLibraryName) && !TextUtils.equals(oldLibraryName, newLibraryName);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mLibraryNameView.setText(mNameEditView.getText().toString());
    }

}
