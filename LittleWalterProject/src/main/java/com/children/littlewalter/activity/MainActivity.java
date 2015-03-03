/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.children.littlewalter.LittleWalterApplication;
import com.children.littlewalter.adapter.ScrollAdapter;
import com.children.littlewalter.model.CardItem;
import com.children.littlewalter.util.UnzipAssets;
import com.children.littlewalter.widget.ScrollLayout;
import com.children.littlewalter.widget.ScrollLayout.OnAddOrDeletePage;
import com.children.littlewalter.widget.ScrollLayout.OnEditModeListener;
import com.children.littlewalter.widget.ScrollLayout.OnPageChangedListener;
import com.children.littlewalter.R;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

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

	// 滑动控件的容器Container
	private ScrollLayout mContainer;

	// Container的Adapter
	private ScrollAdapter mItemsAdapter;
	// Container中滑动控件列表
	private List<CardItem> mList;

	//xUtils中操纵SQLite的助手类
	private DbUtils mDbUtils;

    private HashMap<String, ArrayList<CardItem>> mCardCategoryMap = new HashMap<String, ArrayList<CardItem>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 从缓存中初始化滑动控件列表
		getDataFromCache();
		// 初始化控件
		initViews();
        initEvents();
		//初始化容器Adapter
		loadBackground();
	}

    private void getDataFromCache() {
		mDbUtils = DbUtils.create(this);
		try {
			//使用xUtils，基于orderId从SQLite数据库中获取滑动控件
			mList = mDbUtils.findAll(Selector.from(CardItem.class).orderBy("orderId", false));
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

    @Override
	public void initViews() {
		mContainer = (ScrollLayout) findViewById(R.id.container);
        mContainer.getLayoutParams().height = mScreenWidth;
        mContainer.requestLayout();
		//如果没有缓存数据，则手动添加10条
		if (mList == null || mList.size() == 0) {
			mList = new ArrayList<CardItem>();
			for (int i = 1; i < 11; i++) {
				CardItem item = new CardItem();
				//根据drawable name获取对于的ID
				item.setImgdown(getDrawableId("item" + i + "_down"));
				item.setImgurl(getDrawableId("item" + i + "_normal"));
				item.setOrderId(i);
				item.setMid(i);
				mList.add(item);
			}
		}
		//初始化Container的Adapter
		mItemsAdapter = new ScrollAdapter(this, mList);
		//设置Container添加删除Item的回调
		mContainer.setOnAddPage(this);
		//设置Container页面换转的回调，比如自第一页滑动第二页
		mContainer.setOnPageChangedListener(this);
		//设置Container编辑模式的回调，长按进入修改模式
		mContainer.setOnEditModeListener(this);
		//设置Adapter
		mContainer.setSaAdapter(mItemsAdapter);
		//动态设置Container每页的列数为2行
		mContainer.setColCount(2);
		//动态设置Container每页的行数为2行
		mContainer.setRowCount(2);
		//调用refreView绘制所有的Item
		mContainer.refreView();
	}

    @Override
    protected void initEvents() {
        initLocalCardResources();
    }

    private void initLocalCardResources() {
        boolean firstEnterApp = LittleWalterApplication.getAppPreferences().getBoolean(FIRST_TIME_ENTER_APP, true);
        if (!firstEnterApp) {
            readCardsFromSDcard();
            return;
        }
        LittleWalterApplication.getAppPreferences().putBoolean(FIRST_TIME_ENTER_APP, false);

        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle("提示");
        dialog.setMessage("正在解压文件，请稍后！");
        dialog.show();//显示对话框
        new Thread(){
            public void run() {
                //在新线程中以同名覆盖方式解压
                try {
                    UnzipAssets.unZip(MainActivity.this, "cards.zip", OUTPUT_DIRECTORY, true);
                    readCardsFromSDcard();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.cancel();//解压完成后关闭对话框
            }
        }.start();
    }

    private void readCardsFromSDcard() {
        new Thread() {

            @Override
            public void run() {
                File categoriesFolder = new File(LOCAL_CARDS_DIRECTORY);
                String[] categories = categoriesFolder.list();
                for (String category : categories) {
                    File cardItemsFolder = new File(categoriesFolder.getPath() + "/" + category);
                    String[] cardItems = cardItemsFolder.list();
                    for (String cardItem : cardItems) {
                        File cardItemFolder = new File(cardItemsFolder.getPath() + "/" + cardItem);
                        if (cardItemFolder.isDirectory()) {
                            
                        } else {
                            
                        }
                    }
                }
            }
        }.start();
    }

	// 设置Container滑动背景图片
	private void loadBackground() {
		Options options = new Options();
		options.inSampleSize = 2;
        //Peter: we don't need the background
		//mContainer.setBackGroud(BitmapFactory.decodeResource(getResources(),R.drawable.main_bg, options));
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
			try {
				//退出APP前，保存当前的Items，记得所有item的位置
				List<CardItem> list = mContainer.getAllMoveItems();
                mDbUtils.deleteAll(CardItem.class);
				mDbUtils.saveAll(list);
			} catch (DbException e) {
				e.printStackTrace();
			}
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
}
