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
import android.widget.EditText;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;

/**
 * Created by peter on 3/10/15.
 */
public class NewCategoryActivity extends BaseLittleWaterActivity {
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
        LittleWaterApplication.getCategoryCardsPreferences().putString(newCategoryName, "");
        Intent intent = new Intent();
        intent.putExtra("result_new_category_name", newCategoryName);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
