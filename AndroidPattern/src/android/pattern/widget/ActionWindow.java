package android.pattern.widget;

import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.pattern.BaseActivity;
import android.pattern.R;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class ActionWindow {
    private PopupWindow mBottomPopWindow;
    private View mRootView;
    private int mStyleRes;
    private static final int DEFAULT_STYLE = R.style.Animations_GrowFromBottom;
    private int mOffsetX;
    private int mOffsetY;

    public ActionWindow(final BaseActivity activity, View rootView, View popUpView) {
        initWindow(activity, rootView, popUpView);
    }

    public ActionWindow(final BaseActivity activity, View rootView, int popUpViewLayoutId) {
        View popUpView = activity.getLayoutInflater().inflate(popUpViewLayoutId, null);
        initWindow(activity, rootView, popUpView);
    }
    
    public ActionWindow setWindowHeight(int height) {
        mBottomPopWindow.setHeight(height);
        return this;
    }

    public ActionWindow setWindowWidth(int width) {
        mBottomPopWindow.setWidth(width);
        return this;
    }

    public ActionWindow setOffsetX(int offsetX) {
        mOffsetX = offsetX;
        return this;
    }

    public ActionWindow setOffsetY(int offsetY) {
        mOffsetY = offsetY;
        return this;
    }

    public ActionWindow setAnimationStyle(int styleRes) {
        mStyleRes = styleRes;
        return this;
    }

    private void initWindow(final BaseActivity activity, View rootView, View popUpView) {
        mRootView = rootView;
        mBottomPopWindow = new PopupWindow(popUpView, activity.mScreenWidth, activity.mScreenHeight);
        mBottomPopWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mBottomPopWindow.dismiss();
                    return true;
                }
                return false;
            }
        });

        mBottomPopWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mBottomPopWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mBottomPopWindow.setTouchable(true);
        mBottomPopWindow.setFocusable(true);
        mBottomPopWindow.setOutsideTouchable(true);
        mBottomPopWindow.setBackgroundDrawable(new BitmapDrawable());
    }
    
    public void popup() {
        popup(Gravity.BOTTOM);
    }

    public void popup(int gravity) {
        if (mStyleRes > 0 ) {
            mBottomPopWindow.setAnimationStyle(mStyleRes);
        } else {
            mBottomPopWindow.setAnimationStyle(DEFAULT_STYLE);
        }
        mBottomPopWindow.showAtLocation(mRootView, gravity, mOffsetX, mOffsetY);
    }

    public void dropDown() {
        if (mStyleRes > 0 ) {
            mBottomPopWindow.setAnimationStyle(mStyleRes);
        }
        mBottomPopWindow.showAsDropDown(mRootView, mOffsetX, mOffsetY);
    }

    public void dismiss() {
        mBottomPopWindow.dismiss();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener dismissListener) {
        mBottomPopWindow.setOnDismissListener(dismissListener);
    }
}