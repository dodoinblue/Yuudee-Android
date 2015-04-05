/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.util;

import android.os.Environment;

/**
 * Created by peter on 3/4/15.
 */
public class LittleWaterConstant {
    // About SD card folders
    public static final String LITTLE_WALTER_DIRECTORY = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/LittleWalter";
    public static final String CATEGORIES_DIRECTORY = LITTLE_WALTER_DIRECTORY + "/cards";
    public static final String MATERIAL_LIBRARIES_DIRECTORY = LITTLE_WALTER_DIRECTORY + "/material_libraries/";

    // About Settings
    public static final String FIRST_TIME_ENTER_APP = "first_time_enter_app";
    public static final String SETTINGS_CURRENT_CATEGORY = "current_category";
    public static final String SETTINGS_GUIDE_REMIND = "guide_remind";

    // About activity request code
    public static final int ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY_CARD = 1000;
    public static final int ACTIVITY_REQUEST_CODE_NEW_CATEGORY_CARD = 1001;
    public static final int ACTIVITY_REQUEST_CODE_NEW_MATERIAL_LIBRARY = 1002;
    public static final int ACTIVITY_REQUEST_CODE_NEW_MATERIAL_LIBRARY_CARD = 1003;
    public static final int ACTIVITY_REQUEST_CODE_ADD_MATERIAL_LIBRARY_CARDS = 1004;
    public static final int ACTIVITY_REQUEST_CODE_EDIT_CATEGORY_CARD_SETTINGS = 1005;
    public static final int ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY = 1006;
    public static final int ACTIVITY_REQUEST_CODE_EDIT_CATEGORY_FOLDER = 1007;

    // Animation types
    public static final int ANIMATION_NONE = 0;
    public static final int ANIMATION_ZOOM_IN = 1;
    public static final int ANIMATION_ZOOM_IN_AND_ROTATE = 2;
}
