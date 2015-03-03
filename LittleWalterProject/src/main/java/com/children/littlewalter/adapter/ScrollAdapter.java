/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.children.littlewalter.OnDataChangeListener;
import com.children.littlewalter.R;
import com.children.littlewalter.model.CardItem;
import com.children.littlewalter.widget.ScrollLayout.SAdapter;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 3/3/15.
 */
@SuppressLint("UseSparseArrays")
public class ScrollAdapter implements SAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	
	private List<CardItem> mList;
	private HashMap<String,SoftReference<Drawable>> mCache;
	
	public ScrollAdapter(Context context, List<CardItem> list) {
		
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		
		this.mList = list;
	    this.mCache = new HashMap<String, SoftReference<Drawable>>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (position < mList.size()) {
			final CardItem moveItem = mList.get(position);
			view = mInflater.inflate(R.layout.item, parent, false);
			ImageView iv = (ImageView) view.findViewById(R.id.content_iv);
			String coverUrl = moveItem.getImages()[0];

            Drawable cardCover = null;
            SoftReference<Drawable> cover = mCache.get(coverUrl);
            if (cover != null) {
                cardCover = cover.get();
            }
			
            if (cardCover == null) {
                cardCover = new BitmapDrawable(getCardCover(coverUrl));
                mCache.put(coverUrl, new SoftReference<Drawable>(cardCover));
            }

            TextView nameView = (TextView) view.findViewById(R.id.card_name);
            nameView.setText(moveItem.getName());
            iv.setImageDrawable(cardCover);
			view.setTag(moveItem);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playAudio(moveItem.getAudios()[0]);
                }
            });
		}
		return view;
	}

    private void playAudio(String audioPath) {
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

    private Bitmap getCardCover(String imageFilePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        return BitmapFactory.decodeFile(imageFilePath, options);
    }
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public void exchange(int oldPosition, int newPositon) {
		CardItem item = mList.get(oldPosition);
		mList.remove(oldPosition);
		mList.add(newPositon, item);
	}

	private OnDataChangeListener dataChangeListener = null;

//	public interface OnDataChangeListener {
//		void ondataChange();
//
//	}

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
