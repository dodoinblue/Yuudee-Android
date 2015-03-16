/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.children.littlewalter.BaseLittleWalterActivity;
import com.children.littlewalter.R;
import com.children.littlewalter.adapter.ScrollAdapter;
import com.children.littlewalter.model.CardItem;

/**
 * Created by peter on 3/10/15.
 */
public class EditCardActivity extends BaseLittleWalterActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_edit_card);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
    }

    protected void initEvents() {

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.confirm:
                finish();
                break;
        }
    }
}
