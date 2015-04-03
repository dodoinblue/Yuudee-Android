/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.pattern.adapter.BaseListAdapter;
import android.pattern.util.FileUtils;
import android.pattern.widget.ActionWindow;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.adapter.ScrollAdapter;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.BlurUtility;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;
import com.gcwt.yudee.util.UnzipAssets;
import com.gcwt.yudee.widget.ScrollLayout;
import com.gcwt.yudee.widget.ScrollLayout.OnAddOrDeletePage;
import com.gcwt.yudee.widget.ScrollLayout.OnEditModeListener;
import com.gcwt.yudee.widget.ScrollLayout.OnPageChangedListener;
import com.google.gson.Gson;
import com.umeng.fb.FeedbackAgent;

/**
 * Created by peter on 3/3/15.
 */

@SuppressLint("HandlerLeak")
public class LittleWaterActivity extends BaseLittleWaterActivity implements OnAddOrDeletePage,
		OnPageChangedListener, OnEditModeListener {
    private EditText mCategoryNameEdit;
    private long mFirstPressBackTime;
    private String mCurrentCategory;
    public static final int LAYOUT_TYPE_1_X_1 = 1;
    public static final int LAYOUT_TYPE_2_X_2 = 2;
    private int mCurrentCategoryCardLayoutSetting = LAYOUT_TYPE_2_X_2;
    private boolean mNeedGuideRemind = true;
	// 滑动控件的容器Container
	protected ScrollLayout mContainer;

	// Container的Adapter
	private ScrollAdapter mItemsAdapter;
	// Container中滑动控件列表
	protected List<CardItem> mCardItemList;

    private HashMap<String, ArrayList<CardItem>> mCategoryCardsMap = new HashMap<String, ArrayList<CardItem>>();
    private HashMap<String, String> mCategoryCoverMap = new HashMap<String, String>();
    public static boolean mIsInParentMode;
    private ListView mCategoryListView;
    private List<String> mCategoryList;
    private BaseListAdapter mCategoryListAdapter;
    private ActionWindow mDropDownCategoryListWindow;
    private ActionWindow mSettingsActionWindow;
    private ActionWindow mProductIntroductionWindow;
    private ActionWindow mTrainingIntroductionWindow;
    private ActionWindow mAboutMenuwindow;
    private ActionWindow mNewCategorywindow;
    private TextView mParentCategoryContent;
    private ViewGroup mParentSettingsLayout;
    private ViewGroup mNewCategoryLayout;
    private HashMap<Integer, Boolean> mEnterParentCheckMap = new HashMap<Integer, Boolean>();
    private View.OnTouchListener mViewFlipperOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mEnterParentCheckMap.put(v.getId(), true);
                    if (mEnterParentCheckMap.size() == 3) {
                        Collection<Boolean> downValues = mEnterParentCheckMap.values();
                        boolean tripleDown = true;
                        for (Boolean down : downValues) {
                            if (!down) {
                                tripleDown = false;
                                break;
                            }
                        }
                        if (tripleDown && !mIsInParentMode) {
                            enterParentMode();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mEnterParentCheckMap.put(v.getId(), false);
                    break;
            }
            return true;
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_little_water);
		// 初始化控件
		initViews();
        initEvents();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_CARD_SETTINGS:
                CardItem cardItem = (CardItem) data.getSerializableExtra("card_item");
                int pos = 0;
                for (CardItem item : mCardItemList) {
                    if (TextUtils.equals(item.getName(), cardItem.getName())) {
                        mCardItemList.set(pos, cardItem);
                        LittleWaterUtility.setCategoryCardsList(mCurrentCategory, mCardItemList);
                        Log.d("zheng", "onActivityResult ACTIVITY_REQUEST_CODE_EDIT_CARD_SETTINGS");
                        break;
                    }
                    pos++;
                }
                mContainer.refreshView();
                mContainer.showEdit(true);
                break;
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_CATEGORY_CARD:
                ArrayList<CardItem> selectedList = (ArrayList<CardItem>) data.getSerializableExtra("selected_card_list");
                for (CardItem item : selectedList) {
                    if (!mCardItemList.contains(item)) {
                        mCardItemList.addAll(selectedList);
                    }
                }

                LittleWaterUtility.setCategoryCardsList(mCurrentCategory, mCardItemList);
                mContainer.refreshView();
                break;
        }
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
        mCategoryListAdapter.setOnInViewClickListener(R.id.library_name, new BaseListAdapter.OnInternalClickListener() {
            @Override
            public void OnClickListener(View convertView, View clickedView, Integer position, Object value) {
                mCurrentCategory = (String)value;
                LittleWaterApplication.getAppSettingsPreferences().putString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY, mCurrentCategory);
                mCurrentCategoryCardLayoutSetting = LittleWaterApplication.getCategorySettingsPreferences().getInt(mCurrentCategory, LAYOUT_TYPE_2_X_2);
                mCardItemList = LittleWaterUtility.getCategoryCardsList(mCurrentCategory);
                displayCards();
                if (mDropDownCategoryListWindow != null) {
                    mDropDownCategoryListWindow.dismiss();
                }
            }
        });
        mParentCategoryContent = (TextView) findViewById(R.id.parent_category_content);

        findViewById(R.id.flipper1).setOnTouchListener(mViewFlipperOnTouchListener);
        findViewById(R.id.flipper2).setOnTouchListener(mViewFlipperOnTouchListener);
        findViewById(R.id.flipper3).setOnTouchListener(mViewFlipperOnTouchListener);

        if (mIsInParentMode) {
            enterParentMode();
        } else {
            enterChildMode();
        }
	}

    @Override
    protected void initEvents() {
        initLocalCardResources();
    }

    private void getCardsFromCache() {
        mNeedGuideRemind = LittleWaterApplication.getAppSettingsPreferences().getBoolean(LittleWaterConstant.SETTINGS_GUIDE_REMIND, true);
        mCurrentCategory = LittleWaterApplication.getAppSettingsPreferences().getString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY);
        mCurrentCategoryCardLayoutSetting = LittleWaterApplication.getCategorySettingsPreferences().getInt(mCurrentCategory, LAYOUT_TYPE_2_X_2);

        mCardItemList = LittleWaterUtility.getCategoryCardsList(mCurrentCategory);
        displayCards();
    }

    private void initLocalCardResources() {
        boolean firstEnterApp = LittleWaterApplication.getAppSettingsPreferences().getBoolean(LittleWaterConstant.FIRST_TIME_ENTER_APP, true);
        if (!firstEnterApp) {
            // 从DB中初始化滑动控件列表
            getCardsFromCache();
            return;
        }
        LittleWaterApplication.getAppSettingsPreferences().putBoolean(LittleWaterConstant.FIRST_TIME_ENTER_APP, false);
        FileUtils.delFolder(LittleWaterConstant.LITTLE_WALTER_DIRECTORY);

        final ProgressDialog dialog = new ProgressDialog(LittleWaterActivity.this);
        dialog.setTitle("提示");
        dialog.setMessage("正在解压文件，请稍后！");
        dialog.show();//显示对话框
        new Thread(){
            public void run() {
                //在新线程中以同名覆盖方式解压
                try {
                    UnzipAssets.unZip(LittleWaterActivity.this, "cards.zip", LittleWaterConstant.LITTLE_WALTER_DIRECTORY, true);
                    initCardsFromSDcard();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.cancel();//解压完成后关闭对话框
            }
        }.start();
    }

    private void initCardsFromSDcard() {
        File cardResourceFolder = new File(LittleWaterConstant.CATEGORIES_DIRECTORY);
        File[] categoryFolders = cardResourceFolder.listFiles();
        for (File categoryFolder : categoryFolders) {
            ArrayList<CardItem> cardList = new ArrayList<CardItem>();
            String category = categoryFolder.getName();
            category = category.split("-")[1];
            mCategoryCardsMap.put(category, cardList);
            if (mCurrentCategory == null) {
                mCurrentCategory = category;
                LittleWaterApplication.getAppSettingsPreferences().putString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY, mCurrentCategory);
            }
            File[] cardItemFolders = categoryFolder.listFiles();
            for (File cardItemFolder : cardItemFolders) {
                if (cardItemFolder.isDirectory()) {
                    CardItem item = new CardItem();
                    cardList.add(item);
//                    item.setName(cardItemFolder.getName().split("-")[1].split("\\.")[0]);
                    item.setName(cardItemFolder.getName().split("\\.")[0]);
                    File[] mediaFolders = cardItemFolder.listFiles();
                    for (File mediaFolder : mediaFolders) {
                        if (mediaFolder.getPath().contains("audio")) {
                            File[] audioFiles = mediaFolder.listFiles();
                            List<String> audios = new ArrayList<String>();
                            for (File audioFile : audioFiles) {
                                Log.d("zheng", "audio:" + audioFile.getAbsolutePath());
                                audios.add(audioFile.getAbsolutePath());
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
        mCardItemList = mCategoryCardsMap.get(mCurrentCategory);
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
            LittleWaterApplication.getCategoryCardsPreferences().putString(entry.getKey(), new Gson().toJson(entry.getValue()));
        }

        Set<Map.Entry<String, String>> categoryCoverSet = mCategoryCoverMap.entrySet();
        for (Map.Entry<String, String> entry : categoryCoverSet) {
            LittleWaterApplication.getCategoryCoverPreferences().putString(entry.getKey(), entry.getValue());
        }
    }

    protected void displayCards() {
        addEmptyCardItems();
        validateCardsEffectiveness();
        //动态设置Container每页的列数为2行
        mContainer.setColCount(mCurrentCategoryCardLayoutSetting);
        //动态设置Container每页的行数为2行
        mContainer.setRowCount(mCurrentCategoryCardLayoutSetting);
        //初始化Container的Adapter
        Collections.sort(mCardItemList, new CardItemComparator());
        mItemsAdapter = new ScrollAdapter(mContainer, mCardItemList);
        //设置Adapter
        mContainer.setSaAdapter(mItemsAdapter);
        //调用refreView绘制所有的Item
        mContainer.refreshView();
        mParentCategoryContent.setText(mCurrentCategory);
    }

    private void validateCardsEffectiveness() {
        for (CardItem item : mCardItemList) {
            if (item.getIsEmpty()) {
                continue;
            }
            File file;
            if (item.isLibraryFolder) {
                file = new File(item.getCover());
            } else {
                file = new File(item.getImages().get(0));
            }
            if (!file.exists()) {
                mCardItemList.remove(item);
            }
        }
        if (!(this instanceof SubFolderLittleWaterActivity)) {
            LittleWaterUtility.setCategoryCardsList(mCurrentCategory, mCardItemList);
        }
    }

	@Override
	public void onBackPressed() {
        if (this instanceof SubFolderLittleWaterActivity) {
            super.onBackPressed();
            return;
        }
		//back键监听，如果在编辑模式，则取消编辑模式
		if (mIsInParentMode) {
			enterChildMode();
			return;
		} else {
            if (mFirstPressBackTime + 2000 > System.currentTimeMillis()) {
                //退出APP前，保存当前的Items，记得所有item的位置
                List<CardItem> list = mContainer.getAllMoveItems();
                LittleWaterApplication.getCategoryCardsPreferences().putString(mCurrentCategory, new Gson().toJson(list));
                super.onBackPressed();
                android.os.Process.killProcess(android.os.Process.myPid());
            } else {
                showCustomToast("再按一次退出程序");
            }
            mFirstPressBackTime = System.currentTimeMillis();
		}
	}

	@Override
	public void onEdit() {
		Log.e("test", "onEdit");
	}

	@Override
	public void onPage2Other(int former, int current) {
		Log.e("zheng", "former-->" + former +"  current-->" + current);
	}

	public void onAddOrDeletePage(int page, boolean isAdd) {
		Log.e("zheng", "page-->" + page +"  isAdd-->" + isAdd);
	}

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.unlock_parent_ui:
                showUnloackParentUIRemind();
                break;
            case R.id.unlock_guide_ok:
                findViewById(R.id.unlock_guide).setVisibility(View.GONE);
                break;
            case R.id.unlock_guide_never_remind:
                findViewById(R.id.unlock_guide).setVisibility(View.GONE);
                LittleWaterApplication.getAppSettingsPreferences().putBoolean(LittleWaterConstant.SETTINGS_GUIDE_REMIND, false);
                mNeedGuideRemind = false;
                break;
            case R.id.parent_end_edit:
                enterChildMode();
                break;
            case R.id.parent_about_open:
                showAboutLittleWalterWindow();
                break;
            case R.id.parent_settings:
                popUpSettings();
                break;
            case R.id.parent_settings_cancel:
                mSettingsActionWindow.dismiss();
                break;
            case R.id.parent_settings_finish:
                settingsFinishEdit();
                break;
            case R.id.parent_settings_delete_category:
                settingsDeleteCategory();
                break;
            case R.id.parent_settings_title_category:
                if (mParentSettingsLayout != null) {
                    mParentSettingsLayout.findViewById(R.id.parent_settings_title_category).setVisibility(View.GONE);
                    mParentSettingsLayout.findViewById(R.id.parent_settings_edit_category_name).setVisibility(View.VISIBLE);
                    mParentSettingsLayout.findViewById(R.id.parent_settings_edit_category_name).requestFocus();
                }
                break;
            case R.id.parent_settings_layout1_1:
                if (mParentSettingsLayout != null) {
                    mParentSettingsLayout.findViewById(R.id.parent_settings_layout1_1_checked).setVisibility(View.VISIBLE);
                    mParentSettingsLayout.findViewById(R.id.parent_settings_layout2_2_checked).setVisibility(View.GONE);
                }
                break;
            case R.id.parent_settings_layout2_2:
                if (mParentSettingsLayout != null) {
                    mParentSettingsLayout.findViewById(R.id.parent_settings_layout1_1_checked).setVisibility(View.GONE);
                    mParentSettingsLayout.findViewById(R.id.parent_settings_layout2_2_checked).setVisibility(View.VISIBLE);
                }
                break;
            case R.id.parent_categories_title:
                showCategoryDropDownWindow();
                break;
            case R.id.parent_resource_library:
                showResourceLibrary();
                break;
            case R.id.parent_add_new_category:
//                Intent intent = new Intent(this, NewCategoryActivity.class);
//                startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_CATEGORY);
                showAddNewCategoryWindow();
                break;
            case R.id.parent_add_new_category_confirm:
                saveNewCategory();
                mNewCategorywindow.dismiss();
                break;
            case R.id.parent_add_new_category_cancel:
                mNewCategorywindow.dismiss();
                break;
            case R.id.about_product_introduction:
                showProductIntroductionWindow();
//                mAboutMenuwindow.dismiss();
                break;
            case R.id.about_train_introduction:
                showTrainingIntroductionWindow();
//                mAboutMenuwindow.dismiss();
                break;
            case R.id.about_feedback_advice:
                FeedbackAgent agent = new FeedbackAgent(this);
                agent.startFeedbackActivity();
//                mAboutMenuwindow.dismiss();
                break;
            case R.id.about_export_resource_library:
//                mAboutMenuwindow.dismiss();
                break;
            case R.id.about_reset_cards:
//                mAboutMenuwindow.dismiss();
                break;
            case R.id.about_train_introduction_window_quit:
                mTrainingIntroductionWindow.dismiss();
                break;
            case R.id.about_product_introduction_window_quit:
                mProductIntroductionWindow.dismiss();
                break;
            case R.id.card_edit:
                Intent in = new Intent(this, EditCategoryCardSettingsActivity.class);
                in.putExtra("card_item", (CardItem) view.getTag());
                startActivityForResult(in, LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_CARD_SETTINGS);
                break;
            case R.id.add_new_card:
                Intent newCardIntent = new Intent(this, NewCategoryCardActivity.class);
                newCardIntent.putExtra("current_category", mCurrentCategory);
                startActivityForResult(newCardIntent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_CATEGORY_CARD);
                break;
        }
    }

    private void showUnloackParentUIRemind() {
        if (mNeedGuideRemind) {
            findViewById(R.id.unlock_guide).setVisibility(View.VISIBLE);
        } else {
//            findViewById(R.id.unlock_guide_flicker).setVisibility(View.VISIBLE);
            final ViewFlipper flipper1 = (ViewFlipper) findViewById(R.id.flipper1);
            flipper1.startFlipping();
            final ViewFlipper flipper2 = (ViewFlipper) findViewById(R.id.flipper2);
            flipper2.startFlipping();
            final ViewFlipper flipper3 = (ViewFlipper) findViewById(R.id.flipper3);
            flipper3.startFlipping();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    flipper1.stopFlipping();
                    flipper2.stopFlipping();
                    flipper3.stopFlipping();
//                    findViewById(R.id.unlock_guide_flicker).setVisibility(View.INVISIBLE);
                }
            }, 1000);
        }
    }

    private void settingsDeleteCategory() {
        LittleWaterApplication.getCategoryCardsPreferences().remove(mCurrentCategory);
        mCategoryList.remove(mCurrentCategory);
        mCurrentCategory = mCategoryList.get(0);
        LittleWaterApplication.getAppSettingsPreferences().putString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY, mCurrentCategory);
        mCardItemList = LittleWaterUtility.getCategoryCardsList(mCurrentCategory);
        displayCards();
        mSettingsActionWindow.dismiss();
    }

    private void settingsFinishEdit() {
        String newCategoryName = mCategoryNameEdit.getEditableText().toString();
        if (!TextUtils.equals(newCategoryName, mCurrentCategory)) {
            updateCategoryNameInPreferences(mCurrentCategory, newCategoryName);
            mCategoryList.set(mCategoryList.indexOf(mCurrentCategory), newCategoryName);
            mCategoryListAdapter.notifyDataSetChanged();
            mCurrentCategory = newCategoryName;
            mParentCategoryContent.setText(mCurrentCategory);
        }

        if (mParentSettingsLayout != null) {
            int visibility = mParentSettingsLayout.findViewById(R.id.parent_settings_layout1_1_checked).getVisibility();
            int newCategorySetting = (visibility == View.VISIBLE) ? LAYOUT_TYPE_1_X_1 : LAYOUT_TYPE_2_X_2;
            if (mCurrentCategoryCardLayoutSetting != newCategorySetting) {
                mCurrentCategoryCardLayoutSetting = newCategorySetting;
                LittleWaterApplication.getCategorySettingsPreferences().putInt(mCurrentCategory, mCurrentCategoryCardLayoutSetting);
                mContainer.setColCount(mCurrentCategoryCardLayoutSetting);
                mContainer.setRowCount(mCurrentCategoryCardLayoutSetting);
                displayCards();
            }
        }

        mSettingsActionWindow.dismiss();
    }

    private void updateCategoryNameInPreferences(String oldCategoryName, String newCategoryName) {
        LittleWaterApplication.getAppSettingsPreferences().putString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY, newCategoryName);

        String cateoryCardListStr = LittleWaterApplication.getCategoryCardsPreferences().getString(oldCategoryName);
        LittleWaterApplication.getCategoryCardsPreferences().remove(oldCategoryName);
        LittleWaterApplication.getCategoryCardsPreferences().putString(newCategoryName, cateoryCardListStr);

        String cateoryCoverStr = LittleWaterApplication.getCategoryCoverPreferences().getString(oldCategoryName);
        LittleWaterApplication.getCategoryCoverPreferences().remove(oldCategoryName);
        LittleWaterApplication.getCategoryCoverPreferences().putString(newCategoryName, cateoryCoverStr);

        int cateorySetting = LittleWaterApplication.getCategorySettingsPreferences().getInt(oldCategoryName, LAYOUT_TYPE_2_X_2);
        LittleWaterApplication.getCategorySettingsPreferences().remove(oldCategoryName);
        LittleWaterApplication.getCategorySettingsPreferences().putInt(newCategoryName, cateorySetting);
    }

    private void saveNewCategory() {
        EditText categoryNameEditView = (EditText) mNewCategoryLayout.findViewById(R.id.edit_new_category_name);
        if (TextUtils.isEmpty(categoryNameEditView.getText().toString())) {
            showCustomToast("请输入新课件名称");
            return;
        }
        mCurrentCategory = categoryNameEditView.getText().toString();
        LittleWaterApplication.getCategoryCardsPreferences().putString(mCurrentCategory, "");
        mCardItemList = LittleWaterUtility.getCategoryCardsList(mCurrentCategory);
        LittleWaterUtility.setCategoryCardsList(mCurrentCategory, mCardItemList);
        LittleWaterApplication.getAppSettingsPreferences().putString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY, mCurrentCategory);

        displayCards();
        mDropDownCategoryListWindow.dismiss();
    }

    private void showAddNewCategoryWindow() {
        mNewCategoryLayout = (ViewGroup) mInflater.inflate(R.layout.action_window_new_category, null);
        mNewCategorywindow = new ActionWindow(this, findViewById(R.id.parent_settings), mNewCategoryLayout);
        mNewCategorywindow.setWindowHeight(WindowManager.LayoutParams.MATCH_PARENT);
        mNewCategorywindow.setWindowWidth(WindowManager.LayoutParams.MATCH_PARENT);
        mNewCategorywindow.popup(Gravity.CENTER);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mNewCategoryLayout.findViewById(R.id.settings_root_container).setBackground(new BitmapDrawable(getScreenshot()));
                BlurUtility.blur(LittleWaterActivity.this, mNewCategoryLayout.findViewById(R.id.settings_root_container));
            }
        }, 100);
    }

    private void showAboutLittleWalterWindow() {
//        mAboutMenuwindow = new ActionWindow(this, findViewById(R.id.parent_about_open), R.layout.layout_about_menu);
//        mAboutMenuwindow.popup(Gravity.LEFT|Gravity.BOTTOM);
        View menuContainer = findViewById(R.id.about_menu_container);
        if (menuContainer.getVisibility() == View.VISIBLE) {
            menuContainer.setVisibility(View.INVISIBLE);
        } else {
            menuContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showProductIntroductionWindow() {
        mProductIntroductionWindow = new ActionWindow(this, findViewById(R.id.parent_about_open), R.layout.action_window_product_introduction);
        mProductIntroductionWindow.popup();
    }

    private void showTrainingIntroductionWindow() {
        mTrainingIntroductionWindow = new ActionWindow(this, findViewById(R.id.parent_about_open), R.layout.action_window_training_introduction);
        mTrainingIntroductionWindow.popup();
    }

    private void showCategoryDropDownWindow() {
        final ViewGroup categoryListLayout = (ViewGroup) mInflater.inflate(R.layout.layout_category_list, null, false);
        mDropDownCategoryListWindow = new ActionWindow(this, findViewById(R.id.parent_categories_title), categoryListLayout);
        mDropDownCategoryListWindow.dropDown();
        final ImageView spinnerTitleView = (ImageView) findViewById(R.id.spinner_title);
        spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titlefoldbtn);
        mDropDownCategoryListWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titleunfoldbtn);
                mParentCategoryContent.setText(mCurrentCategory);
            }
        });
        mCategoryListView = (ListView) categoryListLayout.findViewById(R.id.category_list);
        mCategoryListView.setAdapter(mCategoryListAdapter);
    }

    private void popUpSettings() {
        mParentSettingsLayout = (ViewGroup) mInflater.inflate(R.layout.action_window_parent_settings, null);
        mSettingsActionWindow = new ActionWindow(this, findViewById(R.id.parent_settings), mParentSettingsLayout);
        mSettingsActionWindow.setWindowHeight(WindowManager.LayoutParams.MATCH_PARENT);
        mSettingsActionWindow.setWindowWidth(WindowManager.LayoutParams.MATCH_PARENT);
        mSettingsActionWindow.popup(Gravity.CENTER);
        mCategoryNameEdit = (EditText) mParentSettingsLayout.findViewById(R.id.parent_settings_edit_category_name);
        TextView categoryNameView = (TextView) mParentSettingsLayout.findViewById(R.id.parent_settings_title_category);
        categoryNameView.setText(mCurrentCategory);
        mCategoryNameEdit.setText(mCurrentCategory);
        if (mCurrentCategoryCardLayoutSetting == LAYOUT_TYPE_1_X_1) {
            mParentSettingsLayout.findViewById(R.id.parent_settings_layout1_1_checked).setVisibility(View.VISIBLE);
            mParentSettingsLayout.findViewById(R.id.parent_settings_layout2_2_checked).setVisibility(View.INVISIBLE);
        } else {
            mParentSettingsLayout.findViewById(R.id.parent_settings_layout1_1_checked).setVisibility(View.INVISIBLE);
            mParentSettingsLayout.findViewById(R.id.parent_settings_layout2_2_checked).setVisibility(View.VISIBLE);
        }

        mParentSettingsLayout.findViewById(R.id.settings_root_container).setBackground(new BitmapDrawable(getScreenshot()));
        BlurUtility.blur(LittleWaterActivity.this, mParentSettingsLayout.findViewById(R.id.settings_root_container));
    }

    private Bitmap getScreenshot() {
        View view = getWindow().getDecorView();
        Display display = getWindowManager().getDefaultDisplay();
        view.layout(0, 0, display.getWidth(), display.getHeight());
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void showResourceLibrary() {
        startActivity(MaterialLibrariesActivity.class);
    }

    private void enterParentMode() {
        mIsInParentMode = true;
        if (this instanceof SubFolderLittleWaterActivity) {
            findViewById(R.id.parent_top).setVisibility(View.GONE);
            findViewById(R.id.parent_bottom).setVisibility(View.GONE);
        } else {
            findViewById(R.id.parent_top).setVisibility(View.VISIBLE);
            findViewById(R.id.parent_bottom).setVisibility(View.VISIBLE);
            findViewById(R.id.unlock_parent_ui).setVisibility(View.GONE);
            findViewById(R.id.root_container).setBackgroundResource(R.mipmap.background2);
        }
        Set<String> categorySet = LittleWaterApplication.getCategoryCardsPreferences().getAll().keySet();
        mCategoryList = new ArrayList<String>(categorySet);
        mCategoryListAdapter.setList(mCategoryList);
        if (mItemsAdapter != null) {
            mContainer.refreshView();
        }
        mContainer.showEdit(true);
    }

    private void enterChildMode() {
        mIsInParentMode = false;
        findViewById(R.id.parent_top).setVisibility(View.GONE);
        findViewById(R.id.parent_bottom).setVisibility(View.GONE);
        findViewById(R.id.unlock_parent_ui).setVisibility(View.VISIBLE);
        findViewById(R.id.root_container).setBackgroundResource(R.mipmap.background);
        if (mItemsAdapter != null) {
            mContainer.refreshView();
        }
        mContainer.showEdit(false);
    }

    private static class CardNameComparator implements Comparator<String> {
        @Override
        public int compare(String obj1, String obj2) {
            return obj1.compareTo(obj2);
        }
    }

    private static class CardItemComparator implements Comparator<CardItem> {
        @Override
        public int compare(CardItem obj1, CardItem obj2) {
            if (obj1.getName() == null) {
                return 1;
            }
            if (obj2.getName() == null) {
                return -1;
            }
            return obj1.getName().compareTo(obj2.getName());
        }
    }

    /**
     * 判断是否需要为当前的卡片列表增加空的卡片页, 如果需要就增加之, 在不确定是否要增加空卡片时可以调用此方法
     */
    private void addEmptyCardItems() {
        int displayCount = mContainer.getDisplayCount(getActualCardCount());
        int extraEmptyCardCount = displayCount - mCardItemList.size();
        if (extraEmptyCardCount != 0) {
            for (int i = 0; i < extraEmptyCardCount; i++) {
                CardItem item = new CardItem();
                item.setIsEmpty(true);
                mCardItemList.add(item);
            }
        }
    }

    /**
     * 返回从第一张卡片至最后一张不为空的卡片的个数
     * @return
     */
    private int getActualCardCount() {
        CardItem lastNotEmptyItem = null;
        for (CardItem item : mCardItemList) {
            if (!item.getIsEmpty()) {
                lastNotEmptyItem = item;
            }
        }
        if (lastNotEmptyItem != null) {
            return mCardItemList.indexOf(lastNotEmptyItem) + 1;
        }
        return 0;
    }
}
