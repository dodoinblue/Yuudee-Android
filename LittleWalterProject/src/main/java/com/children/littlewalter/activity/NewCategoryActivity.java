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
public class NewCategoryActivity extends BaseLittleWalterActivity {
    private EditText mNameEditView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_new_category);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
        mNameEditView = (EditText) findViewById(R.id.edit_new_category_name);
    }

    protected void initEvents() {
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
        if (TextUtils.isEmpty(mNameEditView.getText().toString())) {
            showCustomToast("请输入新课件名称");
            return;
        }
        String newCategoryName = mNameEditView.getText().toString();
        LittleWalterApplication.getCategoryCardsPreferences().putString(newCategoryName, "");
        Intent intent = new Intent();
        intent.putExtra("result_new_category_name", newCategoryName);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
