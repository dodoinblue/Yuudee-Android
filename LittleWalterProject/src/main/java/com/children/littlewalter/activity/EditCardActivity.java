/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import android.os.Bundle;
import android.view.View;

import com.children.littlewalter.BaseLittleWalterActivity;
import com.children.littlewalter.R;

/**
 * Created by peter on 3/10/15.
 */
public class EditCardActivity extends BaseLittleWalterActivity {
    private View mNoNanimationChecked;
    private View mZoomInAnimationChecked;
    private View mZoomInAndRotateAnimationChecked;
    private static final int ANIMATION_NONE = 0;
    private static final int ANIMATION_ZOOM_IN = 1;
    private static final int ANIMATION_ZOOM_IN_AND_ROTATE = 2;
    private int mAnimatonType = ANIMATION_ZOOM_IN;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_edit_card);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
        mNoNanimationChecked = findViewById(R.id.no_animation_checked);
        mZoomInAnimationChecked = findViewById(R.id.zoomin_animation_checked);
        mZoomInAndRotateAnimationChecked = findViewById(R.id.zoomin_and_rotate_animation_checked);
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
            case R.id.no_animation_container:
                mNoNanimationChecked.setVisibility(View.VISIBLE);
                mZoomInAnimationChecked.setVisibility(View.INVISIBLE);
                mZoomInAndRotateAnimationChecked.setVisibility(View.INVISIBLE);
                mAnimatonType = ANIMATION_NONE;
                break;
            case R.id.animation_zoom_in_container:
                mNoNanimationChecked.setVisibility(View.INVISIBLE);
                mZoomInAnimationChecked.setVisibility(View.VISIBLE);
                mZoomInAndRotateAnimationChecked.setVisibility(View.INVISIBLE);
                mAnimatonType = ANIMATION_ZOOM_IN;
                break;
            case R.id.animation_zoomin_and_rotate_container:
                mNoNanimationChecked.setVisibility(View.INVISIBLE);
                mZoomInAnimationChecked.setVisibility(View.INVISIBLE);
                mZoomInAndRotateAnimationChecked.setVisibility(View.VISIBLE);
                mAnimatonType = ANIMATION_ZOOM_IN_AND_ROTATE;
                break;
        }
    }
}
