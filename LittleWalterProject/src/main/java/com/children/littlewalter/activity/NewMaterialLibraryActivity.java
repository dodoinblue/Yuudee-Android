/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.children.littlewalter.BaseLittleWalterActivity;
import com.children.littlewalter.LittleWalterApplication;
import com.children.littlewalter.R;
import com.children.littlewalter.model.CardItem;
import com.children.littlewalter.util.LittleWalterConstant;

import java.io.File;

/**
 * Created by peter on 3/10/15.
 */
public class NewMaterialLibraryActivity extends BaseLittleWalterActivity implements TextWatcher {
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
        File libFile = new File(LittleWalterConstant.MATERIAL_LIBRARIES_DIRECTORY + libraryName);
        if (libFile.exists()) {
            showCustomToast("分类名称已存在, 请换个名称.");
            return;
        }
        libFile.mkdirs();

        LittleWalterApplication.getMaterialLibraryCardsPreferences().putString(libraryName, "");

        mCardItem.name = libraryName;
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
