/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.children.littlewalter.BaseLittleWalterActivity;
import com.children.littlewalter.LittleWalterApplication;
import com.children.littlewalter.R;

/**
 * Created by peter on 3/10/15.
 */
public class NewResourceLibraryActivity extends BaseLittleWalterActivity implements TextWatcher {
    private EditText mNameEditView;
    private TextView mCategoryName;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_new_resource_library);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
        mNameEditView = (EditText) findViewById(R.id.edit_name);
        mCategoryName = (TextView) findViewById(R.id.category_name);
    }

    protected void initEvents() {
        mNameEditView.addTextChangedListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.confirm:
                saveNewCategory();
                break;
        }
    }

    private void saveNewCategory() {
        if (TextUtils.isEmpty(mCategoryName.getText().toString())) {
            showCustomToast("请输入分类名称");
            return;
        }
        LittleWalterApplication.getCategoryCardsPreferences().putString(mCategoryName.getText().toString(), "");
        finish();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d("zheng", "onTextChanged");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.d("zheng", "beforeTextChanged");
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d("zheng", "afterTextChanged");
        mCategoryName.setText(mNameEditView.getText().toString());
    }
}
