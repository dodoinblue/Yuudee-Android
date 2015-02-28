package android.pattern.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.pattern.BaseActivity;
import android.pattern.R;
import android.pattern.adapter.LocationExpandableListAdapter;
import android.pattern.adapter.LocationExpandableListAdapter.ChildItem;
import android.pattern.adapter.LocationExpandableListAdapter.GroupItem;
import android.pattern.provider.LocationDBManager;
import android.pattern.widget.AnimatedExpandableListView;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public class LocationSelectActivity extends BaseActivity {
    private AnimatedExpandableListView mListView;
    private LocationDBManager dbm;
    private SQLiteDatabase db;
    private SparseArray<GroupItem> mLocationList = new SparseArray<GroupItem>();
    private LocationExpandableListAdapter mAdapter;
    private int mExpandedGroupPosition;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_location_select);
        initViews();
        initEvents();
    }

    @Override
    protected void initViews() {
        mListView = (AnimatedExpandableListView) findViewById(R.id.list_view);
        mAdapter = new LocationExpandableListAdapter(this, mLocationList);
        mListView.setAdapter(mAdapter);

        // In order to show animations, we need to use a custom click handler
        // for our ExpandableListView.
        mListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // We call collapseGroupWithAnimation(int) and
                // expandGroupWithAnimation(int) to animate group
                // expansion/collapse.
                if (mListView.isGroupExpanded(groupPosition)) {
                    mListView.collapseGroupWithAnimation(groupPosition);
                } else {
                    mListView.collapseGroupWithAnimation(mExpandedGroupPosition);
                    mListView.expandGroupWithAnimation(groupPosition);
                    mExpandedGroupPosition = groupPosition;
                }
                return true;
            }
        });
        mListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                GroupItem groupItem = mLocationList.valueAt(groupPosition);
                ChildItem childItem = mLocationList.valueAt(groupPosition).items.valueAt(childPosition);
                Intent data = new Intent();
                data.putExtra("province", groupItem.title);
                data.putExtra("city", childItem.title);
                setResult(Activity.RESULT_OK, data);
                finish();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void initEvents() {
        initProvince();
        int count = mLocationList.size();
        for (int i = 0 ; i < count; i++) {
            GroupItem item = mLocationList.valueAt(i);
            initCity(item.hint);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void initProvince() {
        dbm = new LocationDBManager(this);
        dbm.openDatabase();
        db = dbm.getDatabase();
        try {
            String sql = "select * from province";
            Cursor cursor = db.rawQuery(sql,null);
            while (cursor.moveToNext()){
                String code=cursor.getString(cursor.getColumnIndex("code"));
                byte bytes[]=cursor.getBlob(2);
                String name=new String(bytes,"gbk");
                GroupItem groupItem=new GroupItem();
                groupItem.title = name;
                groupItem.hint = code;
                mLocationList.put(Integer.valueOf(code), groupItem);
            }
        } catch (Exception e) {
        }
        dbm.closeDatabase();
        db.close();

    }
    public void initCity(String provinceCode){
        dbm = new LocationDBManager(this);
        dbm.openDatabase();
        db = dbm.getDatabase();
        try {
            String sql = "select * from city where pcode='"+provinceCode+"'";
            Cursor cursor = db.rawQuery(sql,null);
            while (cursor.moveToNext()){
                String code=cursor.getString(cursor.getColumnIndex("code"));
                byte bytes[]=cursor.getBlob(2);
                String name=new String(bytes,"gbk");
                ChildItem childItem = new ChildItem();
                childItem.title = name;
                childItem.hint = code;
                mLocationList.get(Integer.valueOf(provinceCode)).items.put(Integer.valueOf(code), childItem);
            }
        } catch (Exception e) {
        }
        dbm.closeDatabase();
        db.close();
    }
}