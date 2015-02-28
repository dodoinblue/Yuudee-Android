package android.pattern.activities;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.pattern.BaseActivity;
import android.pattern.R;
import android.pattern.util.HttpRequest;
import android.pattern.util.ImageLoadOptions;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public class BuildingAlbumActivity extends BaseActivity {
    public final static String API_BUILDING_BUILDINGPIC = "building/buildingpic/";
    private String mBuildingId;
    private ViewFlipper viewFlipper;
    private TextView mPictureCountView;
    private TextView mSelectedTypeView;
    private ViewGroup mCirclesContainer;
    private Timer mTimer;

    private class Picture {
        public String id;// :图片ID,
        public String pic_mobile;// :手机上显示的（小图）,
        public String pic_path;// :网站上使用的图片(上传的原始图片)
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_building_album);
        initViews();
        initEvents();
    }

    @Override
    protected void initViews() {
        setTitle("楼盘相册");
        viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        mPictureCountView = (TextView) findViewById(R.id.picture_count);
        mCirclesContainer = (ViewGroup) findViewById(R.id.circles_container);
    }

    @Override
    protected void initEvents() {
        mTimer = new Timer();
        mBuildingId = getIntent().getStringExtra("buildingid");
        mSelectedTypeView = (TextView) findViewById(R.id.yangban);
        getPictures(mBuildingId, "1");
    }

    private void getPictures(String buildingid, String pictype) {
        RequestParams params = new RequestParams();
        params.put("buildingid", buildingid);
        params.put("pictype", pictype);
        HttpRequest.get(API_BUILDING_BUILDINGPIC, params,
                        new HttpRequest.HttpResponseHandler(this) {
            @Override
            public void onSuccess(JSONObject response) {
                super.onSuccess(response);
                try {
                    JSONArray list = response.getJSONArray("list");
                    Gson gson = new Gson();
                    List<Picture> pictures = gson.fromJson(list.toString(),
                                    new TypeToken<List<Picture>>() {
                    }.getType());
                    mPictureCountView.setText("共" + pictures.size() + "张");
                    boolean isFirstOne = true;
                    mCirclesContainer.removeAllViews();
                    viewFlipper.removeAllViews();
                    viewFlipper.stopFlipping();
                    mTimer.cancel();
                    for (Picture picture : pictures) {
                        ImageView imageView = (ImageView) mInflater.inflate(
                                        R.layout.layout_image, viewFlipper, false);
                        ImageLoader.getInstance().displayImage(picture.pic_mobile,
                                        imageView, ImageLoadOptions.getOptions());
                        viewFlipper.addView(imageView);
                        ImageView circleView = (ImageView) mInflater.inflate(R.layout.layout_small_circle_view, mCirclesContainer, false);
                        if (isFirstOne) {
                            isFirstOne = false;
                            circleView.setImageResource(R.drawable.orange_small_real_circle);
                        } else {
                            circleView.setImageResource(R.drawable.gray_small_real_circle);
                        }
                        mCirclesContainer.addView(circleView);
                    }
                    if (viewFlipper.getChildCount() > 1) {
                        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(
                                        BuildingAlbumActivity.this,
                                        R.anim.push_left_in));
                        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(
                                        BuildingAlbumActivity.this,
                                        R.anim.push_left_out));
                        viewFlipper.startFlipping();
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        int lastChildIndex = mCirclesContainer.getChildCount() - 1;
                                        View view = mCirclesContainer.getChildAt(lastChildIndex);
                                        mCirclesContainer.removeViewAt(lastChildIndex);
                                        mCirclesContainer.addView(view, 0);
                                    }
                                });
                            }
                        }, 5000, 5000);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClick(View view) {
        String picType = "1";
        int id = view.getId();
        if (id == R.id.yangban) {
            picType = "1";
        } else if (id == R.id.huxing) {
            picType = "2";
        } else if (id == R.id.xiaoguo) {
            picType = "3";
        } else if (id == R.id.jiaotong) {
            picType = "4";
        } else if (id == R.id.shijing) {
            picType = "5";
        } else {
            picType = "1";
        }
        ((TextView)view).setTextColor(getResources().getColor(R.color.orange));
        mSelectedTypeView.setTextColor(getResources().getColor(android.R.color.black));
        mSelectedTypeView = ((TextView)view);
        getPictures(mBuildingId, picType);
    }
}