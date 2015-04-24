/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.pattern.adapter.BaseListAdapter;
import android.pattern.util.BlurUtility;
import android.pattern.util.FileUtils;
import android.pattern.util.Logger;
import android.pattern.util.PhotoUtil;
import android.pattern.util.UnzipAssets;
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
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.adapter.ScrollAdapter;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;
import com.gcwt.yudee.widget.ScrollLayout;
import com.gcwt.yudee.widget.ScrollLayout.OnAddOrDeletePage;
import com.gcwt.yudee.widget.ScrollLayout.OnEditModeListener;
import com.gcwt.yudee.widget.ScrollLayout.OnPageChangedListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * Created by peter on 3/3/15.
 */

@SuppressLint("HandlerLeak")
public class LittleWaterActivity extends BaseLittleWaterActivity implements OnAddOrDeletePage,
		OnPageChangedListener, OnEditModeListener {
    protected CardItem mSubFolderItem;
    private EditText mCategoryNameEdit;
    private long mFirstPressBackTime;
    public static final int LAYOUT_TYPE_1_X_1 = 1;
    public static final int LAYOUT_TYPE_2_X_2 = 2;
    protected int mCurrentCategoryCardLayoutSetting = LAYOUT_TYPE_2_X_2;
    private boolean mNeedGuideRemind = true;
    private int[] flipperResIds = { R.id.flipper1, R.id.flipper2, R.id.flipper3 };
    // Container的Adapter
	private ScrollAdapter mItemsAdapter;
    private HashMap<String, ArrayList<CardItem>> mCategoryCardsMap = new LinkedHashMap<String, ArrayList<CardItem>>();
    private HashMap<String, String> mCategoryCoverMap = new LinkedHashMap<String, String>();
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
    private HashMap<Integer, Boolean> mEnterParentCheckMap = new LinkedHashMap<Integer, Boolean>();
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
                        View guideView = findViewById(R.id.unlock_guide);
                        if (tripleDown && !mIsInParentMode && (guideView.getVisibility() == View.GONE)) {
                            enterParentMode();
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    mEnterParentCheckMap.put(v.getId(), false);
                    return true;
                default:
                    return false;
            }
        }
    };

    private BroadcastReceiver mMaterialLibraryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            if (ACTION_MATERIAL_LIBRARY_CHANGED.equals(data.getAction())) {
                int requestCode = data.getIntExtra("request_code", LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY_CARD);
                handleCardEditRequest(data, requestCode, true);
            } else if (ScrollLayout.ACTION_CARD_ITEM_MOVE.equals(data.getAction())) {
                addEmptyCardItems();
                mContainer.refreshView();
                mContainer.showEdit(mIsInParentMode);
            }
        }
    };

    protected int mRctivityLayout = R.layout.activity_little_water;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mRctivityLayout);
		// 初始化控件
		initViews();
        initEvents();
	}

    private void setCardCategory(ArrayList<CardItem> cardList) {
        for (CardItem item : cardList) {
            item.category = mCurrentCategory;
            if (item.isLibrary) {
                setCardCategory(item.childCardList);
            }
        }
    }

    private void setCardLibrary(ArrayList<CardItem> cardList) {
        for (CardItem item : cardList) {
            if (this instanceof SubFolderLittleWaterActivity) {
                item.libraryName = mSubFolderItem.name;
            } else {
                item.libraryName = "";
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LittleWaterConstant.ACTIVITY_REQUEST_CODE_SHOW_CARD) {
            mItemsAdapter.displayBackAfterShowCard();
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_CATEGORY_CARD:
                ArrayList<CardItem> selectedList = (ArrayList<CardItem>) data.getSerializableExtra("selected_card_list");
                int position = mCardItemList.indexOf(mNewCardItem);
                setCardCategory(selectedList);
//                setCardLibrary(selectedList);
                if (position != -1) {
                    mCardItemList.remove(position);
                    mCardItemList.addAll(position, selectedList);
                } else {
                    mCardItemList.addAll(selectedList);
                }

                mContainer.refreshView();
                mContainer.showEdit(mIsInParentMode);
                if (isMainUI()) {
                    LittleWaterUtility.setCategoryCardsList(mCurrentCategory, mContainer.getAllMoveItems());
                } else {
                    backupSubFolderList();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private boolean isMainUI() {
        return !(this instanceof SubFolderLittleWaterActivity);
    }

    @Override
	public void initViews() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MATERIAL_LIBRARY_CHANGED);
        filter.addAction(ScrollLayout.ACTION_CARD_ITEM_MOVE);
        if (isMainUI()) {
            registerReceiver(mMaterialLibraryChangeReceiver, filter);
        }
		mContainer = (ScrollLayout) findViewById(R.id.container);
        mContainer.getLayoutParams().height = mScreenWidth;
        mContainer.requestLayout();
		//设置Container添加删除Item的回调
		mContainer.setOnAddPage(this);
		//设置Container页面换转的回调，比如自第一页滑动第二页
		mContainer.setOnPageChangedListener(this);
		//设置Container编辑模式的回调，长按进入修改模式
		mContainer.setOnEditModeListener(this);
        //初始化Container的Adapter
        mItemsAdapter = new ScrollAdapter(mContainer, mCardItemList);
        //设置Adapter
        mContainer.setSaAdapter(mItemsAdapter, this);

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
                mCardItemList.clear();
                mCardItemList.addAll(LittleWaterUtility.getCategoryCardsList(mCurrentCategory));
                displayCards();

                final ImageView spinnerTitleView = (ImageView) findViewById(R.id.spinner_title);
                View dropDownView = findViewById(R.id.dropdown_category_list);
                dropDownView.setVisibility(View.INVISIBLE);
                spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titleunfoldbtn);
                mParentCategoryContent.setText(mCurrentCategory);
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
    protected void onPause() {
        super.onPause();
        if (isMainUI()) {
            LittleWaterUtility.setCategoryCardsList(mCurrentCategory, mContainer.getAllMoveItems());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMainUI()) {
            mCardItemList.clear();
            mCardItemList.addAll(LittleWaterUtility.getCategoryCardsList(mCurrentCategory));
            mContainer.refreshView();
            mContainer.showEdit(mIsInParentMode);
        }
    }

    @Override
    protected void onDestroy() {
        if (isMainUI()) {
            unregisterReceiver(mMaterialLibraryChangeReceiver);
        }
        super.onDestroy();
    }

    @Override
    protected void initEvents() {
        initLocalCardResources();
    }

    private void getCardsFromCache() {
        mNeedGuideRemind = LittleWaterApplication.getAppSettingsPreferences().getBoolean(LittleWaterConstant.SETTINGS_GUIDE_REMIND, true);
        mCurrentCategory = LittleWaterApplication.getAppSettingsPreferences().getString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY);
        mCurrentCategoryCardLayoutSetting = LittleWaterApplication.getCategorySettingsPreferences().getInt(mCurrentCategory, LAYOUT_TYPE_2_X_2);

        mCardItemList.clear();
        mCardItemList.addAll(LittleWaterUtility.getCategoryCardsList(mCurrentCategory));
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

        try {
            final ProgressDialog dialog = new ProgressDialog(LittleWaterActivity.this);
            dialog.setTitle(R.string.notice);
            dialog.setMessage(getString(R.string.unzip_in_progress));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();//显示对话框
            new Thread(){
                public void run() {
                    //在新线程中以同名覆盖方式解压
                    try {
                        UnzipAssets.unZip(LittleWaterActivity.this, "cards.zip", LittleWaterConstant.LITTLE_WALTER_DIRECTORY, true);
                        initCategoryCardsFromSDcard();
                        initDefaultMaterialLibraryFromSDcard();
                    } catch (IOException e) {
                        e.printStackTrace();
                        MobclickAgent.reportError(LittleWaterActivity.this, e);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.cancel();//解压完成后关闭对话框
                        }
                    });
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initCategoryCardsFromSDcard() {
        File cardResourceFolder = new File(LittleWaterConstant.CATEGORIES_DIRECTORY);
        File[] categoryFolders = cardResourceFolder.listFiles();
        for (File categoryFolder : categoryFolders) {
            ArrayList<CardItem> cardList = new ArrayList<CardItem>();
            String category = categoryFolder.getName();
            Log.d("zheng", "category:" + category);
//            category = category.split("-")[1];
            category = category.substring(category.indexOf("-") + 1);
            mCategoryCardsMap.put(category, cardList);
            if (mCurrentCategory == null) {
                mCurrentCategory = category;
                LittleWaterApplication.getAppSettingsPreferences().putString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY, mCurrentCategory);
            }
            LittleWaterUtility.parseCardCategory(categoryFolder, cardList, mCategoryCoverMap, category);
        }
        saveAllCardsIntoCache();
        mCardItemList.clear();
        mCardItemList.addAll(mCategoryCardsMap.get(mCurrentCategory));
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
            LittleWaterUtility.setCategoryCardsList(entry.getKey(), entry.getValue());
            LittleWaterUtility.setMaterialLibraryCardsList(entry.getKey(), entry.getValue());
        }

        Set<Map.Entry<String, String>> categoryCoverSet = mCategoryCoverMap.entrySet();
        for (Map.Entry<String, String> entry : categoryCoverSet) {
            LittleWaterApplication.getCategoryCoverPreferences().putString(entry.getKey(), entry.getValue());
            LittleWaterApplication.getMaterialLibraryCoverPreferences().putString(entry.getKey(), entry.getValue());
        }
    }

    protected void displayCards() {
        validateCardsEffectiveness();
//        addEmptyCardItems();
        LittleWaterUtility.sortCardList(mCardItemList);
        //动态设置Container每页的列数为2行
        mContainer.setColCount(mCurrentCategoryCardLayoutSetting);
        //动态设置Container每页的行数为2行
        mContainer.setRowCount(mCurrentCategoryCardLayoutSetting);
        //调用refreView绘制所有的Item
        mContainer.refreshView();
        mContainer.showEdit(mIsInParentMode);
        mParentCategoryContent.setText(mCurrentCategory);
    }

    private void validateCardsEffectiveness() {
        List<CardItem> removeItemList = new ArrayList<CardItem>();
        for (CardItem item : mCardItemList) {
            if (item.getIsEmpty()) {
                continue;
            }
            File file;
            if (item.isLibrary) {
                file = new File(item.getCover());
            } else {
                file = new File(item.getImages().get(0));
            }
            if (!file.exists()) {
                removeItemList.add(item);
            }
        }
        mCardItemList.removeAll(removeItemList);
        if (!(this instanceof SubFolderLittleWaterActivity)) {
            LittleWaterUtility.setCategoryCardsList(mCurrentCategory, mCardItemList);
        }
    }

    protected void backupSubFolderList() {
    }

	@Override
	public void onBackPressed() {
        if (this instanceof SubFolderLittleWaterActivity) {
            backupSubFolderList();
            super.onBackPressed();
            return;
        }
		//back键监听，如果在编辑模式，则取消编辑模式
		if (mIsInParentMode) {
            boolean someWhereNeedDisappear = false;
            View menuContainer = findViewById(R.id.about_menu_container);
            if (menuContainer.getVisibility() == View.VISIBLE) {
                menuContainer.setVisibility(View.INVISIBLE);
                someWhereNeedDisappear = true;
            }

            View dropDownView = findViewById(R.id.dropdown_category_list);
            if (dropDownView.getVisibility() == View.VISIBLE) {
                dropDownView.setVisibility(View.INVISIBLE);
                someWhereNeedDisappear = true;
            }
            if (someWhereNeedDisappear) {
                return;
            }

			enterChildMode();
			return;
		} else {
            if (mFirstPressBackTime + 2000 > System.currentTimeMillis()) {
                //退出APP前，保存当前的Items，记得所有item的位置
                LittleWaterUtility.setCategoryCardsList(mCurrentCategory, mContainer.getAllMoveItems());
                super.onBackPressed();
                android.os.Process.killProcess(android.os.Process.myPid());
            } else {
                showCustomToast(R.string.click_again_to_quit_app);
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
                ImageView spinnerTitleView = (ImageView) findViewById(R.id.spinner_title);
                View dropDownView = findViewById(R.id.dropdown_category_list);
                dropDownView.setVisibility(View.INVISIBLE);
                spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titleunfoldbtn);
                mParentCategoryContent.setText(mCurrentCategory);

                findViewById(R.id.about_menu_container).setVisibility(View.INVISIBLE);
                enterChildMode();
                break;
            case R.id.parent_about_open:
                showAboutLittleWalterWindow();
                break;
            case R.id.parent_settings:
                spinnerTitleView = (ImageView) findViewById(R.id.spinner_title);
                dropDownView = findViewById(R.id.dropdown_category_list);
                dropDownView.setVisibility(View.INVISIBLE);
                spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titleunfoldbtn);
                mParentCategoryContent.setText(mCurrentCategory);
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
                break;
            case R.id.parent_add_new_category_cancel:
                mNewCategorywindow.dismiss();
                break;
            case R.id.about_product_introduction:
                showProductIntroductionWindow();
                findViewById(R.id.about_menu_container).setVisibility(View.INVISIBLE);
//                mAboutMenuwindow.dismiss();
                break;
            case R.id.about_train_introduction:
                showTrainingIntroductionWindow();
                findViewById(R.id.about_menu_container).setVisibility(View.INVISIBLE);
//                mAboutMenuwindow.dismiss();
                break;
            case R.id.about_feedback_advice:
                FeedbackAgent agent = new FeedbackAgent(this);
                agent.startFeedbackActivity();
                findViewById(R.id.about_menu_container).setVisibility(View.INVISIBLE);
//                mAboutMenuwindow.dismiss();
                break;
            case R.id.update_app:
                UmengUpdateAgent.forceUpdate(this);
                findViewById(R.id.about_menu_container).setVisibility(View.INVISIBLE);
                showCustomToast(R.string.check_update_in_progress);
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
                Log.d("zheng", "card_edit");
                CardItem cardItem = (CardItem) view.getTag();
                if (cardItem.isLibrary) {
                    Intent intent = new Intent(LittleWaterActivity.this, EditCategoryFolderActivity.class);
                    intent.putExtra("library", cardItem);
                    startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_CATEGORY_FOLDER);
                } else {
                    Intent in = new Intent(this, EditCategoryCardSettingsActivity.class);
                    in.putExtra("card_item", cardItem);
                    startActivityForResult(in, LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_CATEGORY_CARD_SETTINGS);
                }
                break;
            case R.id.add_new_card:
                Log.d("zheng", "add_new_card");
                mNewCardItem = (CardItem) view.getTag();
                Intent newCardIntent = new Intent(this, NewCategoryCardActivity.class);
                newCardIntent.putExtra("current_category", mCurrentCategory);
                startActivityForResult(newCardIntent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_NEW_CATEGORY_CARD);
                break;
        }
    }
    private CardItem mNewCardItem;

    private void showUnloackParentUIRemind() {
        if (mNeedGuideRemind) {
            findViewById(R.id.unlock_guide).setVisibility(View.VISIBLE);
        } else {
            for (int flipperResId : flipperResIds) {
                final ViewFlipper flipper = (ViewFlipper) findViewById(flipperResId);
                flipper.startFlipping();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flipper.stopFlipping();
                        flipper.setDisplayedChild(0);
                    }
                }, 900);
            }
        }
    }

    private void settingsDeleteCategory() {
        if (mCategoryList.size() == 1) {
            showCustomToast(R.string.already_last_one_can_not_delete);
            return;
        }
        LittleWaterApplication.getCategoryCardsPreferences().remove(mCurrentCategory);
        mCategoryList.remove(mCurrentCategory);
        mCurrentCategory = mCategoryList.get(0);
        LittleWaterApplication.getAppSettingsPreferences().putString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY, mCurrentCategory);
        mCardItemList.clear();
        mCardItemList.addAll(LittleWaterUtility.getCategoryCardsList(mCurrentCategory));
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
        String category = categoryNameEditView.getText().toString();
        if (TextUtils.isEmpty(category)) {
            showCustomToast(R.string.enter_new_course_name);
            return;
        }
        if (mCategoryList.contains(category)) {
            showCustomToast(R.string.course_name_already_exist);
            return;
        }
        mCategoryList.add(category);
        mCategoryListAdapter.notifyDataSetChanged();

        mCurrentCategory = categoryNameEditView.getText().toString();
        LittleWaterApplication.getCategoryCardsPreferences().putString(mCurrentCategory, "");
        mCardItemList.clear();
        mCardItemList.addAll(LittleWaterUtility.getCategoryCardsList(mCurrentCategory));
        LittleWaterUtility.setCategoryCardsList(mCurrentCategory, mCardItemList);
        LittleWaterApplication.getAppSettingsPreferences().putString(LittleWaterConstant.SETTINGS_CURRENT_CATEGORY, mCurrentCategory);

        displayCards();
        findViewById(R.id.dropdown_category_list).setVisibility(View.INVISIBLE);
        final ImageView spinnerTitleView = (ImageView) findViewById(R.id.spinner_title);
        spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titleunfoldbtn);
        mNewCategorywindow.dismiss();
    }

    private void showAddNewCategoryWindow() {
        mNewCategoryLayout = (ViewGroup) mInflater.inflate(R.layout.action_window_new_category, null);
        mNewCategorywindow = new ActionWindow(this, findViewById(R.id.parent_settings), mNewCategoryLayout);

        blurBackground(mNewCategoryLayout, mNewCategorywindow);
    }

    private void blurBackground(final View backgroundLayout, final ActionWindow actionWindow) {
        actionWindow.setWindowHeight(WindowManager.LayoutParams.MATCH_PARENT);
        actionWindow.setWindowWidth(WindowManager.LayoutParams.MATCH_PARENT);
        actionWindow.popup(Gravity.CENTER);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                backgroundLayout.setBackground(new BitmapDrawable(getScreenshot()));
                BlurUtility.blur(LittleWaterActivity.this, backgroundLayout);
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
        ViewGroup actionLayout = (ViewGroup) mInflater.inflate(R.layout.action_window_product_introduction, null);
        mProductIntroductionWindow = new ActionWindow(this, findViewById(R.id.parent_about_open), actionLayout);
        mProductIntroductionWindow.popup(Gravity.CENTER);
//        blurBackground(actionLayout, mProductIntroductionWindow);
    }

    private void showTrainingIntroductionWindow() {
        ViewGroup actionLayout = (ViewGroup) mInflater.inflate(R.layout.action_window_training_introduction, null);
        mTrainingIntroductionWindow = new ActionWindow(this, findViewById(R.id.parent_about_open), actionLayout);
        mTrainingIntroductionWindow.popup(Gravity.CENTER);
//        blurBackground(actionLayout, mTrainingIntroductionWindow);
    }

    private void showCategoryDropDownWindow() {
        final ImageView spinnerTitleView = (ImageView) findViewById(R.id.spinner_title);
        View dropDownView = findViewById(R.id.dropdown_category_list);
        if (dropDownView.getVisibility() == View.INVISIBLE) {
            dropDownView.setVisibility(View.VISIBLE);
            spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titlefoldbtn);
            mCategoryListView = (ListView) findViewById(R.id.category_list);
            mCategoryListView.setAdapter(mCategoryListAdapter);
        } else {
            dropDownView.setVisibility(View.INVISIBLE);
            spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titleunfoldbtn);
            mParentCategoryContent.setText(mCurrentCategory);
        }
//        final ViewGroup categoryListLayout = (ViewGroup) mInflater.inflate(R.layout.layout_category_list, null, false);
//        mDropDownCategoryListWindow = new ActionWindow(this, findViewById(R.id.parent_categories_title), categoryListLayout);
//        mDropDownCategoryListWindow.dropDown();
//        spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titlefoldbtn);
//        mDropDownCategoryListWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                spinnerTitleView.setBackgroundResource(R.mipmap.parent_main_titleunfoldbtn);
//                mParentCategoryContent.setText(mCurrentCategory);
//            }
//        });
//        mCategoryListView = (ListView) categoryListLayout.findViewById(R.id.category_list);
//        mCategoryListView.setAdapter(mCategoryListAdapter);
    }

    private void popUpSettings() {
        mParentSettingsLayout = (ViewGroup) mInflater.inflate(R.layout.action_window_parent_settings, null);
        mSettingsActionWindow = new ActionWindow(this, findViewById(R.id.parent_settings), mParentSettingsLayout);
        mSettingsActionWindow.setAnimationStyle(0);
        blurBackground(mParentSettingsLayout, mSettingsActionWindow);
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
        UmengUpdateAgent.update(this);
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

    private void initDefaultMaterialLibraryFromSDcard() {
        File file = new File(LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + getString(R.string.uncategory));
        //如果目标目录不存在，则创建
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                Log.d("zheng", "initDefaultMaterialLibraryFromSDcard file:" + file.getName() + " create failed");
            }

            String libraryName = getString(R.string.uncategory);
            Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.default_image)).getBitmap();
            String coverFolder = LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + libraryName;
            String coverName = "cover.png";
            PhotoUtil.saveBitmap(coverFolder, coverName, bitmap, true);
            String cover = coverFolder + "/" + coverName;
            LittleWaterApplication.getMaterialLibraryCoverPreferences().putString(libraryName, cover);

            LittleWaterApplication.getMaterialLibraryCardsPreferences().putString(libraryName, "");
        }
    }

    public static class CardItemComparator implements Comparator<CardItem> {
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
    public void addEmptyCardItems() {
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

    public void removeEmptyCardItems() {
        int lastNotEmptyPosition = LittleWaterUtility.getLastNotEmptyCardPosition(mCardItemList);
        for(int i = mCardItemList.size() -1; i > lastNotEmptyPosition; i--) {
            mCardItemList.remove(i);
        }
//        mCardItemList = mCardItemList.subList(0,  + 1);
    }

    /**
     * 返回从第一张卡片至最后一张不为空的卡片的个数
     * @return
     */
    private int getActualCardCount() {
        return LittleWaterUtility.getLastNotEmptyCardPosition(mCardItemList) + 1;
    }
}
