/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.view.View;

import com.gcwt.yudee.R;
import com.gcwt.yudee.util.LittleWaterUtility;

/**
 * Created by peter on 15/3/31.
 */
public class SubFolderLittleWaterActivity extends LittleWaterActivity {

    @Override
    public void initViews() {
        super.initViews();
        setTitle("");
        findViewById(R.id.root_container).setBackgroundResource(R.mipmap.child_incat_woodbg);
        findViewById(R.id.unlock_parent_ui).setVisibility(View.GONE);
    }

    @Override
    protected void initEvents() {
//        super.initEvents();
        mCardItemList = LittleWaterUtility.getMaterialLibraryCardsList(getIntent().getStringExtra("library"));
        displayCards();
    }
}
