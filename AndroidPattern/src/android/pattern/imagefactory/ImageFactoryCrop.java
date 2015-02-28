package android.pattern.imagefactory;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.pattern.R;
import android.pattern.imagefactory.widgets.CropImage;
import android.pattern.imagefactory.widgets.CropImageView;
import android.pattern.util.PhotoUtils;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public class ImageFactoryCrop extends ImageFactory {
	public static final int SHOW_PROGRESS = 0;
	public static final int REMOVE_PROGRESS = 1;
	private CropImageView mCivDisplay;
	private ProgressBar mPbBar;

	private String mPath;
	private Bitmap mBitmap;
	private CropImage mCropImage;

	public ImageFactoryCrop(ImageFactoryActivity activity, View contentRootView) {
		super(activity, contentRootView);
	}

	@Override
	public void initViews() {
		mCivDisplay = (CropImageView) findViewById(R.id.imagefactory_crop_civ_display);
		mPbBar = (ProgressBar) findViewById(R.id.imagefactory_crop_pb_progressbar);
	}

	@Override
	public void initEvents() {

	}

	public void init(String path, int w, int h) {
		mPath = path;
		mBitmap = PhotoUtils.createBitmap(mPath, w, h);
		if (mBitmap != null) {
			resetImageView(mBitmap);
		}
	}

	private void resetImageView(Bitmap b) {
		mCivDisplay.clear();
		mCivDisplay.setImageBitmap(b);
		mCivDisplay.setImageBitmapResetBase(b, true);
		mCropImage = new CropImage(mContext, mCivDisplay, handler);
		mCropImage.crop(b);
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW_PROGRESS:
				mPbBar.setVisibility(View.VISIBLE);
				break;
			case REMOVE_PROGRESS:
				handler.removeMessages(SHOW_PROGRESS);
				mPbBar.setVisibility(View.INVISIBLE);
				break;
			}
		}

	};

	public void Rotate() {
		if (mCropImage != null) {
			mCropImage.startRotate(90.f);
		}
	}

	public Bitmap cropAndSave() {
		return mCropImage.cropAndSave();
	}

}
