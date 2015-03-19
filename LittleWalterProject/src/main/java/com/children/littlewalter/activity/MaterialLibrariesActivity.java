/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.pattern.widget.ActionWindow;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.children.littlewalter.BaseLittleWalterActivity;
import com.children.littlewalter.LittleWalterApplication;
import com.children.littlewalter.R;
import com.children.littlewalter.adapter.ScrollAdapter;
import com.children.littlewalter.model.CardItem;
import com.children.littlewalter.widget.ScrollLayout;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by peter on 15/3/5.
 */
public class MaterialLibrariesActivity extends BaseLittleWalterActivity {
    private static final String LOCAL_RESOURCES_DIRECTORY = MainActivity.OUTPUT_DIRECTORY + "/resources";
    // 滑动控件的容器Container
    private ScrollLayout mContainer;
    // Container的Adapter
    private ScrollAdapter mItemsAdapter;
    // Container中滑动控件列表
    private List<CardItem> mCardItemList = new ArrayList<CardItem>();
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
        mContainer.getLayoutParams().height = mScreenWidth;
        mContainer.requestLayout();
        //设置Container添加删除Item的回调
//        mContainer.setOnAddPage(this);
        //设置Container页面换转的回调，比如自第一页滑动第二页
//        mContainer.setOnPageChangedListener(this);
        //设置Container编辑模式的回调，长按进入修改模式
//        mContainer.setOnEditModeListener(this);

        initResourcesInSDcard();

//        CardItem item = new CardItem();
//        item.name = "未分类";
//        item.cover = "";
//        mCardItemList.add(item);
        HashMap<String, String> libraryCoverMap = (HashMap<String, String>) LittleWalterApplication.getCategoryCoverPreferences().getAll();
        getMeterialLibrary(libraryCoverMap, false);

        libraryCoverMap = (HashMap<String, String>) LittleWalterApplication.getMaterialLibraryCoverPreferences().getAll();
        getMeterialLibrary(libraryCoverMap, true);

        //动态设置Container每页的列数为2行
        mContainer.setColCount(MainActivity.LAYOUT_TYPE_2_X_2);
        //动态设置Container每页的行数为2行
        mContainer.setRowCount(MainActivity.LAYOUT_TYPE_2_X_2);
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
                            cardCover = new BitmapDrawable(getBitmapFromSdCard(moveItem.cover));
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
                            startActivity(intent);
                        }
                    });
                    return view;
                } else {
                    return null;
                }
            }
        };
        //设置Adapter
        mContainer.setSaAdapter(mItemsAdapter);
        //调用refreView绘制所有的Item
        mContainer.refreView();
    }

    private void getMeterialLibrary(HashMap<String, String> libraryCoverMap, boolean editable) {
        Set<Map.Entry<String, String>> categoryCoverSet = libraryCoverMap.entrySet();
        for (Map.Entry<String, String> categoryCover : categoryCoverSet) {
            CardItem item = new CardItem();
            item.name = categoryCover.getKey();
            item.cover = categoryCover.getValue();
            item.editable = editable;
            mCardItemList.add(item);
        }
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
                mNewResourceWindow = new ActionWindow(this, findViewById(R.id.material_library_new),
                        R.layout.drop_down_menu_new_material_or_card);
                mNewResourceWindow.dropDown();
                break;
            case R.id.create_card:
                startActivity(NewMaterialLibraryCardActivity.class);
                mNewResourceWindow.dismiss();
                break;
            case R.id.create_library:
                startActivity(NewMaterialLibraryActivity.class);
                mNewResourceWindow.dismiss();
                break;
        }
    }

    private void initResourcesInSDcard() {
        File file = new File(LOCAL_RESOURCES_DIRECTORY);
        //如果目标目录不存在，则创建
        if (!file.exists()) {
            file.mkdirs();
            file = new File(LOCAL_RESOURCES_DIRECTORY + "/未分类");
            file.mkdir();
        }
    }
}