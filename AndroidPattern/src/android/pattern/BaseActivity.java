/*
 * Copyright (c) 2015 著作权由郑志佳所有。著作权人保留一切权利。
 *
 * 这份授权条款，在使用者符合以下三条件的情形下，授予使用者使用及再散播本
 * 软件包装原始码及二进位可执行形式的权利，无论此包装是否经改作皆然：
 *
 * * 对于本软件源代码的再散播，必须保留上述的版权宣告、此三条件表列，以
 *   及下述的免责声明。
 * * 对于本套件二进位可执行形式的再散播，必须连带以文件以及／或者其他附
 *   于散播包装中的媒介方式，重制上述之版权宣告、此三条件表列，以及下述
 *   的免责声明。
 * * 未获事前取得书面许可，不得使用郑志佳或本软件贡献者之名称，
 *   来为本软件之衍生物做任何表示支持、认可或推广、促销之行为。
 *
 * 免责声明：本软件是由郑志佳及本软件之贡献者以现状（"as is"）提供，
 * 本软件包装不负任何明示或默示之担保责任，包括但不限于就适售性以及特定目
 * 的的适用性为默示性担保。郑志佳及本软件之贡献者，无论任何条件、
 * 无论成因或任何责任主义、无论此责任为因合约关系、无过失责任主义或因非违
 * 约之侵权（包括过失或其他原因等）而起，对于任何因使用本软件包装所产生的
 * 任何直接性、间接性、偶发性、特殊性、惩罚性或任何结果的损害（包括但不限
 * 于替代商品或劳务之购用、使用损失、资料损失、利益损失、业务中断等等），
 * 不负任何责任，即在该种使用已获事前告知可能会造成此类损害的情形下亦然。
 */

package android.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.pattern.util.DownloadManager;
import android.pattern.util.NetWorkUtils;
import android.pattern.util.NetWorkUtils.NetWorkState;
import android.pattern.util.Utility;
import android.pattern.util.WeakReferenceHandler;
import android.pattern.widget.BaseAlertDialogFragment;
import android.pattern.widget.CroutonWrapper;
import android.pattern.widget.FlippingLoadingDialog;
import android.pattern.widget.HandyTextView;
import android.pattern.widget.HomeMenuPopupWindow;
import android.pattern.widget.TopView;
import android.pattern.widget.HomeMenuPopupWindow.onHomeMenuClickListener;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public abstract class BaseActivity extends FragmentActivity implements onHomeMenuClickListener {
    public final static int MESSAGE_INVALID = -1;
    public final static int MESSAGE_DOWNLOAD_STARTED = 0;
    public final static int MESSAGE_DOWNLOADING = 1;
    public final static int MESSAGE_DOWNLOAD_COMPLETED = 2;
    public final static int MESSAGE_DOWNLOAD_CANCELLED = 3;
    public final static int MESSAGE_QUIT_APP = 4;

    protected final String TAG = Utility.getTAG(BaseActivity.this.getClass());
    protected CroutonWrapper mCrouton;
    protected DialogFragment mNoNetDialog;
    protected BaseApplication mApplication;
    public NetWorkUtils mNetWorkUtils;
    protected FlippingLoadingDialog mLoadingDialog;
    protected boolean mIsMenuHomeClicked = false;
    private boolean isExit = false;

    /**
     * 屏幕的宽度、高度、密度
     */
    public int mScreenWidth;
    public int mScreenHeight;
    protected float mDensity;

    protected List<AsyncTask<Void, Void, Boolean>> mAsyncTasks = new ArrayList<AsyncTask<Void, Void, Boolean>>();

    private int mWidth;
    private int mHeaderHeight;
    private HomeMenuPopupWindow mHomeMenuPopupWindow;

    protected long mFileTotalSize;
    protected long mFileDownLoadedSize;
    protected String mFilename;
    protected LayoutInflater mInflater;
    public DownloadManager mDownloadManager;
    public LinearLayout mViewContent;
    public TopView mTopView;;
    public final Handler mHandler = new WeakReferenceHandler<Activity>(this) {

        @Override
        protected void handleMessage(Activity reference, Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case MESSAGE_QUIT_APP:
                        isExit = false;
                        break;
                    case MESSAGE_DOWNLOAD_STARTED:
                        mFileTotalSize = (Integer) msg.obj;
                        handleDownloadStarted();
                        break;
                    case MESSAGE_DOWNLOADING:
                        mFileDownLoadedSize = (Integer) msg.obj;
                        handleDownloading();
                        break;
                    case MESSAGE_DOWNLOAD_COMPLETED:
                        Toast.makeText(reference, "文件下载完成", Toast.LENGTH_LONG).show();
                        handleDownloadCompleted();
                        break;
                    case MESSAGE_DOWNLOAD_CANCELLED:
                        Toast.makeText(reference, "文件下载被取消", Toast.LENGTH_LONG).show();
                        break;
                    case MESSAGE_INVALID:
                        String error = msg.getData().getString("error");
                        Toast.makeText(reference, error, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    };

    protected void handleDownloadStarted() {
    }

    protected void handleDownloading() {
    }

    protected void handleDownloadCompleted() {
    }

    public void cancelDownload() {
    }
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
        	actionBar.setDisplayHomeAsUpEnabled(true); // show the '<' in action bar
        	actionBar.setDisplayShowHomeEnabled(false); // don't show the Logo icon in action bar.
        }
        mCrouton = new CroutonWrapper(this);
        mApplication = BaseApplication.getInstance();
        mNetWorkUtils = new NetWorkUtils(this);
        mLoadingDialog = new FlippingLoadingDialog(this, getString(R.string.loading));
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mInflater = getLayoutInflater();

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;
        mScreenHeight = metric.heightPixels;
        mDensity = metric.density;
    }
    
    protected void addContentView(int viewResId) {
        addContentView(viewResId, false);
    }

    protected void addContentView(int viewResId, boolean isActionbarTransparent) {
        if (isActionbarTransparent) {
            setContentView(R.layout.base_transparent_bar_activity);
        } else {
            setContentView(R.layout.base_activity);
        }
        mTopView = new TopView(this);
        mViewContent = (LinearLayout) findViewById(R.id.base_activity_container);
        ViewGroup viewContent = (ViewGroup) mInflater.inflate(viewResId, mViewContent, false);
        if (isActionbarTransparent) {
            viewContent.setPadding(viewContent.getPaddingLeft(),
                    getResources().getDimensionPixelSize(R.dimen.actionbar_height) + viewContent.getPaddingTop(),
                    viewContent.getPaddingRight(), viewContent.getPaddingBottom());
        } else {
            findViewById(R.id.navibar_layout).setBackgroundColor(getResources().getColor(R.color.actionbar_color));
        }
        mViewContent.addView(viewContent);
    }

    @Override
    protected void onDestroy() {
        clearAsyncTask();
        dismissAlertDialog();
        super.onDestroy();
    }

    /** 初始化视图 **/
    protected abstract void initViews();

    /** 初始化事件 **/
    protected abstract void initEvents();

    protected void putAsyncTask(AsyncTask<Void, Void, Boolean> asyncTask) {
        mAsyncTasks.add(asyncTask.execute());
    }

    protected void clearAsyncTask() {
        Iterator<AsyncTask<Void, Void, Boolean>> iterator = mAsyncTasks.iterator();
        while (iterator.hasNext()) {
            AsyncTask<Void, Void, Boolean> asyncTask = iterator.next();
            if (asyncTask != null && !asyncTask.isCancelled()) {
                asyncTask.cancel(true);
            }
        }
        mAsyncTasks.clear();
    }

    public void showLoadingDialog(String text) {
        if (!TextUtils.isEmpty(text)) {
            mLoadingDialog.setText(text);
        }
        mLoadingDialog.show();
    }

    public void dismissLoadingDialog() {
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    /** 短暂显示Toast提示(来自res) **/
    protected void showShortToast(int resId) {
        Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
    }

    /** 短暂显示Toast提示(来自String) **/
    protected void showShortToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /** 长时间显示Toast提示(来自res) **/
    protected void showLongToast(int resId) {
        Toast.makeText(this, getString(resId), Toast.LENGTH_LONG).show();
    }

    /** 长时间显示Toast提示(来自String) **/
    protected void showLongToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    /** 显示自定义Toast提示(来自res) **/
    protected void showCustomToast(int resId) {
        showCustomToast(getString(resId));
    }

    /** 显示自定义Toast提示(来自String) **/
    @SuppressLint("InflateParams")
    public void showCustomToast(String text) {
        View toastRoot = LayoutInflater.from(BaseActivity.this).inflate(R.layout.common_toast, null);
        ((HandyTextView) toastRoot.findViewById(R.id.toast_text)).setText(text);
        Toast toast = new Toast(BaseActivity.this);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastRoot);
        toast.show();
    }

    /** Debug输出Log日志 **/
    protected static void showLogDebug(String tag, String msg) {
        Log.d(tag, msg);
    }

    /** Error输出Log日志 **/
    protected static void showLogError(String tag, String msg) {
        Log.e(tag, msg);
    }

    /** 通过Class跳转界面 **/
    public void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    /** 含有Bundle通过Class跳转界面 **/
    public void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /** 通过Action跳转界面 **/
    protected void startActivity(String action) {
        startActivity(action, null);
    }

    /** 含有Bundle通过Action跳转界面 **/
    protected void startActivity(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /** 含有标题和内容的对话框 **/
    protected AlertDialog showAlertDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message).show();
        return alertDialog;
    }

    /** 含有标题、内容、两个按钮的对话框 **/
    public AlertDialog showAlertDialog(String title, String message, String positiveText,
                    DialogInterface.OnClickListener onPositiveClickListener, String negativeText,
                    DialogInterface.OnClickListener onNegativeClickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                        .setPositiveButton(positiveText, onPositiveClickListener)
                        .setNegativeButton(negativeText, onNegativeClickListener).show();
        return alertDialog;
    }

    /** 含有标题、内容、一个按钮的对话框 **/
    protected AlertDialog showAlertDialog(String title, String message, String positiveText,
                    DialogInterface.OnClickListener onPositiveClickListener) {
        if (onPositiveClickListener == null) {
            onPositiveClickListener = new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                        .setPositiveButton(positiveText, onPositiveClickListener).show();
        return alertDialog;
    }

    /** 含有标题、内容、图标、两个按钮的对话框 **/
    protected AlertDialog showAlertDialog(String title, String message, int icon, String positiveText,
                    DialogInterface.OnClickListener onPositiveClickListener, String negativeText,
                    DialogInterface.OnClickListener onNegativeClickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message).setIcon(icon)
                        .setPositiveButton(positiveText, onPositiveClickListener)
                        .setNegativeButton(negativeText, onNegativeClickListener).show();
        return alertDialog;
    }

    /** 默认退出 **/
    protected void defaultFinish() {
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
        //        initPopupWindow();
        //        getMenuInflater().inflate(R.menu.home_menu, menu);
    }

    /**
     * Get the root view of this Activity
     * 
     * @return
     */
    private View getRootView() {
        return ((ViewGroup) BaseActivity.this.findViewById(android.R.id.content)).getChildAt(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.menu_home) {
            showHomeMenuPopupWindow();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected void showHomeMenuPopupWindow() {
        mHomeMenuPopupWindow.showAtLocation(getRootView(), Gravity.RIGHT | Gravity.TOP, -10, mHeaderHeight + 10);
    }

    public synchronized boolean showNoNetworkAlert() {
        if (mNetWorkUtils.getConnectState() != NetWorkState.NONE) {
            return false;
        }
        dismissAlertDialog();
        mNoNetDialog = BaseAlertDialogFragment.newInstance(R.string.ui_activity_no_network);
        mNoNetDialog.show(getFragmentManager(), "noNetworkDialog");

        return true;
    }
    
    public boolean hasNetwork() {
        return mNetWorkUtils.getConnectState() != NetWorkState.NONE;
    }

    @Override
    protected void onResume() {
    	super.onResume();
//    	showNoNetworkAlert();
    }

    public synchronized void dismissAlertDialog() {
        if (mNoNetDialog != null && mNoNetDialog.isAdded()) {
            mNoNetDialog.dismiss();
            mNoNetDialog = null;
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    protected void initPopupWindow() {
        mWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 164, getResources().getDisplayMetrics());
        mHeaderHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources()
                        .getDisplayMetrics());
        mHomeMenuPopupWindow = new HomeMenuPopupWindow(this, mWidth, LayoutParams.WRAP_CONTENT);
        mHomeMenuPopupWindow.setOnHomeMenuClickListener(this);
    }

    public void showInfoCrouton(String info) {
        if (!TextUtils.isEmpty(info)) {
//            mCrouton.showInfoCrouton(info);
            showCustomToast(info);
        }
    }

    public void showAlertCrouton(String info) {
        if (!TextUtils.isEmpty(info)) {
//            mCrouton.showAlertCrouton(info);
            showCustomToast(info);
        }
    }

    public boolean allowDownloadByCurrentNetwork() {
        return true;
    }

    public void showNetworkOption() {
    }

    @Override
    public void onHomeClick() {

    }

    @Override
    public void onPeixunClick() {

    }

    @Override
    public void onYuelanClick() {

    }

    @Override
    public void onMoreClick() {

    }

    protected void ToQuitTheApp() {
        if (isExit) {
            // ACTION_MAIN with category CATEGORY_HOME 启动主屏幕
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            System.exit(0);// 使虚拟机停止运行并退出程序
        } else {
            isExit = true;
            Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
            Message msg = mHandler.obtainMessage();
            msg.what = MESSAGE_QUIT_APP;
            mHandler.sendMessageDelayed(msg, 3000);// 3秒后发送消息
        }
    }
    
    public void onLeftClicked(View view) {
    	finish();
    }
    
    public void onRightClicked(View view) {
    }
    
    @Override
    public void setTitle(CharSequence title) {
    	super.setTitle(title);
        if (mTopView != null) {
            mTopView.setTitle(title);
        }
    }
    
    @Override
    public void setTitle(int titleId) {
    	super.setTitle(titleId);
        if (mTopView != null) {
            mTopView.setTitle(titleId);
        }
    }
    
    protected void hideActionBar() {
    	if (getActionBar() != null) {
    		getActionBar().hide();
    	}
        if (mTopView != null) {
            mTopView.hideActionBar();
        }
    }
    
    protected void showActionBar() {
    	if (getActionBar() != null) {
    		getActionBar().show();
    	}
        if (mTopView != null) {
            mTopView.showActionBar();
        }
    }
}
