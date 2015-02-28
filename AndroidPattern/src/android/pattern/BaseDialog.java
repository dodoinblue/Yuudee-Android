/*
 * Copyright (c) 2015 著作权由郑志佳所有。著作权人保留一切权利。
 *
 * 这份授权条款，在使用者符合以下三条件的情形下，授予使用者使用及再散播本
 * 软件包装原始码及二进位可执行形式的权利，无论此包装是否经改作皆然：
 *
 * * 对于本软件源代码的再散播，必须保留上述的版权宣告、此三条件表列，以
 *   及下述的免责声明。
 * * 对于本套件二进位可执行形式的再散播，必须连带以文件以及／或者其他附
 *   于散播包装中的媒介方式，重制上述之版权宣告、此三条件表列，以及下述
 *   的免责声明。
 * * 未获事前取得书面许可，不得使用郑志佳或本软件贡献者之名称，
 *   来为本软件之衍生物做任何表示支持、认可或推广、促销之行为。
 *
 * 免责声明：本软件是由郑志佳及本软件之贡献者以现状（"as is"）提供，
 * 本软件包装不负任何明示或默示之担保责任，包括但不限于就适售性以及特定目
 * 的的适用性为默示性担保。郑志佳及本软件之贡献者，无论任何条件、
 * 无论成因或任何责任主义、无论此责任为因合约关系、无过失责任主义或因非违
 * 约之侵权（包括过失或其他原因等）而起，对于任何因使用本软件包装所产生的
 * 任何直接性、间接性、偶发性、特殊性、惩罚性或任何结果的损害（包括但不限
 * 于替代商品或劳务之购用、使用损失、资料损失、利益损失、业务中断等等），
 * 不负任何责任，即在该种使用已获事前告知可能会造成此类损害的情形下亦然。
 */

package android.pattern;

import android.pattern.R;
import android.pattern.widget.HandyTextView;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class BaseDialog extends Dialog implements android.view.View.OnClickListener {

    private final Context mContext;// 上下文
    private LinearLayout mLayoutRoot;// 总体根布局
    private LinearLayout mLayoutTop;// 头部根布局
    private LinearLayout mLayoutTitle;// 标题根布局
    private HandyTextView mHtvTitle;// 标题
    private View mViewTitleLine;// 标题分割线
    private LinearLayout mLayoutContent;// 内容根布局
    private HandyTextView mHtvMessage;// 内容
    private LinearLayout mLayoutBottom;// 底部根布局
    private Button mBtnButton1;// 底部按钮1
    private Button mBtnButton2;// 底部按钮2
    private Button mBtnButton3;// 底部按钮3

    private static BaseDialog mBaseDialog;// 当前的对话框
    private OnClickListener mOnClickListener1;// 按钮1的单击监听事件
    private OnClickListener mOnClickListener2;// 按钮2的单击监听事件
    private OnClickListener mOnClickListener3;// 按钮3的单击监听事件

    public BaseDialog(Context context) {
        super(context, R.style.Theme_Light_FullScreenDialogAct);
        mContext = context;
        setContentView(R.layout.common_dialog_generic);
        initViews();
        initEvents();
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    private void initViews() {
        mLayoutRoot = (LinearLayout) findViewById(R.id.dialog_generic_layout_root);
        mLayoutTop = (LinearLayout) findViewById(R.id.dialog_generic_layout_top);
        mLayoutTitle = (LinearLayout) findViewById(R.id.dialog_generic_layout_title);
        mHtvTitle = (HandyTextView) findViewById(R.id.dialog_generic_htv_title);
        mViewTitleLine = findViewById(R.id.dialog_generic_view_titleline);
        mLayoutContent = (LinearLayout) findViewById(R.id.dialog_generic_layout_content);
        mHtvMessage = (HandyTextView) findViewById(R.id.dialog_generic_htv_message);
        mLayoutBottom = (LinearLayout) findViewById(R.id.dialog_generic_layout_bottom);
        mBtnButton1 = (Button) findViewById(R.id.dialog_generic_btn_button1);
        mBtnButton2 = (Button) findViewById(R.id.dialog_generic_btn_button2);
        mBtnButton3 = (Button) findViewById(R.id.dialog_generic_btn_button3);
        mLayoutRoot.setVisibility(View.VISIBLE);
        setTitleLineVisibility(View.VISIBLE);

    }

    private void initEvents() {
        mBtnButton1.setOnClickListener(this);
        mBtnButton2.setOnClickListener(this);
        mBtnButton3.setOnClickListener(this);
    }

    /**
     * 填充新布局到内容布局
     * 
     * @param resource
     */
    public void setDialogContentView(int resource) {
        View v = LayoutInflater.from(mContext).inflate(resource, null);
        if (mLayoutContent.getChildCount() > 0) {
            mLayoutContent.removeAllViews();
        }
        mLayoutContent.addView(v);
    }

    /**
     * 填充新布局到内容布局
     * 
     * @param resource
     * @param params
     */
    public void setDialogContentView(int resource, LinearLayout.LayoutParams params) {
        View v = LayoutInflater.from(mContext).inflate(resource, null);
        if (mLayoutContent.getChildCount() > 0) {
            mLayoutContent.removeAllViews();
        }
        mLayoutContent.addView(v, params);
    }

    public static BaseDialog getDialog(Context context, CharSequence title, CharSequence message) {
        return getDialog(context, title, message, null, null, null, null, null, null);
    }

    public static BaseDialog getDialog(Context context, CharSequence title, CharSequence message, CharSequence button1,
            DialogInterface.OnClickListener listener1) {
        return getDialog(context, title, message, button1, listener1, null, null, null, null);
    }

    public static BaseDialog getDialog(Context context, CharSequence title, CharSequence message, CharSequence button1,
            DialogInterface.OnClickListener listener1, CharSequence button2, DialogInterface.OnClickListener listener2) {
        return getDialog(context, title, message, button1, listener1, button2, listener2, null, null);
    }

    public static BaseDialog getDialog(Context context, CharSequence title, CharSequence message, CharSequence button1,
            DialogInterface.OnClickListener listener1, CharSequence button2, DialogInterface.OnClickListener listener2,
            CharSequence button3, DialogInterface.OnClickListener listener3) {
        mBaseDialog = new BaseDialog(context);
        if (mBaseDialog.titleAndMessageIsExist(title, message)) {
            mBaseDialog.setTitle(title);
            mBaseDialog.setMessage(message);
        }
        if (mBaseDialog.buttonIsExist(button1, listener1, button2, listener2, button3, listener3)) {
            mBaseDialog.setButton1(button1, listener1);
            mBaseDialog.setButton2(button2, listener2);
            mBaseDialog.setButton3(button3, listener3);
        }
        mBaseDialog.setCancelable(true);
        mBaseDialog.setCanceledOnTouchOutside(true);
        return mBaseDialog;
    }

    public boolean titleAndMessageIsExist(CharSequence title, CharSequence message) {
        if (title == null && message == null) {
            mLayoutTop.setVisibility(View.GONE);
            return false;
        } else {
            mLayoutTop.setVisibility(View.VISIBLE);
            return true;
        }
    }

    @Override
    public void setTitle(CharSequence text) {
        if (text != null) {
            mLayoutTitle.setVisibility(View.VISIBLE);
            mHtvTitle.setText(text);
        } else {
            mLayoutTitle.setVisibility(View.GONE);
        }
    }

    public void setMessage(CharSequence text) {
        if (text != null) {
            mLayoutContent.setVisibility(View.VISIBLE);
            mHtvMessage.setText(text);
        } else {

            mLayoutContent.setVisibility(View.GONE);
        }
    }

    public boolean buttonIsExist(CharSequence button1, DialogInterface.OnClickListener listener1, CharSequence button2,
            DialogInterface.OnClickListener listener2, CharSequence button3, DialogInterface.OnClickListener listener3) {
        if ((button1 != null && listener1 != null) || (button2 != null && listener2 != null)
                || (button3 != null && listener3 != null)) {
            mLayoutBottom.setVisibility(View.VISIBLE);
            return true;
        } else {
            mLayoutBottom.setVisibility(View.GONE);
            return false;
        }
    }

    public void setButton1(CharSequence text, DialogInterface.OnClickListener listener) {
        if (text != null && listener != null) {
            mLayoutBottom.setVisibility(View.VISIBLE);
            mBtnButton1.setVisibility(View.VISIBLE);
            mBtnButton1.setText(text);
            mOnClickListener1 = listener;
        } else {
            mBtnButton1.setVisibility(View.GONE);
        }
    }

    public void setButton2(CharSequence text, DialogInterface.OnClickListener listener) {
        if (text != null && listener != null) {
            mLayoutBottom.setVisibility(View.VISIBLE);
            mBtnButton2.setVisibility(View.VISIBLE);
            mBtnButton2.setText(text);
            mOnClickListener2 = listener;
        } else {
            mBtnButton2.setVisibility(View.GONE);
        }
    }

    public void setButton3(CharSequence text, DialogInterface.OnClickListener listener) {
        if (text != null && listener != null) {
            mLayoutBottom.setVisibility(View.VISIBLE);
            mBtnButton3.setVisibility(View.VISIBLE);
            mBtnButton3.setText(text);
            mOnClickListener3 = listener;
        } else {
            mBtnButton3.setVisibility(View.GONE);
        }
    }

    public void setButton1Background(int id) {
        mBtnButton1.setBackgroundResource(id);
    }

    public void setButton2Background(int id) {
        mBtnButton2.setBackgroundResource(id);
    }

    public void setButton3Background(int id) {
        mBtnButton3.setBackgroundResource(id);
    }

    public void setTitleLineVisibility(int visibility) {
        mViewTitleLine.setVisibility(visibility);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialog_generic_btn_button1) {
            if (mOnClickListener1 != null) {
                mOnClickListener1.onClick(mBaseDialog, 0);
            }
        } else if (id == R.id.dialog_generic_btn_button2) {
            if (mOnClickListener2 != null) {
                mOnClickListener2.onClick(mBaseDialog, 1);
            }
        } else if (id == R.id.dialog_generic_btn_button3) {
            if (mOnClickListener3 != null) {
                mOnClickListener3.onClick(mBaseDialog, 2);
            }
        }
    }
}
