/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.model;

/**
 * Created by peter on 3/23/15.
 */
public class CardDirectory {
    public String nameWithNo;
    public String name;
    public int orderNo;

    public CardDirectory(String nameWithNo) {
        this.nameWithNo = nameWithNo;
        String[] nameInfo = nameWithNo.split("-");
        orderNo = Integer.valueOf(nameInfo[0]);
        name = nameInfo[1];
    }
}
