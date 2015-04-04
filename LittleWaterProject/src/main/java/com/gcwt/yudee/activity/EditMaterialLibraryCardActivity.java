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

import java.util.ArrayList;
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
        mCardName.setText(mLibraryCard.getName());
        findViewById(R.id.trash).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.trash:
                Intent data = new Intent();
                data.putExtra("library_card", mLibraryCard);
                data.putExtra("library_deleted", true);
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
            default:
                super.onClick(view);
        }
    }
}
