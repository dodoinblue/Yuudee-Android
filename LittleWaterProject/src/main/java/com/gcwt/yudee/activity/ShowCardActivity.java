/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.R;
import com.gcwt.yudee.adapter.ScrollAdapter;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterUtility;

/**
 * Created by peter on 3/10/15.
 */
public class ShowCardActivity extends BaseLittleWaterActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.big_card_item);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
        final CardItem item = (CardItem) getIntent().getSerializableExtra("card_item");
        final ImageView iv = (ImageView) findViewById(R.id.content_iv);
        String coverUrl = item.getImages().get(0);
        Drawable cardCover = LittleWaterUtility.getRoundCornerDrawableFromSdCard(coverUrl);
        iv.setImageDrawable(cardCover);

        TextView nameView = (TextView) findViewById(R.id.card_name);
        nameView.setText(item.getName());

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ScrollAdapter.playCardByDefaultAnimation(ShowCardActivity.this, findViewById(R.id.card_root_view), item);
            }
        }, 500);
    }

    protected void initEvents() {

    }
}
