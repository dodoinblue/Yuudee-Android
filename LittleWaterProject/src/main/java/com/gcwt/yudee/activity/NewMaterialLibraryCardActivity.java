/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.pattern.adapter.BaseListAdapter;
import android.pattern.util.PhotoUtil;
import android.pattern.util.PhotoUtils;
import android.pattern.widget.ActionWindow;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gcwt.yudee.BaseLittleWaterActivity;
import com.gcwt.yudee.LittleWaterApplication;
import com.gcwt.yudee.R;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.soundrecorder.RecordService;
import com.gcwt.yudee.util.FileService;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 3/10/15.
 */
public class NewMaterialLibraryCardActivity extends BaseLittleWaterActivity implements TextWatcher, MediaPlayer.OnCompletionListener {
    private EditText mNameEditView;
    private TextView mCardName;
    private ViewGroup mCoverContainer;
    private ViewGroup mSoundContainer;
    private ViewGroup mPreviousContainer;
    private ViewGroup mNextContainer;
    private ActionWindow mNewResourceWindow;
    private Button mChooseCategoryBtn;

    private Button mRecordButton;
    private TextView mRecordNoticeView;
    private boolean mIsRecording;
    private RecordService mRecordService;
    private Button mPlayButton;
    private boolean mIsPlaying;
    private MediaPlayer mPlayer;
    private String mAudioFile;
    private CardItem mLibraryCard = new CardItem();
    private String mMaterialLibraryPath;
    private String mMaterialLibraryName;

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
                mRecordNoticeView.setText(R.string.startrecorder);
            } else {
                mIsRecording = false;
                mRecordNoticeView.setText(R.string.prepare);
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

        mRecordNoticeView.setText(R.string.stopplay);
        mIsPlaying = false;
    }

    protected void initViews() {
        setTitle("");
        mRootView = (ViewGroup) findViewById(R.id.root_view);
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
        mRecordNoticeView.setText("准备");
    }

    protected void initEvents() {
        mMaterialLibraryName = getIntent().getStringExtra("material_library");
        if (TextUtils.isEmpty(mMaterialLibraryName)) {
            mMaterialLibraryName = "未分类";
        }
        mMaterialLibraryPath = LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + mMaterialLibraryName + "/";
        mChooseCategoryBtn.setText(mMaterialLibraryName);
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
                String cardName = mNameEditView.getEditableText().toString();
                if (TextUtils.isEmpty(cardName)) {
                    showCustomToast("请输入卡片名称");
                    return;
                }

                File cardFile = new File(mMaterialLibraryPath + cardName);
                if (cardFile.exists()) {
                    showCustomToast("卡片名称已存在, 请换个名称.");
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
                saveNewLibraryCard();
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

                Set<String> categorySet = LittleWaterApplication.getCategoryCoverPreferences().getAll().keySet();
                ArrayList<String> libraryList = new ArrayList<String>(categorySet);
                Set<String> librarySet = LittleWaterApplication.getMaterialLibraryCoverPreferences().getAll().keySet();
                libraryList.addAll(librarySet);
                ListView listView = (ListView) layoutCategoryList.findViewById(R.id.list_view);
                BaseListAdapter adapter = new BaseListAdapter<String>(this, libraryList) {
                    @Override
                    public View bindView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = mInflater.inflate(R.layout.layout_new_card_category_list_item, parent, false);
                        }
                        TextView categoryView = (TextView) convertView;
                        mMaterialLibraryName = getItem(position);
                        mMaterialLibraryPath = LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + mMaterialLibraryName + "/";
                        categoryView.setText(mMaterialLibraryName);
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
                    mRecordButton.setBackgroundResource(R.mipmap.record_down);
                    mRecordNoticeView.setText(R.string.startrecorder);
                    mIsRecording = true;
                } else {
                    mRecordService.stopRecord();
                    mRecordButton.setBackgroundResource(R.mipmap.record);
                    mAudioFile = mRecordService.getAudioFilePath();
                    mRecordNoticeView.setText(R.string.stoprecorder);
                    mIsRecording = false;
                }
                break;
            case R.id.play_sound:
                if (!mIsPlaying) {
                    File file = new File(mAudioFile);
                    Uri fileuri = Uri.fromFile(file);
                    mPlayer = MediaPlayer.create(this, fileuri);
                    mPlayer.setOnCompletionListener(this);
                    mPlayer.start();

                    mRecordNoticeView.setText(R.string.startplay);
                    mIsPlaying = true;
                } else {
                    mPlayer.stop();
                    mPlayer.release();

                    mRecordNoticeView.setText(R.string.stopplay);
                    mIsPlaying = false;
                }
                break;
            case R.id.delete_sound:

                break;
        }
    }

    private void saveNewLibraryCard() {
        // Create card folder in SD card.
        mLibraryCard.name = mCardName.getText().toString();
        mLibraryCard.editable = true;
        File cardFile = new File(mMaterialLibraryPath + mLibraryCard.name);
        cardFile.mkdirs();
        String audiosFolder = mMaterialLibraryPath + mLibraryCard.name + "/audios";
        File audiosFile = new File(audiosFolder);
        audiosFile.mkdirs();
        String imagesFolder = mMaterialLibraryPath + mLibraryCard.name + "/images";
        File imagesFile = new File(imagesFolder);
        imagesFile.mkdirs();

        // Save image
        BitmapDrawable drawable = (BitmapDrawable) mCardCoverView.getDrawable();
        if (drawable != null && drawable.getBitmap() != null) {
            Bitmap bitmap = drawable.getBitmap();
            String imageName = "1.png";
            PhotoUtil.saveBitmap(imagesFolder, imageName, bitmap, true);
            mLibraryCard.images.add(imagesFolder + "/" + imageName);
        }

        // Save audio
        FileService fileService = new FileService(this);
        try {
            String newAudioName = audiosFolder + "/1.mp3";
            File oldAudioFile = new File(mAudioFile);
            oldAudioFile.renameTo(new File(newAudioName));
//            fileService.save(newAudioName, fileService.read(mAudioFile));
            mLibraryCard.audios.add(newAudioName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save new card into cache
        List<CardItem> cardItemList =  LittleWaterUtility.getMaterialLibraryCardsList(mMaterialLibraryName);
        cardItemList.add(mLibraryCard);
        LittleWaterUtility.setMaterialLibraryCardsList(mMaterialLibraryName, cardItemList);
        Intent data = new Intent();
        data.putExtra("library_card", mLibraryCard);
        setResult(Activity.RESULT_OK, data);
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

}
