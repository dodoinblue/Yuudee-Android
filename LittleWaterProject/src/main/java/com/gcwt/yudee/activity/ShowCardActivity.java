/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.gcwt.yudee.R;
import com.gcwt.yudee.adapter.ScrollAdapter;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;

/**
 * Created by peter on 3/10/15.
 */
public class ShowCardActivity extends Activity {
    private CardItem mCardItem;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.big_card_item);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
        mCardItem = (CardItem) getIntent().getSerializableExtra("card_item");
        final ImageView iv = (ImageView) findViewById(R.id.content_iv);
        String coverUrl = mCardItem.getImages().get(0);
        Drawable cardCover = LittleWaterUtility.getRoundCornerDrawableFromSdCard(coverUrl);
        iv.setImageDrawable(cardCover);

        TextView nameView = (TextView) findViewById(R.id.card_name);
        nameView.setText(LittleWaterUtility.getCardDisplayName(mCardItem.getName()));
    }

    protected void initEvents() {
        switch (mCardItem.getCardSettings().getAnimationType()) {
            case LittleWaterConstant.ANIMATION_ZOOM_IN:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LittleWaterUtility.playCardByFlippingAnimation(ShowCardActivity.this, findViewById(R.id.card_root_view), mCardItem);
                    }
                }, 200);
                break;
            case LittleWaterConstant.ANIMATION_ZOOM_IN_AND_ROTATE:
                LittleWaterUtility.playCardByRotateAndFlippingAnimation(this, findViewById(R.id.card_root_view), mCardItem);
        }
    }
}
