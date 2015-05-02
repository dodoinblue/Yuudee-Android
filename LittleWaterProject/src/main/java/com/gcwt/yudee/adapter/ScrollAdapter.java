/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.gcwt.yudee.adapter;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gcwt.yudee.OnDataChangeListener;
import com.gcwt.yudee.R;
import com.gcwt.yudee.activity.LittleWaterActivity;
import com.gcwt.yudee.activity.MaterialLibraryCardsActivity;
import com.gcwt.yudee.activity.ShowCardActivity;
import com.gcwt.yudee.activity.SubFolderLittleWaterActivity;
import com.gcwt.yudee.model.CardItem;
import com.gcwt.yudee.util.LittleWaterConstant;
import com.gcwt.yudee.util.LittleWaterUtility;
import com.gcwt.yudee.widget.ScrollLayout;
import com.gcwt.yudee.widget.ScrollLayout.SAdapter;
import com.umeng.analytics.MobclickAgent;

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
    private View mHiddenCard;

//    public static final int DISPLAY_MODE_NORMAL = 0;
//    public static final int DISPLAY_MODE_EDIT = 1;
//    public static final int DISPLAY_MODE_SELECT = 2;
//
//    private int mDisplayMode;

	public ScrollAdapter(ScrollLayout layout, List<CardItem> list) {
		mContext = layout.getContext();
		mInflater = LayoutInflater.from(mContext);
        mScrollLayout = layout;
		mList = list;
	    mCache = new HashMap<String, SoftReference<Drawable>>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        final Resources resources = mContext.getResources();
        final CardItem moveItem = mList.get(position);
        if (!moveItem.getIsEmpty()) {
            int layoutRes = 0;
            int categoryBottomMargin;
            if (mScrollLayout.getColCount() == LittleWaterActivity.LAYOUT_TYPE_2_X_2) {
                categoryBottomMargin = resources.getDimensionPixelSize(R.dimen.cagegory_item_name_padding_bottom_2x2);
                layoutRes = R.layout.card_item;
            } else {
                categoryBottomMargin = resources.getDimensionPixelSize(R.dimen.cagegory_item_name_padding_bottom_1x1);
                layoutRes = R.layout.big_card_item;
            }
			final View view = mInflater.inflate(layoutRes, parent, false);
            TextView nameView = (TextView) view.findViewById(R.id.card_name);
            nameView.setText(LittleWaterUtility.getCardDisplayName(moveItem.getName()));

			final ImageView iv = (ImageView) view.findViewById(R.id.content_iv);
            String coverUrl;
            View cardBgView = view.findViewById(R.id.card_bg);
            if (moveItem.isLibrary) {
                coverUrl = moveItem.getCover();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)nameView.getLayoutParams();
                params.setMargins(0, 0, 0, categoryBottomMargin);
                if (mScrollLayout.getColCount() == LittleWaterActivity.LAYOUT_TYPE_2_X_2) {
                    cardBgView.setBackgroundResource(R.mipmap.cat_bg);
                } else {
                    cardBgView.setBackgroundResource(R.mipmap.big_cat_bg);
                }
            } else {
                coverUrl = moveItem.getImages().get(0);
            }

            Drawable cardCover = null;
//            SoftReference<Drawable> cover = mCache.get(coverUrl);
//            if (cover != null) {
//                cardCover = cover.get();
//            }
//
//            if (cardCover == null) {
                cardCover = LittleWaterUtility.getRoundCornerDrawableFromSdCard(coverUrl);
//                mCache.put(coverUrl, new SoftReference<Drawable>(cardCover));
//            }

            iv.setImageDrawable(cardCover);
			view.setTag(moveItem);
            if (mContext instanceof MaterialLibraryCardsActivity) {
                return view;
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LittleWaterActivity.mIsInParentMode) {
                        if (!moveItem.isLibrary) {
                            return;
                        }
                    }

                    //友盟统计用户使用的官方卡片/自制卡片的使用情况
                    if (moveItem.editable) {
                        MobclickAgent.onEvent(mContext,"Personal-Card");
                    } else {
                        MobclickAgent.onEvent(mContext,"Official-Card");
                    }

                    if (moveItem.isLibrary) {
                        Intent intent = new Intent(mContext, SubFolderLittleWaterActivity.class);
                        intent.putExtra("library", moveItem);
                        mScrollLayout.mActivity.startActivity(intent);
                    } else if (mScrollLayout.getColCount() == LittleWaterActivity.LAYOUT_TYPE_1_X_1) {
                        switch (moveItem.getCardSettings().getAnimationType()) {
                            case LittleWaterConstant.ANIMATION_NONE:
                            case LittleWaterConstant.ANIMATION_ZOOM_IN:
                                LittleWaterUtility.playCardByFlippingAnimation(mContext, view, moveItem);
                                break;
                            case LittleWaterConstant.ANIMATION_ZOOM_IN_AND_ROTATE:
                                LittleWaterUtility.playCardByRotateAndFlippingAnimation(mContext, view, moveItem);
                                break;
                        }
                    } else {
                        switch (moveItem.getCardSettings().getAnimationType()) {
                            case LittleWaterConstant.ANIMATION_NONE:
                                LittleWaterUtility.playCardByFlippingAnimation(mContext, view, moveItem);
                                break;
                            case LittleWaterConstant.ANIMATION_ZOOM_IN:
                            case LittleWaterConstant.ANIMATION_ZOOM_IN_AND_ROTATE:
                                if (mHiddenCard != null) {
                                    mHiddenCard.setVisibility(View.VISIBLE);
                                }
                                mHiddenCard = view;
                                view.setVisibility(View.INVISIBLE);
                                Intent intent = new Intent(mContext, ShowCardActivity.class);
                                intent.putExtra("card_item", moveItem);
                                ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                                //ActivityOptions opts = ActivityOptions.makeCustomAnimation(mContext, R.anim.zoom_enter, R.anim.dialog_exit);
                                mScrollLayout.mActivity.startActivityForResult(intent, LittleWaterConstant.ACTIVITY_REQUEST_CODE_SHOW_CARD, opts.toBundle());
                                break;
                        }
                    }
                }
            });
            return view;
		} else {
            ImageView view = (ImageView) mInflater.inflate(R.layout.layout_empty_card_position, mScrollLayout, false);
            if (mScrollLayout.getColCount() == LittleWaterActivity.LAYOUT_TYPE_1_X_1) {
                int padding = resources.getDimensionPixelSize(R.dimen.big_empty_card_padding);
                view.setPadding(padding, padding, padding, padding);

            }
            view.setTag(moveItem);
            if (LittleWaterActivity.mIsInParentMode) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.INVISIBLE);
            }
            return view;
        }
	}

    public void displayBackAfterShowCard() {
        mScrollLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mHiddenCard != null) {
                    mHiddenCard.setVisibility(View.VISIBLE);
                    mHiddenCard = null;
                }
            }
        }, 250);
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
