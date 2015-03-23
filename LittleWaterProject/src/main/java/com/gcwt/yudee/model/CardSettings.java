/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.model;

import com.gcwt.yudee.util.LittleWaterConstant;

import java.io.Serializable;

/**
 * Created by peter on 3/23/15.
 */
public class CardSettings implements Serializable {
    private static final long serialVersionUID = 3388701081007512694L;

    public boolean mute;
    public int animationType = LittleWaterConstant.ANIMATION_ZOOM_IN;

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean getMute() {
        return mute;
    }

    public void setAnimationType(int animationType) {
        this.animationType = animationType;
    }

    public int getAnimationType() {
        return animationType;
    }
}
