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
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 3/10/15.
 */
public class NewMaterialLibraryCardActivity extends BaseLittleWaterActivity implements TextWatcher, MediaPlayer.OnCompletionListener {
    protected EditText mNameEditView;
    protected TextView mCardNameView;
    private ViewGroup mCoverContainer;
    private ViewGroup mSoundContainer;
    private TextView mRecordDescription;
    private ViewGroup mPreviousContainer;
    private ViewGroup mNextContainer;
    private ActionWindow mNewResourceWindow;
    protected Button mChooseCategoryBtn;

    private Button mRecordButton;
    private TextView mRecordNoticeView;
    private boolean mIsRecording;
    private RecordService mRecordService;
    private Button mPlayButton;
    private boolean mIsPlaying;
    private MediaPlayer mPlayer;
    protected String mAudioFile;
    protected CardItem mLibraryCard = new CardItem();
    private String mMaterialLibraryPath;
    private String mOriginLibraryName;

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
        mCardNameView = (TextView) findViewById(R.id.card_name);
        mCoverContainer = (ViewGroup) findViewById(R.id.cover_container);
        mSoundContainer = (ViewGroup) findViewById(R.id.sound_container);
        mRecordDescription = (TextView) findViewById(R.id.record_description);
        mPreviousContainer = (ViewGroup) findViewById(R.id.previous_container);
        mNextContainer = (ViewGroup) findViewById(R.id.next_container);
        mChooseCategoryBtn = (Button) findViewById(R.id.choose_category);
        mCardCoverView = (ImageView) findViewById(R.id.card_cover);
        updateToRoundImageDrawable(mCardCoverView);

        mRecordButton = (Button)findViewById(R.id.record_sound);
        mPlayButton = (Button)findViewById(R.id.play_sound);
        mRecordNoticeView = (TextView) findViewById(R.id.record_notice);
        mRecordNoticeView.setText("准备");
    }

    protected void initEvents() {
        mOriginLibraryName = getIntent().getStringExtra("material_library");
        String libraryName = mOriginLibraryName;
        if (TextUtils.isEmpty(libraryName)) {
            libraryName = "未分类";
        }
        mMaterialLibraryPath = LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + libraryName + "/";
        mChooseCategoryBtn.setText(libraryName);
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
                String newCardName = mNameEditView.getEditableText().toString();
                String newLibraryName = mChooseCategoryBtn.getText().toString();
                if (TextUtils.isEmpty(newCardName)) {
                    showCustomToast("请输入卡片名称");
                    return;
                }

                if (cardNameChanged(mLibraryCard.name, newCardName) || libraryNameChanged(mOriginLibraryName, newLibraryName)
                        || !(this instanceof EditMaterialLibraryCardActivity)) {
                    ArrayList<CardItem> libraryCardList = LittleWaterUtility.getMaterialLibraryCardsList(newLibraryName);
                    CardItem item = new CardItem();
                    item.name = newCardName;
                    if (libraryCardList.contains(item)) {
                        showCustomToast("卡片名称已存在, 请换个名称.");
                        return;
                    }
                }

                mCoverContainer.setVisibility(View.GONE);
                mSoundContainer.setVisibility(View.VISIBLE);
                mRecordDescription.setVisibility(View.VISIBLE);
                mPreviousContainer.setVisibility(View.GONE);
                mNextContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.previous:
                mCoverContainer.setVisibility(View.VISIBLE);
                mSoundContainer.setVisibility(View.GONE);
                mRecordDescription.setVisibility(View.GONE);
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
                        String libraryName = getItem(position);
                        mMaterialLibraryPath = LittleWaterConstant.MATERIAL_LIBRARIES_DIRECTORY + libraryName + "/";
                        categoryView.setText(libraryName);
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
                if (mAudioFile == null) {
                    showCustomToast("请先点击录音按钮录一段语音.");
                    return;
                }
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
                if (mAudioFile == null) {
                    return;
                }
                File file = new File(mAudioFile);
                file.delete();
                showCustomToast("录音已删除.");
                mAudioFile = null;
                break;
        }
    }

    private boolean cardNameChanged(String oldCardName, String newCardName) {
        return !TextUtils.isEmpty(oldCardName) && !TextUtils.equals(oldCardName, newCardName);
    }

    private boolean libraryNameChanged(String oldLibraryName, String newLibraryName) {
        return !TextUtils.isEmpty(oldLibraryName) && !TextUtils.equals(newLibraryName, oldLibraryName);
    }

    private void saveNewLibraryCard() {
        String newLibraryName = mChooseCategoryBtn.getText().toString();
        if (libraryNameChanged(mOriginLibraryName, newLibraryName)) {
            List<CardItem> cardItemList =  LittleWaterUtility.getMaterialLibraryCardsList(mOriginLibraryName);
            cardItemList.remove(mLibraryCard);
            LittleWaterUtility.setMaterialLibraryCardsList(mOriginLibraryName, cardItemList);
        }
        String oldCardName = mLibraryCard.name;
        String newCardName = mCardNameView.getText().toString();

        // Save image
        if (mGotACardCover || mLibraryCard.images.size() == 0) {
            BitmapDrawable drawable = (BitmapDrawable) mCardCoverView.getDrawable();
            if (drawable != null && drawable.getBitmap() != null) {
                String imagesFolder = mMaterialLibraryPath + newCardName + "/images";
                File imagesFile = new File(imagesFolder);
                if (!imagesFile.exists()) {
                    imagesFile.mkdirs();
                }

                Bitmap bitmap = drawable.getBitmap();
                String imageName = "1.png";
                PhotoUtil.saveBitmap(imagesFolder, imageName, bitmap, true);
                mLibraryCard.images.clear();
                mLibraryCard.images.add(imagesFolder + "/" + imageName);
            }
        }

        // Save audio
        if (mAudioFile != null) {
            try {
                String audiosFolder = mMaterialLibraryPath + newCardName + "/audios";
                File audiosFile = new File(audiosFolder);
                if (!audiosFile.exists()) {
                    audiosFile.mkdirs();
                }

                String newAudioName = audiosFolder + "/1.mp3";
                File newAudioFile = new File(newAudioName);
                if (newAudioFile.exists()) {
                    newAudioFile.delete();
                }
                File oldAudioFile = new File(mAudioFile);
                oldAudioFile.renameTo(new File(newAudioName));
                mLibraryCard.audios.clear();
                mLibraryCard.audios.add(newAudioName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Save new card into cache
        mLibraryCard.libraryName = newLibraryName;
        mLibraryCard.name = newCardName;
        mLibraryCard.editable = true;
        List<CardItem> cardItemList =  LittleWaterUtility.getMaterialLibraryCardsList(newLibraryName);
        if (cardItemList.contains(mLibraryCard)) {
            cardItemList.set(cardItemList.indexOf(mLibraryCard), mLibraryCard);
        } else {
            cardItemList.add(mLibraryCard);
        }
        LittleWaterUtility.setMaterialLibraryCardsList(newLibraryName, cardItemList);

        Intent data = new Intent();
        if (cardNameChanged(oldCardName, newCardName)) {
            data.putExtra("old_library_name", oldCardName);
        }
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
        mCardNameView.setText(mNameEditView.getText().toString());
    }

}
