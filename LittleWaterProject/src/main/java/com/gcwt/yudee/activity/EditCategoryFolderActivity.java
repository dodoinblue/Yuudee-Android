/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.R;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;

/**
 * Created by peter on 15/4/5.
 */
public class EditCategoryFolderActivity extends BaseLittleWaterActivity {
    private CardItem mCardItem;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_edit_category_folder);
        initViews();
        initEvents();
    }

    @Override
    protected void initViews() {
        setTitle("");
        mCardItem = (CardItem) getIntent().getSerializableExtra("library");
        if (mCardItem.editable) {
            TextView editView = (TextView) findViewById(R.id.edit_card);
            editView.setText("进入素材库编辑该分类 >>");
        }
    }

    @Override
    protected void initEvents() {

    }

    public void onClick(View view) {
        Intent data = new Intent();
        switch (view.getId()) {
            case R.id.parent_settings_delete_category:
                data.putExtra("library_card", mCardItem);
                data.putExtra("library_deleted", true);
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
            case R.id.edit_card:
                if (!mCardItem.editable) {
                    return;
                }

                Intent intent = new Intent(EditCategoryFolderActivity.this, EditMaterialLibraryActivity.class);
                intent.putExtra("library", mCardItem);
                startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY);
                findViewById(R.id.settings_root_container).setVisibility(View.GONE);
                break;
            case R.id.parent_edit_category_folder_cancel:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED && requestCode == LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY) {
            finish();
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY:
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
        }
    }
}
