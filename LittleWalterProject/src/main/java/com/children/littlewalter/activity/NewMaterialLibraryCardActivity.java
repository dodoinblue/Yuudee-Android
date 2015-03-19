/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.pattern.adapter.BaseListAdapter;
import android.pattern.util.DialogManager;
import android.pattern.util.PhotoUtil;
import android.pattern.util.PhotoUtils;
import android.pattern.util.Utility;
import android.pattern.widget.ActionWindow;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.children.littlewalter.BaseLittleWalterActivity;
import com.children.littlewalter.LittleWalterApplication;
import com.children.littlewalter.R;
import com.children.littlewalter.soundrecorder.RecordService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

/**
 * Created by peter on 3/10/15.
 */
public class NewMaterialLibraryCardActivity extends BaseLittleWalterActivity implements TextWatcher, MediaPlayer.OnCompletionListener {
    private EditText mNameEditView;
    private TextView mCardName;
    private ViewGroup mCoverContainer;
    private ViewGroup mSoundContainer;
    private ViewGroup mPreviousContainer;
    private ViewGroup mNextContainer;
    private ActionWindow mNewResourceWindow;
    private Button mChooseCategoryBtn;
    private ImageView mCardCoverView;

    private Button mRecordButton;
    private TextView mRecordNoticeView;
    private boolean mIsRecording;
    private RecordService mRecordService;
    private Button mPlayButton;
    private boolean mIsPlaying;
    private MediaPlayer mPlayer;
    private String mAudioFile;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_new_material_library_card);
        initViews();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecordService == null) {
            Intent serviceIntent = new Intent(this, RecordService.class);
            bindService(serviceIntent, mRecordServiceConn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        try {
            if (mRecordService != null) {
                unbindService(mRecordServiceConn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mIsPlaying) {
            mPlayer.stop();
            mPlayer.release();
        }
        super.onDestroy();
    }

    private ServiceConnection mRecordServiceConn = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            mRecordService = ((RecordService.RecordServiceBinder)service).getService();
            if (mRecordService.isRecording()) {
                mIsRecording = true;
                mRecordNoticeView.setText(R.string.stoprecorder);
            } else {
                mIsRecording = false;
                mRecordNoticeView.setText(R.string.startrecorder);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            mRecordService = null;
        }

    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp.isPlaying()) {
            mp.stop();
        }
        mp.release();

        mPlayButton.setText(R.string.startplay);
        mIsPlaying = false;
    }

    protected void initViews() {
        setTitle("");
        mNameEditView = (EditText) findViewById(R.id.edit_name);
        mCardName = (TextView) findViewById(R.id.card_name);
        mCoverContainer = (ViewGroup) findViewById(R.id.cover_container);
        mSoundContainer = (ViewGroup) findViewById(R.id.sound_container);
        mPreviousContainer = (ViewGroup) findViewById(R.id.previous_container);
        mNextContainer = (ViewGroup) findViewById(R.id.next_container);
        mChooseCategoryBtn = (Button) findViewById(R.id.choose_category);
        mCardCoverView = (ImageView) findViewById(R.id.card_cover);

        mRecordButton = (Button)findViewById(R.id.record_sound);
        mPlayButton = (Button)findViewById(R.id.play_sound);
        mRecordNoticeView = (TextView) findViewById(R.id.record_notice);
    }

    protected void initEvents() {
        mNameEditView.addTextChangedListener(this);
        Intent service = new Intent(this, RecordService.class);
        startService(service);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.next:
                if (TextUtils.isEmpty(mNameEditView.getEditableText().toString())) {
                    showCustomToast("请输入卡片名称");
                    return;
                }
                mCoverContainer.setVisibility(View.GONE);
                mSoundContainer.setVisibility(View.VISIBLE);
                mPreviousContainer.setVisibility(View.GONE);
                mNextContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.previous:
                mCoverContainer.setVisibility(View.VISIBLE);
                mSoundContainer.setVisibility(View.GONE);
                mPreviousContainer.setVisibility(View.VISIBLE);
                mNextContainer.setVisibility(View.GONE);
                break;
            case R.id.confirm:
                saveNewCategory();
                break;
            case R.id.camera:
                filePath = PhotoUtils.takePicture(NewMaterialLibraryCardActivity.this);
                break;
            case R.id.album:
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent,
                        PhotoUtils.REQUESTCODE_UPLOADAVATAR_LOCATION);
                break;
            case R.id.choose_category:
                View layoutCategoryList = mInflater.inflate(R.layout.drop_down_menu_new_card_category_list, null);
                mNewResourceWindow = new ActionWindow(this, findViewById(R.id.choose_category), layoutCategoryList);
                mNewResourceWindow.dropDown();

                Set<String> mCategorySet = LittleWalterApplication.getCategoryCoverPreferences().getAll().keySet();
                ArrayList<String> categoryList = new ArrayList<String>(mCategorySet);
                ListView listView = (ListView) layoutCategoryList.findViewById(R.id.list_view);
                BaseListAdapter adapter = new BaseListAdapter<String>(this, categoryList) {
                    @Override
                    public View bindView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = mInflater.inflate(R.layout.layout_new_card_category_list_item, parent, false);
                        }
                        TextView categoryView = (TextView) convertView;
                        categoryView.setText(getItem(position));
                        return convertView;
                    }
                };
                adapter.setOnInViewClickListener(R.id.category_item, new BaseListAdapter.OnInternalClickListener() {
                    @Override
                    public void OnClickListener(View convertView, View clickedView, Integer position, Object value) {
                        mChooseCategoryBtn.setText((String)value);
                        mNewResourceWindow.dismiss();
                    }
                });
                listView.setAdapter(adapter);
                break;
            case R.id.record_sound:
                if (!mIsRecording) {
                    mRecordService.startRecord();
                    mRecordNoticeView.setText(R.string.stoprecorder);
                    mIsRecording = true;
                } else {
                    mIsRecording = false;
                    mRecordService.stopRecord();
                    mAudioFile = mRecordService.getAudioFilePath();
                    mRecordNoticeView.setText(R.string.startrecorder);
                    mPlayButton.setText(R.string.startplay);
                }
                break;
            case R.id.play_sound:
                if (!mIsPlaying) {
                    File file = new File(mAudioFile);
                    Uri fileuri = Uri.fromFile(file);
                    mPlayer = MediaPlayer.create(this, fileuri);
                    mPlayer.setOnCompletionListener(this);
                    mPlayer.start();

                    mPlayButton.setText(R.string.stopplay);
                    mIsPlaying = true;
                } else {
                    mPlayer.stop();
                    mPlayer.release();

                    mPlayButton.setText(R.string.startplay);
                    mIsPlaying = false;
                }
                break;
            case R.id.delete_sound:
                break;
        }
    }

    private void saveNewCategory() {
        if (TextUtils.isEmpty(mCardName.getText().toString())) {
            showCustomToast("请输入分类名称");
            return;
        }
        LittleWalterApplication.getCategoryCardsPreferences().putString(mCardName.getText().toString(), "");
        finish();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mCardName.setText(mNameEditView.getText().toString());
    }

    PopupWindow avatorPop;
    public String filePath = "";
    boolean isFromCamera = false;// 区分拍照旋转
    int degree = 0;
    String path;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        DialogManager.showTipMessage(NewMaterialLibraryCardActivity.this, "SD不可用");
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
