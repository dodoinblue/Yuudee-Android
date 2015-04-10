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
import android.pattern.util.BitmapUtil;
import android.pattern.util.DialogManager;
import android.pattern.util.PhotoUtil;
import android.pattern.util.PhotoUtils;
import android.pattern.util.Utility;
import android.provider.MediaStore;
import android.text.TextUtils;
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

import com.gcwt.yudee.activity.LittleWaterActivity;
import com.gcwt.yudee.activity.MaterialLibraryCardsActivity;
import com.gcwt.yudee.activity.SubFolderLittleWaterActivity;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;
import com.gcwt.yudee.widget.ScrollLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 3/3/15.
 */
public abstract class BaseLittleWaterActivity extends BaseActivity {
    protected static final String ACTION_MATERIAL_LIBRARY_CHANGED = "com.gcwt.yudee.ACTION_MATERIAL_LIBRARY_CHANGED";
    // Container中滑动控件列表
    protected List<CardItem> mCardItemList = new ArrayList<CardItem>();
    // 滑动控件的容器Container
    protected ScrollLayout mContainer;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
    }

    // Below are all related about taking picture from album and camera
    protected ViewGroup mRootView;
    TextView layout_choose;
    TextView layout_photo;
    PopupWindow avatorPop;
    protected String filePath = "";
    private static final int DEGREE_90 = 90;
    protected ImageView mCardCoverView;
    protected boolean mGotACardCover;

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
                filePath = PhotoUtils.takePicture(BaseLittleWaterActivity.this);
            }
        });
        layout_choose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent,
                        PhotoUtils.REQUESTCODE_UPLOADAVATAR_LOCATION);

                //Intent intent = new Intent(this, PhotoPickerActivity.class);
                //startActivityForResult(intent, PhotoUtils.REQUESTCODE_UPLOADAVATAR_LOCATION);
            }
        });

        view.findViewById(R.id.layout_cancel).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        avatorPop.dismiss();
                    }
                });

        avatorPop = new PopupWindow(view, mScreenWidth, mScreenHeight);
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
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PhotoUtils.REQUESTCODE_UPLOADAVATAR_CAMERA:// 拍照修改头像
                Log.d("zheng", "拍照修改头像");
                if (resultCode == Activity.RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        showCustomToast("SD不可用");
                        return;
                    }
                    Bitmap bitmap = PhotoUtils.getBitmapFromFile(filePath);
                    bitmap = PhotoUtil.rotaingImageView(DEGREE_90, bitmap);
                    filePath = PhotoUtils.savePhotoToSDCard(PhotoUtils.CompressionPhoto(mScreenWidth, bitmap, 2));
                    PhotoUtils.cropPhoto(this, this, filePath, true);
                }
                break;
            case PhotoUtils.REQUESTCODE_UPLOADAVATAR_LOCATION:// 本地修改头像
                Log.d("zheng", "本地修改头像");
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
                    uri = data.getData();
                    PhotoUtils.cropPhoto(this, this, /*data.getExtras().getString("path")*/Utility.getFilePathFromUri(this, uri), false);
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

            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY:
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_CATEGORY_FOLDER:
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY_CARD:
            case LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_CATEGORY_CARD_SETTINGS:
                handleCardEditRequest(data, requestCode, false);
                if (requestCode == LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY
                        || requestCode == LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY_CARD) {
                    data.setAction(ACTION_MATERIAL_LIBRARY_CHANGED);
                    data.putExtra("request_code", requestCode);
                    sendBroadcast(data);
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    protected void handleCardEditRequest(Intent data, int requestCode, boolean byBroadcast) {
        boolean libraryDeleted = data.getBooleanExtra("library_deleted", false);
        CardItem libraryItem = (CardItem) data.getSerializableExtra("library_card");
        String libraryName = libraryItem.getName();
        String oldLibraryName = data.getStringExtra("old_library_name");
        boolean libraryNameChanged = !TextUtils.isEmpty(oldLibraryName);
        int cardPosition = mCardItemList.indexOf(libraryItem);
        if (libraryDeleted) {
            if (this instanceof LittleWaterActivity || this instanceof SubFolderLittleWaterActivity) {
                if (cardPosition != -1) {
                    CardItem emptyItem = new CardItem();
                    emptyItem.isEmpty = true;
                    mCardItemList.set(cardPosition, emptyItem);
                }
            } else {
                mCardItemList.remove(libraryItem);
            }
            if (needUpdatePreference(requestCode, byBroadcast, libraryDeleted)) {
                LittleWaterApplication.getMaterialLibraryCardsPreferences().remove(libraryName);
                LittleWaterApplication.getMaterialLibraryCoverPreferences().remove(libraryName);
            }
        } else {
            if (libraryNameChanged) {
                // In this case user has changed card name
                CardItem item = new CardItem();
                item.setName(oldLibraryName);
                item.isLibrary = libraryItem.isLibrary;
                cardPosition = mCardItemList.indexOf(item);

                if (needUpdatePreference(requestCode, byBroadcast, libraryDeleted)) {
                    ArrayList<CardItem> libraryCardList = LittleWaterUtility.getMaterialLibraryCardsList(oldLibraryName);
                    LittleWaterApplication.getMaterialLibraryCardsPreferences().remove(oldLibraryName);
                    LittleWaterUtility.setMaterialLibraryCardsList(libraryName, libraryCardList);

                    String libraryCover = LittleWaterApplication.getMaterialLibraryCoverPreferences().getString(oldLibraryName);
                    LittleWaterApplication.getMaterialLibraryCoverPreferences().remove(oldLibraryName);
                    LittleWaterApplication.getMaterialLibraryCoverPreferences().putString(libraryName, libraryCover);
                }
            }
            if (cardPosition != -1) {
                mCardItemList.set(cardPosition, libraryItem);
            }

            if (this instanceof MaterialLibraryCardsActivity) {
                mCardItemList.clear();
                mCardItemList.addAll(LittleWaterUtility.getMaterialLibraryCardsList(getMaterialLibraryName()));
                Log.d("zhengzj", "MaterialLibraryCardsActivity update mCardItemList size:" + mCardItemList.size());
            }
        }
        mContainer.refreshView();
        mContainer.showEdit(true);
    }

    protected String getMaterialLibraryName() {
        return "";
    }

    private boolean needUpdatePreference(int requestCode, boolean byBroadcast, boolean libraryDeleted) {
        return !byBroadcast && (requestCode == LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_MATERIAL_LIBRARY
                                || (requestCode == LittleWaterConstant.ACTIVITY_REQUEST_CODE_EDIT_CATEGORY_FOLDER && !libraryDeleted));
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
//                bitmap = PhotoUtil.toRoundCorner(bitmap, LittleWaterUtility.ROUND_PX);
                bitmap = BitmapUtil.getRoundedCornerBitmap(bitmap, LittleWaterUtility.ROUND_PX);
                mCardCoverView.setImageBitmap(bitmap);
                mGotACardCover = true;
                // 保存图片
//                PhotoUtil.saveBitmap(getPictureSavePath(), getPictureSaveName(), bitmap,
//                        true);
                if (bitmap != null && bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
    }

    protected void updateToRoundImageDrawable(ImageView cardCoverView) {
        BitmapDrawable drawable = (BitmapDrawable) cardCoverView.getDrawable();
        if (drawable != null && drawable.getBitmap() != null) {
            Bitmap bitmap = BitmapUtil.getRoundedCornerBitmap(drawable.getBitmap(), LittleWaterUtility.ROUND_PX);
            cardCoverView.setImageBitmap(bitmap);
        }
    }
}
