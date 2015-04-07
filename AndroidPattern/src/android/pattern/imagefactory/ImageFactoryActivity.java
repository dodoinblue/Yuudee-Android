package android.pattern.imagefactory;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.pattern.BaseActivity;
import android.pattern.R;
import android.pattern.util.PhotoUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ViewFlipper;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public class ImageFactoryActivity extends BaseActivity {
	private ViewFlipper mVfFlipper;
	private Button mBtnLeft;
	private Button mBtnRight;

	private ImageFactoryCrop mImageFactoryCrop;
	private String mPath;
	private String mNewPath;

	public static final String TYPE = "type";
	public static final String CROP = "crop";
	public static final String FLITER = "fliter";

	public int mScreenWidth;
	protected int mScreenHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowHomeEnabled(false); // don't show the Logo.
		}

		setContentView(R.layout.activity_imagefactory);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScreenWidth = metric.widthPixels;
		mScreenHeight = metric.heightPixels;
		initViews();
		initEvents();
		init();
	}

	protected void initViews() {
		mVfFlipper = (ViewFlipper) findViewById(R.id.imagefactory_vf_viewflipper);
		mBtnLeft = (Button) findViewById(R.id.imagefactory_btn_left);
		mBtnRight = (Button) findViewById(R.id.imagefactory_btn_right);
		if (mTopView != null) {
			mTopView.setRightBackground(R.drawable.ic_topbar_rotation);
		}
        setTitle("剪切照片");
	}

    protected void initEvents() {
		mBtnLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		mBtnRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bitmap bitmap = mImageFactoryCrop.cropAndSave();
				mNewPath = PhotoUtils.savePhotoToSDCard(bitmap);
                bitmap.recycle();
				Intent intent = new Intent();
				intent.putExtra("path", mNewPath);
//				intent.putExtra("data", bitmap);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_image_factory, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_rotate) {
			if (mImageFactoryCrop != null) {
				mImageFactoryCrop.Rotate();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onRightClicked(View view) {
	    if (mImageFactoryCrop != null) {
            mImageFactoryCrop.Rotate();
        }
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}

	private void init() {
		mPath = getIntent().getStringExtra("path");
		mNewPath = new String(mPath);
		initImageFactory();
	}

	private void initImageFactory() {
		if (mImageFactoryCrop == null) {
			mImageFactoryCrop = new ImageFactoryCrop(this, mVfFlipper.getChildAt(0));
        }
        mImageFactoryCrop.init(mPath, mScreenWidth, mScreenHeight);
        boolean rotate = getIntent().getBooleanExtra("rotate", false);
//        if (rotate) {
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mImageFactoryCrop.Rotate();
//                }
//            }, 800);
//        }
        mBtnLeft.setText("取    消");
		mBtnRight.setText("确    认");
	}
}
