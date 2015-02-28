/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package android.pattern;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.umeng.update.UmengUpdateAgent;

/**
 * Created by peter on 15/1/11.
 */
public abstract class BaseTabActivity extends BaseActivity {
    protected static final int TAB_0 = 0;
    protected static final int TAB_1 = 1;
    protected static final int TAB_2 = 2;
    protected static final int TAB_3 = 3;
    protected static final int TAB_4 = 4;

    private int[] mTabIds = {R.id.tab_1, R.id.tab_2, R.id.tab_3, R.id.tab_4, R.id.tab_5};
    private int mNewIndex;
    private long mFirstPressBackTime;
    private Fragment mCurrentFragment;
    private static final int DEFAULT_TAB_INDEX = 0;

    protected Fragment[] mFragments;
    protected TextView[] mTabs;
    protected int mCurrentTabIndex = DEFAULT_TAB_INDEX;
    protected int mPreviousTabIndex = DEFAULT_TAB_INDEX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    protected void init() {
        initViews();
        initTabFragments();
        initTabs();
        initEvents();
        initAppUpdate();
    }

    abstract protected void initTabFragments();

    abstract protected void initTabs();

    private void initAppUpdate() {
        UmengUpdateAgent.setDefault();
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);
    }

    @Override
    protected void initViews() {
        mTabs = new TextView[mTabIds.length];
        int tabIndex = 0;
        for (int tabResourceId : mTabIds) {
            mTabs[tabIndex++] = (TextView) findViewById(tabResourceId);
        }

        mTabs[mCurrentTabIndex].setSelected(true);
    }

    @Override
    protected void initEvents() {
        // 添加显示第一个fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mFragments[mCurrentTabIndex])
                .show(mFragments[mCurrentTabIndex]).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            default:
                return onOptionsItemSelected(item);
        }
    }

    /**
     * button点击事件
     *
     * @param view
     */
    public void onTabSelect(View view) {
        mNewIndex = Integer.valueOf((String) view.getTag());
        mPreviousTabIndex = mCurrentTabIndex;
        if (mCurrentTabIndex != mNewIndex) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(mFragments[mCurrentTabIndex]);
            if (!mFragments[mNewIndex].isAdded()) {
                trx.add(R.id.fragment_container, mFragments[mNewIndex]);
            }
            trx.show(mFragments[mNewIndex]);
            trx.commit();
            mTabs[mCurrentTabIndex].setSelected(false);

            // 把当前tab设为选中状态
            mTabs[mNewIndex].setSelected(true);
            mCurrentTabIndex = mNewIndex;
        }
        Log.d("zheng", "onTabSelect mCurrentTabIndex:" + mCurrentTabIndex + " mPreviousTabIndex:" + mPreviousTabIndex);
    }

    /**
     * 连续按两次返回键就退出
     */
    @Override
    public void onBackPressed() {
        if (mFirstPressBackTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            showCustomToast("再按一次退出程序");
        }
        mFirstPressBackTime = System.currentTimeMillis();
    }

    public void pushFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().hide(mFragments[mCurrentTabIndex])
                .add(R.id.fragment_container, fragment).show(fragment).commit();
        mCurrentFragment = fragment;
    }

    public void popupFragment() {
        getSupportFragmentManager().beginTransaction().remove(mCurrentFragment).commit();
        mCurrentFragment = null;
    }
}