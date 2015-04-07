/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.pattern.util.PhotoUtil;
import android.pattern.widget.ActionWindow;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.adapter.ScrollAdapter;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;
import com.gcwt.yudee.widget.ScrollLayout;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by peter on 15/3/5.
 */
public class MaterialLibrariesActivity extends BaseLittleWaterActivity {
    private boolean mSelectMode;
    // Container的Adapter
    private ScrollAdapter mItemsAdapter;
    private ActionWindow mNewResourceWindow;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_material_libraries);
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

        HashMap<String, String> coverMap = (HashMap<String, String>) LittleWaterApplication.getCategoryCoverPreferences().getAll();
        ArrayList<CardItem> categoryCardList = getCardItems(coverMap);

        coverMap = (HashMap<String, String>) LittleWaterApplication.getMaterialLibraryCoverPreferences().getAll();
        mCardItemList = getCardItems(coverMap);
        for (CardItem libraryCard : mCardItemList) {
            if (!categoryCardList.contains(libraryCard) && !TextUtils.equals(libraryCard.getName(), "未分类")) {
                libraryCard.setEditable(true);
            }
        }

        //动态设置Container每页的列数为2行
        mContainer.setColCount(LittleWaterActivity.LAYOUT_TYPE_2_X_2);
        //动态设置Container每页的行数为2行
        mContainer.setRowCount(LittleWaterActivity.LAYOUT_TYPE_2_X_2);
    }

    private ArrayList<CardItem> getCardItems(HashMap<String, String> libraryCoverMap) {
        ArrayList<CardItem> cardItems = new ArrayList<CardItem>();
        Set<Map.Entry<String, String>> categoryCoverSet = libraryCoverMap.entrySet();
        for (Map.Entry<String, String> categoryCover : categoryCoverSet) {
            CardItem item = new CardItem();
            item.name = categoryCover.getKey();
            item.cover = categoryCover.getValue();
            item.isLibrary = true;
            cardItems.add(item);
        }
        return cardItems;
    }

    @Override
    protected void initEvents() {
        mSelectMode = getIntent().getBooleanExtra("select_mode", false);
        if (mSelectMode) {
            findViewById(R.id.material_library_new).setVisibility(View.INVISIBLE);
        }

        //初始化Container的Adapter
        mItemsAdapter = new ScrollAdapter(mContainer, mCardItemList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (position < mList.size()) {
                    final CardItem moveItem = mList.get(position);
                    final View view = mInflater.inflate(R.layout.category_item, parent, false);
                    final ImageView iv = (ImageView) view.findViewById(R.id.content_iv);
                    if (TextUtils.isEmpty(moveItem.cover)) {
                        iv.setImageResource(R.mipmap.default_image);
                    } else {
                        Drawable cardCover = null;
                        SoftReference<Drawable> cover = mCache.get(moveItem.cover);
                        if (cover != null) {
                            cardCover = cover.get();
                        }

                        if (cardCover == null) {
                            cardCover = LittleWaterUtility.getRoundCornerDrawableFromSdCard(moveItem.cover);
                            mCache.put(moveItem.cover, new SoftReference<Drawable>(cardCover));
                        }
                        iv.setImageDrawable(cardCover);
                    }

                    TextView nameView = (TextView) view.findViewById(R.id.card_name);
                    nameView.setText(moveItem.name);

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MaterialLibrariesActivity.this, MaterialLibraryCardsActivity.class);
                            intent.putExtra("library", moveItem);
                            intent.putExtra("select_mode", mSelectMode);
                            startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_ADD_MATERIAL_LIBRARY_CARDS);
                        }
                    });

                    final ImageView editView = (ImageView) view.findViewById(R.id.card_edit);
                    if (mSelectMode) {
                        editView.setVisibility(View.VISIBLE);
                        editView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                ArrayList<CardItem> selectedList = LittleWaterUtility.getMaterialLibraryCardsList(LittleWaterUtility.getCardDisplayName(moveItem.getName()));
//                                Log.d("zheng", "selectedList:" + selectedList.size() + " library name:" + LittleWaterUtility.getCardDisplayName(moveItem.getName()));
                                Intent intent = new Intent();
                                ArrayList<CardItem> selectedList = new ArrayList<CardItem>();
                                selectedList.add(moveItem);
                                intent.putExtra("selected_card_list", selectedList);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        });
                    } else if (moveItem.getEditable()) {
                        editView.setImageResource(R.mipmap.edit);
                        editView.setVisibility(View.VISIBLE);
                        editView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(MaterialLibrariesActivity.this, EditMaterialLibraryActivity.class);
                                intent.putExtra("library", moveItem);
                                startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY);
                            }
                        });
                    }
                    return view;
                } else {
                    return null;
                }
            }
        };
        //设置Adapter
        mContainer.setSaAdapter(mItemsAdapter, this);
        //调用refreView绘制所有的Item
        mContainer.refreshView();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.material_library_back:
                finish();
                break;
            case R.id.material_library_new:
                mNewResourceWindow = new ActionWindow(this, findViewById(R.id.material_library_new),
                        R.layout.drop_down_menu_new_material_or_card);
                mNewResourceWindow.dropDown();
                break;
            case R.id.create_card:
                startActivity(NewMaterialLibraryCardActivity.class);
                mNewResourceWindow.dismiss();
                break;
            case R.id.create_library:
                Intent intent = new Intent(this, NewMaterialLibraryActivity.class);
                startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_MATERIAL_LIBRARY);
                mNewResourceWindow.dismiss();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_MATERIAL_LIBRARY:
                CardItem cardItem = (CardItem) data.getSerializableExtra("library_card");
                mCardItemList.add(cardItem);
                mContainer.refreshView();
                break;
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_ADD_MATERIAL_LIBRARY_CARDS:
                ArrayList<CardItem> selectedList = (ArrayList<CardItem>) data.getSerializableExtra("selected_card_list");
                Intent intent = new Intent();
                intent.putExtra("selected_card_list", selectedList);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
        }
    }
}
