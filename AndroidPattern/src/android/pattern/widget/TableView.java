package android.pattern.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TableView extends LinearLayout {

    private static LayoutParams FILL_FILL_LAYOUTPARAMS = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);
    private static LayoutParams WAP_WAP_LAYOUTPARAMS = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    private static Paint BLACK_PAINT = new Paint();
    private static Paint WHITE_PAINT = new Paint();
    static {
        WHITE_PAINT.setColor(Color.WHITE);
        BLACK_PAINT.setColor(Color.BLACK);
    }

    private final CAdapter cAdapter;

    /** 标题空间. */
    private final LinearLayout titleLayout;
    private final String[] title;

    private final ListView listView;
    /** 数据. */
    private final List<String[]> data;

    /** 列宽数据. */
    private final int[] itemWidth;

    /** 当前选中行. */
    private int selectedPosition = -1;
    /** 自动列宽列. */
    private int autoWidthIndex = -1;

    private AdapterView.OnItemClickListener onItemClickListener;

    /** 行背景颜色. */
    private int[] rowsBackgroundColor;
    /** 选中行背景颜色. */
    private int selectedBackgroundColor = Color.argb(200, 224, 243, 250);
    /** 标题背景颜色. */
    private int titleBackgroundColor;
    /** 标题字体颜色. */
    private int titleTextColor = Color.argb(255, 100, 100, 100);
    /** 内容字体颜色. */
    private int contentTextColor = Color.argb(255, 100, 100, 100);
    /** 标题字体大小. */
    private float titleTextSize = 0;
    /** 内容字体大小. */
    private float contentTextSize = 0;

    /**
     * 初始化带标题ListView
     * 
     * @param context
     *            父级上下文
     * @param title
     *            标题数组
     * @param data
     *            内容列表
     */
    public TableView(Context context, String[] title, List<String[]> data) {
        super(context);
        this.title = title;
        this.data = data;

        // 设定纵向布局
        setOrientation(VERTICAL);
        // 设定背景为白色
        setBackgroundColor(Color.WHITE);

        // 预先设定好每列的宽
        this.itemWidth = new int[title.length];
        autoWidthIndex = this.itemWidth.length - 1;
        // 计算列宽
        calcColumnWidth();

        // 添加title位置
        titleLayout = new LinearLayout(getContext());
        titleLayout.setBackgroundColor(Color.parseColor("#CCCCCC"));
        addView(titleLayout);
        // 绘制标题面板
        drawTitleLayout();

        // 添加listview
        listView = new ListView(getContext());
        listView.setPadding(0, 2, 0, 0);
        cAdapter = new CAdapter();
        listView.setAdapter(cAdapter);
        listView.setCacheColorHint(0);
        listView.setLayoutParams(FILL_FILL_LAYOUTPARAMS);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(parent, view, position, id);
                }
                setSelectedPosition(position);
                selectedPosition = position;
                cAdapter.notifyDataSetChanged();
            }
        });
        addView(listView);
    }

    /**
     * 整体有改变时，刷新显示
     */
    public void definedSetChanged() {
        calcColumnWidth();
        drawTitleLayout();
        cAdapter.notifyDataSetChanged();
    }

    /**
     * 设置选中时的监听器
     * 
     * @param onItemClickListener
     */
    public void setOnItemClickListener(
                    AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 设置行背景颜色, 多个颜色可以作为间隔色
     * 
     * @param color
     *            行背景颜色，可以为多个
     */
    public void setItemBackgroundColor(int... color) {
        rowsBackgroundColor = color;
    }

    /**
     * 数据总数
     */
    public int getCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    /**
     * 当前选中数据
     * 
     * @param position
     * @return
     */
    public String[] getItem(int position) {
        if (data == null) {
            return null;
        }
        return data.get(position);
    }

    /**
     * 设置当前选中位置
     * 
     * @return
     */
    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    /**
     * 当前选中位置
     * 
     * @return
     */
    public int getSelectedPosition() {
        return selectedPosition;
    }

    /**
     * 设置被选中时的背景色
     * 
     * @param color
     */
    public void setSelectedBackgroundColor(int color) {
        selectedBackgroundColor = color;
    }

    /**
     * 设置标题背景色.
     * 
     * @param color
     */
    public void setTitleBackgroundColor(int color) {
        titleBackgroundColor = color;
        titleLayout.setBackgroundColor(titleBackgroundColor);
    }

    /**
     * 设置标题文字颜色
     * 
     * @param color
     */
    public void setTitleTextColor(int color) {
        titleTextColor = color;
        for (int i = 0; i < titleLayout.getChildCount(); i++) {
            ((TextView) titleLayout.getChildAt(i)).setTextColor(titleTextColor);
        }
    }

    /**
     * 设置内容文字颜色
     * 
     * @param color
     */
    public void setContentTextColor(int color) {
        contentTextColor = color;
    }

    /**
     * 设置标题字体大小
     * 
     * @param szie
     */
    public void setTitleTextSize(float szie) {
        titleTextSize = szie;
    }

    /**
     * 设置内容字体大小
     * 
     * @param szie
     */
    public void setContentTextSize(float szie) {
        contentTextSize = szie;
    }

    /**
     * 
     * 设定哪列自动列宽 从0开始计算
     * 
     * @param index
     */
    public void setAutoColumnWidth(int index) {
        autoWidthIndex = index;
        for (int i = 0; i < titleLayout.getChildCount(); i++) {
            TextView tv = ((TextView) titleLayout.getChildAt(i));
            if (i == autoWidthIndex) {
                tv.setLayoutParams(FILL_FILL_LAYOUTPARAMS);
            } else {
                tv.setLayoutParams(WAP_WAP_LAYOUTPARAMS);
                tv.setWidth(itemWidth[i]);
            }
        }
    }

    /**
     * 绘制标题
     */
    private void drawTitleLayout() {
        titleLayout.removeAllViews();
        for (int i = 0; i < title.length; i++) {
            TextView tv = new CTextView(titleLayout.getContext());
            tv.setTextColor(titleTextColor);
            tv.setGravity(Gravity.CENTER);
            tv.setText(title[i]);
            if (titleTextSize > 0) {
                tv.setTextSize(titleTextSize);
            }
            tv.setPadding(5, 0, 5, 0);
            if (i == autoWidthIndex) {
                tv.setLayoutParams(TableView.FILL_FILL_LAYOUTPARAMS);
            } else {
                tv.setWidth(itemWidth[i]);
            }
            titleLayout.addView(tv);
        }
    }

    /**
     * 计算列宽
     * 
     * @return 是否有改动
     */
    private boolean calcColumnWidth() {
        boolean result = false;

        float textSize = new TextView(getContext()).getTextSize();

        // 计算标题列宽
        for (int i = 0; i < itemWidth.length; i++) {
            int w = TableView.GetPixelByText(
                            (titleTextSize > 0) ? titleTextSize : textSize, title[i]);
            if (itemWidth[i] < w) {
                itemWidth[i] = w;
                result = true;
            }
        }

        // 计算内容列宽
        if (contentTextSize > 0) {
            textSize = contentTextSize;
        }
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < itemWidth.length && j < data.get(i).length; j++) {
                int w = TableView
                                .GetPixelByText(textSize, data.get(i)[j]);
                if (itemWidth[j] < w) {
                    itemWidth[j] = w;
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * 计算字符串所占像素
     * 
     * @param textSize
     *            字体大小
     * @param text
     *            字符串
     * @return 字符串所占像素
     */
    private static int GetPixelByText(float textSize, String text) {
        Paint mTextPaint = new Paint();
        mTextPaint.setTextSize(textSize); // 指定字体大小
        mTextPaint.setFakeBoldText(true); // 粗体
        mTextPaint.setAntiAlias(true); // 非锯齿效果

        return (int) (mTextPaint.measureText(text) + textSize);
    }

    /**
     * 主要用的Adapter
     * 
     * @author Cdisk
     * 
     */
    class CAdapter extends BaseAdapter {

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            if (data == null) {
                return 0;
            }
            return data.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            if (data == null) {
                return null;
            }
            return data.get(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // 初始化主layout
            LinearLayout contextLayout = new LinearLayout(
                            TableView.this.getContext());

            String[] dataItem = data.get(position);

            if (getSelectedPosition() == position) { // 为当前选中行
                contextLayout.setBackgroundColor(selectedBackgroundColor);
            } else if (rowsBackgroundColor != null
                            && rowsBackgroundColor.length > 0) {
                contextLayout.setBackgroundColor(rowsBackgroundColor[position
                                                                     % rowsBackgroundColor.length]);
            }

            for (int i = 0; i < title.length; i++) {
                TextView tv = new CTextView(contextLayout.getContext());
                tv.setTextColor(contentTextColor);
                tv.setGravity(Gravity.CENTER);
                if (i < dataItem.length) {
                    tv.setText(dataItem[i]);
                }
                if (i == autoWidthIndex) {
                    tv.setLayoutParams(TableView.FILL_FILL_LAYOUTPARAMS);
                } else {
                    tv.setWidth(itemWidth[i]);
                }
                if (contentTextSize > 0) {
                    tv.setTextSize(contentTextSize);
                }
                contextLayout.addView(tv);
            }

            return contextLayout;
        }

    }

    /**
     * 重写的TextView
     * 
     * @author Cdisk
     */
    class CTextView extends TextView {

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // Top
            canvas.drawLine(0, 0, this.getWidth() - 1, 0, WHITE_PAINT);
            // Left
            canvas.drawLine(0, 0, 0, this.getHeight() - 1, WHITE_PAINT);
            // Right
            canvas.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1,
                            this.getHeight() - 1, BLACK_PAINT);
            // Buttom
            canvas.drawLine(0, this.getHeight() - 1, this.getWidth() - 1,
                            this.getHeight() - 1, BLACK_PAINT);
        }

        public CTextView(Context context) {
            super(context);
            setEllipsize(TextUtils.TruncateAt.END);
            setSingleLine(true);
        }
    }

}