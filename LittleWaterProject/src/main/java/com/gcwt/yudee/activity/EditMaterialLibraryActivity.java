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
import com.gcwt.yudee.util.LittleWaterUtility;

import java.io.File;

/**
 * Created by peter on 3/31/15.
 */
public class EditMaterialLibraryActivity extends BaseLittleWaterActivity implements TextWatcher {
    private EditText mNameEditView;
    private TextView mLibraryNameView;
    private CardItem mCardItem = new CardItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_material_library);
        initViews();
        initEvents();
    }

    @Override
    public void initViews() {
        setTitle("");

        mCardCoverView = (ImageView) findViewById(R.id.content_iv);
        mRootView = (ViewGroup) findViewById(R.id.root_view);
        mNameEditView = (EditText) findViewById(R.id.edit_name);
        mLibraryNameView = (TextView) findViewById(R.id.library_name);
    }

    @Override
    protected void initEvents() {
        mCardItem = (CardItem) getIntent().getSerializableExtra("library");
        mNameEditView.addTextChangedListener(this);

        String libraryName = LittleWaterUtility.getCardDisplayName(mCardItem.getName());
        mNameEditView.setText(libraryName);
        mLibraryNameView.setText(libraryName);

        mCardCoverView.setImageDrawable(LittleWaterUtility.getRoundCornerDrawableFromSdCard(mCardItem.getCover()));
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
                saveEditedLibrary();
                break;
            case R.id.trash:
                deleteLibrary();
                break;
        }
    }

    private void deleteLibrary() {
        DialogManager.showConfirmDialog(this, null, "确认删除整个分类?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FileUtils.delFolder(LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + mCardItem.getName());
                dialogInterface.dismiss();

                Intent data = new Intent();
                data.putExtra("result_material_library", mCardItem);
                data.putExtra("library_deleted", true);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        }, null);
    }

    private void saveEditedLibrary() {
        if (TextUtils.isEmpty(mLibraryNameView.getText().toString())) {
            showCustomToast("请输入分类名称");
            return;
        }
        String libraryName = mLibraryNameView.getText().toString();
        File libFile = new File(LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + libraryName);
        if (libFile.exists()) {
            showCustomToast("分类名称已存在, 请换个名称.");
            return;
        }
        libFile.mkdirs();

        LittleWaterApplication.getMaterialLibraryCardsPreferences().putString(libraryName, "");

        BitmapDrawable drawable = (BitmapDrawable) mCardCoverView.getDrawable();
        String cover = null;
        if (drawable != null && drawable.getBitmap() != null) {
            Bitmap bitmap = drawable.getBitmap();
            String coverFolder = LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + libraryName;
            String coverName = "cover.png";
            PhotoUtil.saveBitmap(coverFolder, coverName, bitmap, true);
            cover = coverFolder + "/" + coverName;
            LittleWaterApplication.getMaterialLibraryCoverPreferences().putString(libraryName, cover);
        }

        mCardItem.name = libraryName;
        mCardItem.cover = cover;
        mCardItem.editable = true;
        Intent data = new Intent();
        data.putExtra("result_material_library", mCardItem);
        setResult(Activity.RESULT_OK, data);
        finish();
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
