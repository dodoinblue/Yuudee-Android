/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.os.Bundle;
import android.view.View;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.R;
import com.gcwt.yudee.adapter.ScrollAdapter;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterUtility;
import com.gcwt.yudee.widget.ScrollLayout;

import java.util.List;

/**
 * Created by peter on 15/3/5.
 */
public class MaterialLibraryCardsActivity extends BaseLittleWaterActivity {
    // 滑动控件的容器Container
    private ScrollLayout mContainer;
    // Container的Adapter
    private ScrollAdapter mItemsAdapter;
    // Container中滑动控件列表
    private List<CardItem> mCardItemList;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_material_library_cards);
        initViews();
        initEvents();
    }

    @Override
    protected void initViews() {
        mContainer = (ScrollLayout) findViewById(R.id.container);
        mContainer.getLayoutParams().height = mScreenWidth;
        mContainer.requestLayout();
        //设置Container添加删除Item的回调
//        mContainer.setOnAddPage(this);
        //设置Container页面换转的回调，比如自第一页滑动第二页
//        mContainer.setOnPageChangedListener(this);
        //设置Container编辑模式的回调，长按进入修改模式
//        mContainer.setOnEditModeListener(this);

        CardItem cardItem = (CardItem) getIntent().getSerializableExtra("library");
        if (cardItem.getEditable()) {
            mCardItemList = LittleWaterUtility.getMaterialLibraryCardsList(cardItem.name);
        } else {
            mCardItemList = LittleWaterUtility.getCategoryCardsList(cardItem.name);
        }

        //动态设置Container每页的列数为2行
        mContainer.setColCount(2);
        //动态设置Container每页的行数为2行
        mContainer.setRowCount(2);
        //初始化Container的Adapter
        mItemsAdapter = new ScrollAdapter(mContainer, mCardItemList);
        //设置Adapter
        mContainer.setSaAdapter(mItemsAdapter);
        //调用refreView绘制所有的Item
        mContainer.refreView();
    }

    @Override
    protected void initEvents() {

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.material_library_back:
                finish();
                break;
            case R.id.material_library_new:
                startActivity(NewMaterialLibraryCardActivity.class);
                break;
        }
    }
}
