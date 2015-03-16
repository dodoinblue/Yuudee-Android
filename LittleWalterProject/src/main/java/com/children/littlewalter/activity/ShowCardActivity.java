/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.children.littlewalter.BaseLittleWalterActivity;
import com.children.littlewalter.R;
import com.children.littlewalter.adapter.ScrollAdapter;
import com.children.littlewalter.model.CardItem;

import java.util.List;

/**
 * Created by peter on 3/10/15.
 */
public class ShowCardActivity extends BaseLittleWalterActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.card_item);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
        CardItem item = (CardItem) getIntent().getSerializableExtra("card_item");
        final ImageView iv = (ImageView) findViewById(R.id.content_iv);
        String coverUrl = item.getImages().get(0);
        Drawable cardCover = new BitmapDrawable(ScrollAdapter.getBitmapFromSdCard(coverUrl));
        iv.setImageDrawable(cardCover);

        TextView nameView = (TextView) findViewById(R.id.card_name);
        nameView.setText(item.getName());

        ScrollAdapter.playCardByDefaultAnimation(this, findViewById(R.id.card_root_view), item);
    }

    protected void initEvents() {

    }
}
