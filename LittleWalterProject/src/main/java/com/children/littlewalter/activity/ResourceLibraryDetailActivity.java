/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import android.os.Bundle;
import android.view.View;

import com.children.littlewalter.BaseLittleWalterActivity;
import com.children.littlewalter.LittleWalterApplication;
import com.children.littlewalter.R;
import com.children.littlewalter.adapter.ScrollAdapter;
import com.children.littlewalter.model.CardItem;
import com.children.littlewalter.widget.ScrollLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 15/3/5.
 */
public class ResourceLibraryDetailActivity extends BaseLittleWalterActivity {
    // 滑动控件的容器Container
    private ScrollLayout mContainer;
    // Container的Adapter
    private ScrollAdapter mItemsAdapter;
    // Container中滑动控件列表
    private List<CardItem> mCardItemList;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_resource_library_detail);
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

        String category = getIntent().getStringExtra("category");
        if ("未分类".equals(category)) {
            mCardItemList = new ArrayList<CardItem>();
        } else {
            mCardItemList = MainActivity.getCategoryCardsList(category);
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
            case R.id.resource_library_back:
                finish();
                break;
            case R.id.resource_library_new:
                startActivity(NewCardActivity.class);
                break;
        }
    }
}
