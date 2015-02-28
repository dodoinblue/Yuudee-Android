package android.pattern.widget;

import android.app.Activity;
import android.pattern.R;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TopView {
	public TextView mLeftView;
	private TextView mRightView;
	private TextView mTitleView;
	private Activity mActivity;
	private ViewGroup mNavigationLayout;

	public TopView(Activity act) {
		this.mActivity = act;
		mLeftView = (TextView) mActivity.findViewById(R.id.btn_top_bar_back);
		mRightView = (TextView) mActivity.findViewById(R.id.btn_top_bar_right);
		mTitleView = (TextView) mActivity.findViewById(R.id.tv_top_bar);
		mNavigationLayout = (ViewGroup) mActivity.findViewById(R.id.navibar_layout);
	}

	public void setTitle(CharSequence text) {
		if (mTitleView != null) {
			mTitleView.setText(text);
		}
	}

	public void setTitle(int text) {
		if (mTitleView != null) {
			mTitleView.setText(text);
		}
	}
	
	public void setTitleBackground(int resId) {
	    if (mTitleView != null) {
            mTitleView.setBackgroundResource(resId);
            mTitleView.setVisibility(View.VISIBLE);
        }
	}

	public void setLeftText(String text) {
		if (mLeftView != null) {
			mLeftView.setText(text);
			mLeftView.setVisibility(View.VISIBLE);
		}
	}

	public void setLeftText(int text) {
		if (mLeftView != null) {
			mLeftView.setText(text);
			mLeftView.setVisibility(View.VISIBLE);
		}
	}
	
	public void setLeftCompoundDrawables(int left, int top, int right, int bottom) {
        if (mLeftView != null) {
            mLeftView.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
            mLeftView.setVisibility(View.VISIBLE);
        }
    }
	
	public void setLeftBackground(int res) {
        if (mLeftView != null) {
            mLeftView.setBackgroundResource(res);
            mLeftView.setVisibility(View.VISIBLE);
        }
    }
	
	public void setLeftEnabled(boolean enabled) {
        if (mLeftView == null) {
            return;
        }
        if (enabled) {
            mLeftView.setVisibility(View.VISIBLE);
        } else {
            mLeftView.setVisibility(View.GONE);
        }
    }

	public void setRightText(String text) {
		if (mRightView != null) {
			mRightView.setText(text);
			mRightView.setVisibility(View.VISIBLE);
		}
	}

	public void setRightText(int text) {
		if (mRightView != null) {
			mRightView.setText(text);
			mRightView.setVisibility(View.VISIBLE);
		}
	}
	
	public void setRightCompoundDrawables(int left, int top, int right, int bottom) {
        if (mRightView != null) {
            mRightView.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
            mRightView.setVisibility(View.VISIBLE);
        }
    }

	public void setRightBackground(int res) {
		if (mRightView != null) {
			mRightView.setBackgroundResource(res);
			mRightView.setVisibility(View.VISIBLE);
		}
	}

	public void setRightEnabled(boolean enabled) {
		if (mRightView == null) {
			return;
		}
		if (enabled) {
			mRightView.setVisibility(View.VISIBLE);
		} else {
			mRightView.setVisibility(View.GONE);
		}
	}
	
	public void clearActionBar() {
		if (mLeftView != null) {
			mLeftView.setVisibility(View.GONE);
			mLeftView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			mLeftView.setText("");
			mLeftView.setBackgroundResource(0);
		}
		if (mRightView != null) {
			mRightView.setVisibility(View.GONE);
			mRightView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			mRightView.setText("");
			mRightView.setBackgroundResource(0);
		}
		
		if (mTitleView != null) {
		    mTitleView.setBackground(null);
		}
		showActionBar();
	}
	
	public void hideActionBar() {
		if (mNavigationLayout != null) {
			mNavigationLayout.setVisibility(View.GONE);
		}
	}
	
	public void showActionBar() {
		if (mNavigationLayout != null) {
			mNavigationLayout.setVisibility(View.VISIBLE);
		}
	}
}
