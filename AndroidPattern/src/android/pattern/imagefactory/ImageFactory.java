package android.pattern.imagefactory;

import android.content.Context;
import android.view.View;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public abstract class ImageFactory {
	protected ImageFactoryActivity mActivity;
	protected Context mContext;
	private View mContentRootView;

	public ImageFactory(ImageFactoryActivity activity, View contentRootView) {
		mActivity = activity;
		mContext = (Context) mActivity;
		mContentRootView = contentRootView;
		initViews();
		initEvents();

	}

	public View findViewById(int id) {
		return mContentRootView.findViewById(id);
	}

	public abstract void initViews();

	public abstract void initEvents();
}
