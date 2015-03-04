/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.pattern.AlphabetInfo;
import android.pattern.adapter.BaseListAdapter;
import android.pattern.widget.ActionWindow;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.children.littlewalter.LittleWalterApplication;
import com.children.littlewalter.adapter.ScrollAdapter;
import com.children.littlewalter.model.CardItem;
import com.children.littlewalter.util.LittleWalterConstant;
import com.children.littlewalter.util.UnzipAssets;
import com.children.littlewalter.widget.ScrollLayout;
import com.children.littlewalter.widget.ScrollLayout.OnAddOrDeletePage;
import com.children.littlewalter.widget.ScrollLayout.OnEditModeListener;
import com.children.littlewalter.widget.ScrollLayout.OnPageChangedListener;
import com.children.littlewalter.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by peter on 3/3/15.
 */

@SuppressLint("HandlerLeak")
public class MainActivity extends BaseLittleWalterActivity implements OnAddOrDeletePage,
		OnPageChangedListener, OnEditModeListener {
    private final String OUTPUT_DIRECTORY = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/LittleWalter";
    private final String LOCAL_CARDS_DIRECTORY = OUTPUT_DIRECTORY + "/cards/";
    private static final String FIRST_TIME_ENTER_APP = "first_time_enter_app";
    private String mCurrentCategory;

	// 滑动控件的容器Container
	private ScrollLayout mContainer;

	// Container的Adapter
	private ScrollAdapter mItemsAdapter;
	// Container中滑动控件列表
	private List<CardItem> mList;

    private HashMap<String, ArrayList<CardItem>> mCategoryCardsMap = new HashMap<String, ArrayList<CardItem>>();
    private HashMap<String, String> mCategoryCoverMap = new HashMap<String, String>();
    private boolean mIsInParentMode;
    private ListView mCategoryListView;
    private List<String> mCategoryList;
    private BaseListAdapter mCategoryListAdapter;
    private ActionWindow mDropDownCategoryListWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化控件
		initViews();
        initEvents();
	}

    @Override
	public void initViews() {
		mContainer = (ScrollLayout) findViewById(R.id.container);
        mContainer.getLayoutParams().height = mScreenWidth;
        mContainer.requestLayout();
		//设置Container添加删除Item的回调
		mContainer.setOnAddPage(this);
		//设置Container页面换转的回调，比如自第一页滑动第二页
		mContainer.setOnPageChangedListener(this);
		//设置Container编辑模式的回调，长按进入修改模式
		mContainer.setOnEditModeListener(this);
		//动态设置Container每页的列数为2行
		mContainer.setColCount(2);
		//动态设置Container每页的行数为2行
		mContainer.setRowCount(2);

        mCategoryListAdapter = new BaseListAdapter<String>(this, new ArrayList<String>()) {
            @Override
            public View bindView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.layout_category_list_item, parent, false);
                }
                TextView categoryView = (TextView) convertView;
                categoryView.setText(getItem(position));
                return convertView;
            }
        };
        mCategoryListAdapter.setOnInViewClickListener(R.id.category_name, new BaseListAdapter.OnInternalClickListener() {
            @Override
            public void OnClickListener(View convertView, View clickedView, Integer position, Object value) {
                String category = (String)value;
                String cardsInfoJson = LittleWalterApplication.getCategoryCardsPreferences().getString(category);
                mList = new Gson().fromJson(cardsInfoJson, new TypeToken<List<CardItem>>() { }.getType());
                displayCards();
                if (mDropDownCategoryListWindow != null) {
                    mDropDownCategoryListWindow.dismiss();
                }
            }
        });
	}

    @Override
    protected void initEvents() {
        initLocalCardResources();
    }

    private void getCardsFromCache() {
        mCurrentCategory = LittleWalterApplication.getSettingsPreferences().getString(LittleWalterConstant.SETTINGS_CURRENT_CATEGORY);
        String curCategoryCardsJson = LittleWalterApplication.getCategoryCardsPreferences().getString(mCurrentCategory);
        mList = new Gson().fromJson(curCategoryCardsJson, new TypeToken<List<CardItem>>() { }.getType());
        displayCards();
    }

    private void initLocalCardResources() {
        boolean firstEnterApp = LittleWalterApplication.getSettingsPreferences().getBoolean(FIRST_TIME_ENTER_APP, true);
        if (!firstEnterApp) {
            // 从DB中初始化滑动控件列表
            getCardsFromCache();
            return;
        }
        LittleWalterApplication.getSettingsPreferences().putBoolean(FIRST_TIME_ENTER_APP, false);

        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle("提示");
        dialog.setMessage("正在解压文件，请稍后！");
        dialog.show();//显示对话框
        new Thread(){
            public void run() {
                //在新线程中以同名覆盖方式解压
                try {
                    UnzipAssets.unZip(MainActivity.this, "cards.zip", OUTPUT_DIRECTORY, true);
                    initCardsFromSDcard();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.cancel();//解压完成后关闭对话框
            }
        }.start();
    }

    private void initCardsFromSDcard() {
        File cardResourceFolder = new File(LOCAL_CARDS_DIRECTORY);
        File[] categoryFolders = cardResourceFolder.listFiles();
        for (File categoryFolder : categoryFolders) {
            ArrayList<CardItem> cardList = new ArrayList<CardItem>();
            String category = categoryFolder.getName();
            category = category.split("-")[1];
            mCategoryCardsMap.put(category, cardList);
            if (mCurrentCategory == null) {
                mCurrentCategory = category;
                LittleWalterApplication.getSettingsPreferences().putString(LittleWalterConstant.SETTINGS_CURRENT_CATEGORY, mCurrentCategory);
            }
            File[] cardItemFolders = categoryFolder.listFiles();
            for (File cardItemFolder : cardItemFolders) {
                if (cardItemFolder.isDirectory()) {
                    CardItem item = new CardItem();
                    cardList.add(item);
                    item.setName(cardItemFolder.getName().split("-")[1].split("\\.")[0]);
                    File[] mediaFolders = cardItemFolder.listFiles();
                    for (File mediaFolder : mediaFolders) {
                        if (mediaFolder.getPath().contains("audio")) {
                            File[] audioFiles = mediaFolder.listFiles();
                            String[] audios = new String[audioFiles.length];
                            int i = 0;
                            for (File audioFile : audioFiles) {
                                Log.d("zheng", "audio:" + audioFile.getAbsolutePath());
                                audios[i++] = audioFile.getAbsolutePath();
                            }
                            item.setAudios(audios);
                        } else if (mediaFolder.getPath().contains("image")) {
                            File[] imageFiles = mediaFolder.listFiles();
                            List<String> images = new ArrayList<String>();
                            for (File imageFile : imageFiles) {
                                Log.d("zheng", "image:" + imageFile.getAbsolutePath());
                                images.add(imageFile.getAbsolutePath());
                            }
                            item.setImages(images);
                        }
                    }
                } else {
                    mCategoryCoverMap.put(category, cardItemFolder.getAbsolutePath());
                }
            }
        }

        saveAllCardsIntoCache();
        mList = mCategoryCardsMap.get(mCurrentCategory);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayCards();
            }
        });
    }

    private void saveAllCardsIntoCache() {
        Set<Map.Entry<String, ArrayList<CardItem>>> categoryCardsSet = mCategoryCardsMap.entrySet();
        for (Map.Entry<String, ArrayList<CardItem>> entry : categoryCardsSet) {
            LittleWalterApplication.getCategoryCardsPreferences().putString(entry.getKey(), new Gson().toJson(entry.getValue()));
        }

        Set<Map.Entry<String, String>> categoryCoverSet = mCategoryCoverMap.entrySet();
        for (Map.Entry<String, String> entry : categoryCoverSet) {
            LittleWalterApplication.getCategoryCoverPreferences().putString(entry.getKey(), entry.getValue());
        }
    }

    private void displayCards() {
        //初始化Container的Adapter
        mItemsAdapter = new ScrollAdapter(this, mList);
        //设置Adapter
        mContainer.setSaAdapter(mItemsAdapter);
        //调用refreView绘制所有的Item
        mContainer.refreView();
    }

	private int getDrawableId(String name) {
		return getResources().getIdentifier(name, "drawable", "com.children.littlewalter");
	}

	@Override
	public void onBackPressed() {
		//back键监听，如果在编辑模式，则取消编辑模式
		if (mContainer.isEditting()) {
			mContainer.showEdit(false);
			return;
		} else {
            //退出APP前，保存当前的Items，记得所有item的位置
            List<CardItem> list = mContainer.getAllMoveItems();
            LittleWalterApplication.getCategoryCardsPreferences().putString(mCurrentCategory, new Gson().toJson(list));
			super.onBackPressed();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	@Override
	public void onEdit() {
		Log.e("test", "onEdit");
	}

	@Override
	public void onPage2Other(int former, int current) {
		Log.e("test", "former-->" + former +"  current-->" + current);
	}

	public void onAddOrDeletePage(int page, boolean isAdd) {
		Log.e("test", "page-->" + page +"  isAdd-->" + isAdd);
	}

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.unlock_parent_ui:
                findViewById(R.id.unlock_guide).setVisibility(View.VISIBLE);
                enterParentMode();
                break;
            case R.id.unlock_guide:
                findViewById(R.id.unlock_guide).setVisibility(View.GONE);
                break;
            case R.id.parent_end_edit:
                enterChildMode();
                break;
            case R.id.parent_about_open:
                openAboutLittleWalterWindow();
                break;
            case R.id.parent_settings:
                popUpSettings();
                break;
            case R.id.parent_categories_title:
                showCategoryDropDownWindow();
                break;
            case R.id.parent_resource_library:
                showResourceLibrary();
                break;
            case R.id.about_product_introduction:
                break;
            case R.id.about_train_introduction:
                break;
            case R.id.about_feedback_advice:
                break;
            case R.id.about_export_resource_library:
                break;
            case R.id.about_reset_cards:
                break;
        }
    }

    private void openAboutLittleWalterWindow() {
        ActionWindow window = new ActionWindow(this, findViewById(R.id.parent_about_open), R.layout.layout_about_menu);
        window.popup();
    }

    private void showCategoryDropDownWindow() {
        ViewGroup categoryListLayout = (ViewGroup) mInflater.inflate(R.layout.layout_category_list, null, false);
        mDropDownCategoryListWindow = new ActionWindow(this, findViewById(R.id.parent_categories_title), categoryListLayout);
        mDropDownCategoryListWindow.dropDown();
        mCategoryListView = (ListView) categoryListLayout.findViewById(R.id.category_list);
        mCategoryListView.setAdapter(mCategoryListAdapter);
        mCategoryListAdapter.notifyDataSetChanged();
    }

    private void popUpSettings() {

    }

    private void showResourceLibrary() {

    }

    private void enterParentMode() {
        mIsInParentMode = true;
        findViewById(R.id.parent_top).setVisibility(View.VISIBLE);
        findViewById(R.id.parent_bottom).setVisibility(View.VISIBLE);
        findViewById(R.id.unlock_parent_ui).setVisibility(View.GONE);
        Set<String> categorySet = LittleWalterApplication.getCategoryCoverPreferences().getAll().keySet();
        mCategoryList = new ArrayList<String>(categorySet);
//        Collections.sort(mCategoryList, new WordsComparator());
        mCategoryListAdapter.setList(mCategoryList);
    }

    private void enterChildMode() {
        mIsInParentMode = false;
        findViewById(R.id.parent_top).setVisibility(View.GONE);
        findViewById(R.id.parent_bottom).setVisibility(View.GONE);
        findViewById(R.id.unlock_parent_ui).setVisibility(View.VISIBLE);
    }

    public class WordsComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}
