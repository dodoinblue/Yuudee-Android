/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package children.com.littlewalter;

import android.content.Context;
import android.pattern.adapter.BaseListAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by peter on 15/3/2.
 */
public class LittleWalterAdapter extends BaseListAdapter {

    public LittleWalterAdapter(Context context, ArrayList<LittleWalter> walterList) {
        super(context, walterList);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_little_walter, parent, false);
        }
        return convertView;
    }
}