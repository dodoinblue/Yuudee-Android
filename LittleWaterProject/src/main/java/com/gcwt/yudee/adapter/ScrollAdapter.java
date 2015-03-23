/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.pattern.BaseActivity;
import android.pattern.util.BitmapUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.gcwt.yudee.OnDataChangeListener;
import com.gcwt.yudee.R;
import com.gcwt.yudee.activity.LittleWaterActivity;
import com.gcwt.yudee.activity.MaterialLibraryCardsActivity;
import com.gcwt.yudee.activity.ShowCardActivity;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.widget.ScrollLayout;
import com.gcwt.yudee.widget.ScrollLayout.SAdapter;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 3/3/15.
 */
public class ScrollAdapter implements SAdapter {
	private Context mContext;
    private ScrollLayout mScrollLayout;
	private LayoutInflater mInflater;
	protected List<CardItem> mList;
    protected HashMap<String,SoftReference<Drawable>> mCache;

	public ScrollAdapter(ScrollLayout layout, List<CardItem> list) {
		mContext = layout.getContext();
		mInflater = LayoutInflater.from(mContext);
        mScrollLayout = layout;
		mList = list;
	    mCache = new HashMap<String, SoftReference<Drawable>>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position < mList.size()) {
			final CardItem moveItem = mList.get(position);
            int layoutRes = 0;
            if (mScrollLayout.getColCount() == LittleWaterActivity.LAYOUT_TYPE_2_X_2) {
                layoutRes = R.layout.card_item;
            } else {
                layoutRes = R.layout.single_card_item;
            }
			final View view = mInflater.inflate(layoutRes, parent, false);
			final ImageView iv = (ImageView) view.findViewById(R.id.content_iv);
			String coverUrl = moveItem.getImages().get(0);

            Drawable cardCover = null;
            SoftReference<Drawable> cover = mCache.get(coverUrl);
            if (cover != null) {
                cardCover = cover.get();
            }
			
            if (cardCover == null) {
                cardCover = new BitmapDrawable(BitmapUtil.getRoundedCornerBitmap(getBitmapFromSdCard(coverUrl), LittleWaterActivity.sRoundPx));
                mCache.put(coverUrl, new SoftReference<Drawable>(cardCover));
            }

            TextView nameView = (TextView) view.findViewById(R.id.card_name);
            nameView.setText(moveItem.getName());
            iv.setImageDrawable(cardCover);
			view.setTag(moveItem);
            if (mContext instanceof MaterialLibraryCardsActivity) {
                return view;
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.setVisibility(View.INVISIBLE);
//                    playCardByDefaultAnimation(view, moveItem);
                    actionWithScaleUpAnimation(view, moveItem);
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.setVisibility(View.VISIBLE);
                        }
                    }, 500 * moveItem.getImages().size());
                }
            });
            return view;
		} else {
            ImageView view = new ImageView(mContext);
            view.setBackgroundResource(R.mipmap.parent_main_blankcard);
            view.setTag(new CardItem());
            return view;
        }
	}

    private void actionWithScaleUpAnimation(View view, CardItem moveItem) {
        Intent intent = new Intent(mContext, ShowCardActivity.class);
        intent.putExtra("card_item", moveItem);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
        mContext.startActivity(intent, opts.toBundle());
    }

    public static void playCardByDefaultAnimation(final BaseActivity activity, View view, CardItem moveItem) {
        final ImageView iv = (ImageView) view.findViewById(R.id.content_iv);
        final ViewFlipper flipper = (ViewFlipper) view.findViewById(R.id.content_show);
        flipper.setVisibility(View.VISIBLE);
        iv.setVisibility(View.GONE);
        playAudio(moveItem.getAudios().get(0));
        List<String> images = moveItem.getImages();
        flipper.removeAllViews();
        for (String image : images) {
            ImageView imageView = (ImageView) activity.getLayoutInflater().inflate(R.layout.layout_image, flipper, false);
            imageView.setImageDrawable(new BitmapDrawable(BitmapUtil.getRoundedCornerBitmap(getBitmapFromSdCard(image), LittleWaterActivity.sRoundPx)));
            flipper.addView(imageView);
        }
        flipper.startFlipping();
        flipper.postDelayed(new Runnable() {
            @Override
            public void run() {
                flipper.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
                if (activity instanceof ShowCardActivity) {
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                }
            }
        }, 500 * moveItem.getImages().size());
    }

    private static void playAudio(String audioPath) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
//        mediaPlayer.stop();
//        mediaPlayer.release();
    }

    public static Bitmap getBitmapFromSdCard(String imageFilePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        return BitmapFactory.decodeFile(imageFilePath, options);
    }
	
	@Override
	public int getCount() {
//		return mList.size();
        return mScrollLayout.getDisplayCount(mList.size());
	}

	@Override
	public void exchange(int oldPosition, int newPositon) {
		CardItem item = mList.get(oldPosition);
		mList.remove(oldPosition);
		mList.add(newPositon, item);
	}

	private OnDataChangeListener dataChangeListener = null;

	public OnDataChangeListener getOnDataChangeListener() {
		return dataChangeListener;
	}

	public void setOnDataChangeListener(OnDataChangeListener dataChangeListener) {
		this.dataChangeListener = dataChangeListener;
	}

	public void delete(int position) {
		if (position < getCount()) {
			mList.remove(position);
		}
	}

	public void add(CardItem item) {
		mList.add(item);
	}

	public CardItem getMoveItem(int position) {
		return mList.get(position);
	}
	
	public void recycleCache() {
		if (mCache != null) {
			Set<String> keys = mCache.keySet();
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
                String key = it.next();
				SoftReference<Drawable> reference = mCache.get(key);
				if (reference != null) {
					reference.clear();
				}
			}
			mCache.clear();
			mCache = null;
		}
	}
}
