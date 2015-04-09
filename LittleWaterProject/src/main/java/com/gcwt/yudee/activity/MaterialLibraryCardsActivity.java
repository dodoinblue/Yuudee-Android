/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.adapter.ScrollAdapter;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;
import com.gcwt.yudee.widget.ScrollLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 15/3/5.
 */
public class MaterialLibraryCardsActivity extends BaseLittleWaterActivity {
    private boolean mSelectMode;
    private CardItem mMaterialLibraryItem;
    // Container的Adapter
    private ScrollAdapter mItemsAdapter;
    // Container中滑动控件列表
    private ArrayList<CardItem> mSelectedCardItemList;
    private Button mConfirmButton;

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
        mContainer.draggable = false;
        mContainer.getLayoutParams().height = mScreenWidth;
        mContainer.requestLayout();
        //设置Container添加删除Item的回调
//        mContainer.setOnAddPage(this);
        //设置Container页面换转的回调，比如自第一页滑动第二页
//        mContainer.setOnPageChangedListener(this);
        //设置Container编辑模式的回调，长按进入修改模式
//        mContainer.setOnEditModeListener(this);
        mConfirmButton = (Button) findViewById(R.id.confirm);
        mMaterialLibraryItem = (CardItem) getIntent().getSerializableExtra("library");
        mSelectMode = getIntent().getBooleanExtra("select_mode", false);
        if (mSelectMode) {
            findViewById(R.id.material_library_new).setVisibility(View.INVISIBLE);
            mSelectedCardItemList = new ArrayList<CardItem>();
            mConfirmButton.setVisibility(View.VISIBLE);
            mConfirmButton.setEnabled(false);
            mConfirmButton.setAlpha(.6f);
        }
        mCardItemList = LittleWaterUtility.getMaterialLibraryCardsList(mMaterialLibraryItem.name);
        Log.d("zheng", "mCardItemList size:" + mCardItemList.size());
        LittleWaterUtility.sortCardList(mCardItemList);
//        if (!mMaterialLibraryItem.getEditable() && mCardItemList.size() == 0) {
//            mCardItemList = LittleWaterUtility.getCategoryCardsList(mMaterialLibraryItem.name);
//            LittleWaterUtility.sortCardList(mCardItemList);
//        }

        //动态设置Container每页的列数为2行
        mContainer.setColCount(2);
        //动态设置Container每页的行数为2行
        mContainer.setRowCount(2);
        //初始化Container的Adapter
        mItemsAdapter = new ScrollAdapter(mContainer, mCardItemList) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final CardItem moveItem = mList.get(position);
                if (moveItem.getIsEmpty()) {
                    view.setVisibility(View.INVISIBLE);
                } else if (mSelectMode) {
                    final ImageView selectView = (ImageView) view.findViewById(R.id.card_edit);
                    selectView.setImageResource(R.mipmap.box);
                    selectView.setVisibility(View.VISIBLE);
                    selectView.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            CardItem cardItem = mCardItemList.get(position);
                            if (mSelectedCardItemList.contains(cardItem)) {
                                selectView.setImageResource(R.mipmap.box);
                                mSelectedCardItemList.remove(cardItem);
                            } else {
                                selectView.setImageResource(R.mipmap.checkedbox);
                                mSelectedCardItemList.add(cardItem);
                            }

                            if (mSelectedCardItemList.size() == 0) {
                                mConfirmButton.setEnabled(false);
                                mConfirmButton.setAlpha(.6f);
                            } else {
                                mConfirmButton.setAlpha(1f);
                                mConfirmButton.setEnabled(true);
                            }
                        }
                    });
                } else if (moveItem.getEditable()) {
                    final ImageView editView = (ImageView) view.findViewById(R.id.card_edit);
                    editView.setImageResource(R.mipmap.edit);
                    editView.setVisibility(View.VISIBLE);
                    editView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MaterialLibraryCardsActivity.this, EditMaterialLibraryCardActivity.class);
                            intent.putExtra("library_card", moveItem);
                            intent.putExtra("library", mMaterialLibraryItem);
                            startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY_CARD);
                        }
                    });
                }

                return view;
            }
        };
        //设置Adapter
        mContainer.setSaAdapter(mItemsAdapter, this);
        //调用refreView绘制所有的Item
        mContainer.refreshView();
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
                Intent intent = new Intent(this, NewMaterialLibraryCardActivity.class);
                intent.putExtra("material_library", getMaterialLibraryName());
                startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_MATERIAL_LIBRARY_CARD);
                break;
            case R.id.confirm:
                Intent data = new Intent();
                data.putExtra("selected_card_list", mSelectedCardItemList);
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
        }
    }

    @Override
    protected String getMaterialLibraryName() {
        return mMaterialLibraryItem.name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        CardItem cardItem;
        switch (requestCode) {
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_MATERIAL_LIBRARY_CARD:
                cardItem = (CardItem) data.getSerializableExtra("library_card");
                mCardItemList.add(cardItem);
                mContainer.refreshView();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}
