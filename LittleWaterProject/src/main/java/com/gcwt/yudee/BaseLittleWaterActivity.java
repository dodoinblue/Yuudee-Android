/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.pattern.BaseActivity;
import android.pattern.util.DialogManager;
import android.pattern.util.PhotoUtil;
import android.pattern.util.PhotoUtils;
import android.pattern.util.Utility;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by peter on 3/3/15.
 */
abstract public class BaseLittleWaterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
    }

    protected void AddNewCardItem() {

    }

    // Below are all related about taking picture from album and camera
    protected ViewGroup mRootView;
    TextView layout_choose;
    TextView layout_photo;
    PopupWindow avatorPop;
    public String filePath = "";
    boolean isFromCamera = false;// 区分拍照旋转
    int degree = 0;
    String path;
    protected ImageView mCardCoverView;

    protected void showAvatarPop() {
        View view = mInflater.inflate(R.layout.pop_layout_avatar, null);
        RelativeLayout parent = (RelativeLayout) view
                .findViewById(R.id.partent);
        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                avatorPop.dismiss();

            }
        });
        layout_choose = (TextView) view.findViewById(R.id.layout_choose);
        layout_photo = (TextView) view.findViewById(R.id.layout_photo);
        layout_photo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d("zheng", "点击拍照");
                filePath = PhotoUtils.takePicture(BaseLittleWaterActivity.this);
            }
        });
        layout_choose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d("zheng", "点击相册");

                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent,
                        PhotoUtils.REQUESTCODE_UPLOADAVATAR_LOCATION);

//				Intent intent = new Intent(this, PhotoPickerActivity.class);
//				startActivityForResult(intent, PhotoUtils.REQUESTCODE_UPLOADAVATAR_LOCATION);
            }
        });

        view.findViewById(R.id.layout_cancel).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        avatorPop.dismiss();
                    }
                });

        avatorPop = new PopupWindow(view, mScreenWidth, 600);
        avatorPop.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    avatorPop.dismiss();
                    return true;
                }
                return false;
            }
        });

        avatorPop.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        avatorPop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        avatorPop.setTouchable(true);
        avatorPop.setFocusable(true);
        avatorPop.setOutsideTouchable(true);

        avatorPop.setBackgroundDrawable(new BitmapDrawable());
        // 动画效果 从底部弹起
        // avatorPop.setAnimationStyle(R.style.Animations_GrowFromBottom);
        avatorPop.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case PhotoUtils.REQUESTCODE_UPLOADAVATAR_CAMERA:// 拍照修改头像
                System.out.println("拍照修改头像");
                if (resultCode == Activity.RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        showCustomToast("SD不可用");
                        return;
                    }
                    filePath = PhotoUtils.savePhotoToSDCard(PhotoUtils
                            .CompressionPhoto(mScreenWidth, filePath, 2));
                    PhotoUtils.cropPhoto(this, this, filePath);
                }
                break;
            case PhotoUtils.REQUESTCODE_UPLOADAVATAR_LOCATION:// 本地修改头像
                System.out.println("本地修改头像");
                if (avatorPop != null) {
                    avatorPop.dismiss();
                }
                Uri uri = null;
                if (data == null) {
                    return;
                }
                if (resultCode == Activity.RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        DialogManager.showTipMessage(BaseLittleWaterActivity.this, "SD不可用");
                        return;
                    }
                    isFromCamera = false;
                    uri = data.getData();
                    PhotoUtils.cropPhoto(this, this, /*data.getExtras().getString("path")*/Utility.getFilePathFromUri(this, uri));
                } else {
                    DialogManager.showTipMessage(this, "照片获取失败");
                }
                break;
            case PhotoUtils.REQUESTCODE_UPLOADAVATAR_CROP:// 裁剪头像返回

                if (avatorPop != null) {
                    avatorPop.dismiss();
                }
                if (data == null) {
                    // Toast.makeText(this, "取消选择", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    saveCropAvator(data);
                }
                // 初始化文件路径
                filePath = "";
                break;
            default:
                break;
        }
    }

    /**
     * 保存裁剪的头像
     */
    private void saveCropAvator(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
//            Bitmap bitmap = extras.getParcelable("data");
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            Log.d("zheng", "path:" + extras.getString("path") + " path:" + data.getStringExtra("path"));
            File file = new File(extras.getString("path"));
            Bitmap bitmap = null;
            try {
                FileInputStream stream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Log.i("life", "avatar - bitmap = " + bitmap);
            if (bitmap != null) {
                bitmap = PhotoUtil.toRoundCorner(bitmap, 10);
                if (isFromCamera && degree != 0) {
                    bitmap = PhotoUtil.rotaingImageView(degree, bitmap);
                }

                final Bitmap bm = bitmap;
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mCardCoverView.setImageBitmap(bm);
                    }
                }, 300);
                mCardCoverView.invalidate();
                // 保存图片
                String filename = new SimpleDateFormat("yyMMddHHmmss")
                        .format(new Date()) + ".png";
                path = PhotoUtils.MyAvatarDir + filename;
                PhotoUtil.saveBitmap(PhotoUtils.MyAvatarDir, filename, bitmap,
                        true);
                // 上传头像
                if (bitmap != null && bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
    }
}
