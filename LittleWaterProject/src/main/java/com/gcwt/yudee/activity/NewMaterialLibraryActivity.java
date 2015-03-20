/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.pattern.util.PhotoUtil;
import android.pattern.util.PhotoUtils;
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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by peter on 3/10/15.
 */
public class NewMaterialLibraryActivity extends BaseLittleWaterActivity implements TextWatcher {
    private EditText mNameEditView;
    private TextView mLibraryName;
    private CardItem mCardItem = new CardItem();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_new_material_library);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");

        mCardCoverView = (ImageView) findViewById(R.id.content_iv);
        mRootView = (ViewGroup) findViewById(R.id.root_view);
        mNameEditView = (EditText) findViewById(R.id.edit_name);
        mLibraryName = (TextView) findViewById(R.id.library_name);
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
                saveNewLibrary();
                break;
        }
    }

    private void saveNewLibrary() {
        if (TextUtils.isEmpty(mLibraryName.getText().toString())) {
            showCustomToast("请输入分类名称");
            return;
        }
        String libraryName = mLibraryName.getText().toString();
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
        Intent data = new Intent();
        data.putExtra("result_new_material_library", mCardItem);
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
        mLibraryName.setText(mNameEditView.getText().toString());
    }

}
