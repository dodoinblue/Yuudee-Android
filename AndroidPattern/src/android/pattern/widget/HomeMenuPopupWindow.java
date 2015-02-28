package android.pattern.widget;

import android.pattern.BasePopupWindow;
import android.pattern.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public class HomeMenuPopupWindow extends BasePopupWindow implements OnClickListener {
    private HandyTextView mHome;
    private HandyTextView mPeixun;
    private HandyTextView mYuelan;
    private HandyTextView mMore;

    private onHomeMenuClickListener mOnHomeMenuClickListener;

    public HomeMenuPopupWindow(Context context, int width, int height) {
        super(LayoutInflater.from(context).inflate(R.layout.include_dialog_home, null), width, height);
        setAnimationStyle(R.style.Popup_Animation_Alpha);
    }

    @Override
    public void initViews() {
        mHome = (HandyTextView) findViewById(R.id.dialog_home);
        mPeixun = (HandyTextView) findViewById(R.id.dialog_internal_training);
        mYuelan = (HandyTextView) findViewById(R.id.dialog_user_read);
        mMore = (HandyTextView) findViewById(R.id.dialog_more);
    }

    @Override
    public void initEvents() {
        mHome.setOnClickListener(this);
        mPeixun.setOnClickListener(this);
        mYuelan.setOnClickListener(this);
        mMore.setOnClickListener(this);
    }

    @Override
    public void init() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialog_home) {
            if (mOnHomeMenuClickListener != null) {
                mOnHomeMenuClickListener.onHomeClick();
            }
        } else if (id == R.id.dialog_internal_training) {
            if (mOnHomeMenuClickListener != null) {
                mOnHomeMenuClickListener.onPeixunClick();
            }
        } else if (id == R.id.dialog_user_read) {
            if (mOnHomeMenuClickListener != null) {
                mOnHomeMenuClickListener.onYuelanClick();
            }
        } else if (id == R.id.dialog_more) {
            if (mOnHomeMenuClickListener != null) {
                mOnHomeMenuClickListener.onMoreClick();
            }
        }
        dismiss();
    }

    public void setOnHomeMenuClickListener(onHomeMenuClickListener listener) {
        mOnHomeMenuClickListener = listener;
    }

    public interface onHomeMenuClickListener {
        void onHomeClick();

        void onPeixunClick();

        void onYuelanClick();

        void onMoreClick();
    }
}
