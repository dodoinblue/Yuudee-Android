/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import android.os.Bundle;
import android.pattern.adapter.BaseListAdapter;
import android.pattern.widget.ActionWindow;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.children.littlewalter.BaseLittleWalterActivity;
import com.children.littlewalter.LittleWalterApplication;
import com.children.littlewalter.R;
import com.children.littlewalter.model.CardItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by peter on 3/10/15.
 */
public class NewCardActivity extends BaseLittleWalterActivity implements TextWatcher {
    private EditText mNameEditView;
    private TextView mCardName;
    private ViewGroup mCoverContainer;
    private ViewGroup mSoundContainer;
    private ViewGroup mPreviousContainer;
    private ViewGroup mNextContainer;
    private ActionWindow mNewResourceWindow;
    private Button mChooseCategoryBtn;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_new_card);
        initViews();
        initEvents();
    }

    protected void initViews() {
        setTitle("");
        mNameEditView = (EditText) findViewById(R.id.edit_name);
        mCardName = (TextView) findViewById(R.id.card_name);
        mCoverContainer = (ViewGroup) findViewById(R.id.cover_container);
        mSoundContainer = (ViewGroup) findViewById(R.id.sound_container);
        mPreviousContainer = (ViewGroup) findViewById(R.id.previous_container);
        mNextContainer = (ViewGroup) findViewById(R.id.next_container);
        mChooseCategoryBtn = (Button) findViewById(R.id.choose_category);
    }

    protected void initEvents() {
        mNameEditView.addTextChangedListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.next:
                mCoverContainer.setVisibility(View.GONE);
                mSoundContainer.setVisibility(View.VISIBLE);
                mPreviousContainer.setVisibility(View.GONE);
                mNextContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.previous:
                mCoverContainer.setVisibility(View.VISIBLE);
                mSoundContainer.setVisibility(View.GONE);
                mPreviousContainer.setVisibility(View.VISIBLE);
                mNextContainer.setVisibility(View.GONE);
                break;
            case R.id.confirm:
                saveNewCategory();
                break;
            case R.id.camera:
                break;
            case R.id.album:
                break;
            case R.id.choose_category:
                View layoutCategoryList = mInflater.inflate(R.layout.drop_down_menu_new_card_category_list, null);
                mNewResourceWindow = new ActionWindow(this, findViewById(R.id.choose_category), layoutCategoryList);
                mNewResourceWindow.dropDown();

                Set<String> mCategorySet = LittleWalterApplication.getCategoryCoverPreferences().getAll().keySet();
                ArrayList<String> categoryList = new ArrayList<String>(mCategorySet);
                ListView listView = (ListView) layoutCategoryList.findViewById(R.id.list_view);
                BaseListAdapter adapter = new BaseListAdapter<String>(this, categoryList) {
                    @Override
                    public View bindView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = mInflater.inflate(R.layout.layout_new_card_category_list_item, parent, false);
                        }
                        TextView categoryView = (TextView) convertView;
                        categoryView.setText(getItem(position));
                        return convertView;
                    }
                };
                adapter.setOnInViewClickListener(R.id.category_item, new BaseListAdapter.OnInternalClickListener() {
                    @Override
                    public void OnClickListener(View convertView, View clickedView, Integer position, Object value) {
                        mChooseCategoryBtn.setText((String)value);
                        mNewResourceWindow.dismiss();
                    }
                });
                listView.setAdapter(adapter);
                break;
            case R.id.record_sound:
                break;
            case R.id.play_sound:
                break;
            case R.id.delete_sound:
                break;
        }
    }

    private void saveNewCategory() {
        if (TextUtils.isEmpty(mCardName.getText().toString())) {
            showCustomToast("请输入分类名称");
            return;
        }
        LittleWalterApplication.getCategoryCardsPreferences().putString(mCardName.getText().toString(), "");
        finish();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d("zheng", "onTextChanged");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.d("zheng", "beforeTextChanged");
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d("zheng", "afterTextChanged");
        mCardName.setText(mNameEditView.getText().toString());
    }
}
