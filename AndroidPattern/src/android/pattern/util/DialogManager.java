package android.pattern.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.pattern.R;
import android.text.*;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 绫诲悕锛氬璇濇绠＄悊 浣滅敤锛�1.鎻愮ず瀵硅瘽妗嗗畾涔�2.纭瀵硅瘽妗嗗畾涔�3.toast淇℃伅妗嗗畾涔�
 * 
 * 
 */
public class DialogManager {
	public static PopupWindow my_dialog;

	public static void showAlertDialog(Context context, String title,
			String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(context)
				.create();
		View vDialog = LayoutInflater.from(context).inflate(
				R.layout.dialog_alert_pop_up, null);
		alertDialog.setView(vDialog, 0, 0, 0, 0);

		TextView tvTitle = (TextView) vDialog
				.findViewById(R.id.tv_dialog_title);
		if (android.text.TextUtils.isEmpty(title)) {
			tvTitle.setVisibility(View.GONE);
		} else {
			tvTitle.setText(title);
		}

		TextView tvMessage = (TextView) vDialog
				.findViewById(R.id.tv_dialog_message);
		if (!android.text.TextUtils.isEmpty(message)) {
			tvMessage.setText(message);
		} else {
			tvMessage.setVisibility(View.INVISIBLE);
		}

		vDialog.findViewById(R.id.btn_dialog_confirm_submit)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						alertDialog.dismiss();
					}
				});

		alertDialog.setCancelable(true);

		alertDialog.show();
		alertDialog.getWindow().setLayout(
				WindowManager.LayoutParams.FILL_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	/**
	 * 未登录确认对话框
	 * 
	 * @author jqc
	 * @date 2013-08-27
	 * @modify
	 */
	public static void showLoginDialog(View paView, Context context,
			String message, final OnBtnClickListener okClickListener,
			final OnBtnClickListener cancleClickListener) {

		final PopupWindow window = new PopupWindow(context);
		View view = View.inflate(context, R.layout.dialog_confirm_login, null);

		window.setContentView(view);
		window.setWidth(LayoutParams.MATCH_PARENT);
		window.setHeight(LayoutParams.MATCH_PARENT);
		window.setBackgroundDrawable(new BitmapDrawable());
		window.setFocusable(true);
		window.setTouchable(true);
		window.setOutsideTouchable(true);

		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		TextView tv_message = (TextView) view
				.findViewById(R.id.tv_dialog_login);
		if (message.equals("")) {
			tv_message.setVisibility(View.GONE);
		} else {
			tv_message.setText(message);
		}
		final RelativeLayout rl_partent = (RelativeLayout) view
				.findViewById(R.id.login_partent);

		rl_partent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				window.dismiss();
			}
		});
		// rl_partent.getBackground().setAlpha(50);
		final Button btnOk = (Button) view
				.findViewById(R.id.btn_dialog_login_submit);
		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				okClickListener.OnClick(btnOk);
				window.dismiss();
			}
		});
		final Button btncancle = (Button) view
				.findViewById(R.id.btn_dialog_login_cancel);
		btncancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				cancleClickListener.OnClick(btncancle);
				window.dismiss();
			}
		});
		window.showAtLocation(paView, Gravity.CENTER, 0, 0);
	}

	/**
	 * 确认对话框
	 * 
	 * @author jqc
	 * @date 2013-08-27
	 * @modify
	 */
	public static AlertDialog showConfirmDialog(Context context, String title,
			String message, final DialogInterface.OnClickListener okClicked,
			final DialogInterface.OnClickListener cancelClicked) {

		final AlertDialog alertDialog = new AlertDialog.Builder(context)
				.create();
		View vDialog = LayoutInflater.from(context).inflate(
				R.layout.dialog_confirm_up, null);
		alertDialog.setView(vDialog, 0, 0, 0, 0);
		TextView tvTitle = (TextView) vDialog
				.findViewById(R.id.tv_dialog_title);
		if (title == null || title.equals("")) {
			tvTitle.setVisibility(View.GONE);
			;
		} else {
			tvTitle.setText(title);
		}

		TextView tvMessage = (TextView) vDialog
				.findViewById(R.id.tv_dialog_message);
		if (message != null) {
			tvMessage.setText(message);
		}

		vDialog.findViewById(R.id.btn_dialog_confirm_submit)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (okClicked == null) {

						} else {
							okClicked.onClick(alertDialog,
									DialogInterface.BUTTON_POSITIVE);
						}
					}
				});
		vDialog.findViewById(R.id.btn_dialog_confirm_cancel)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (cancelClicked == null) {
							alertDialog.dismiss();
						} else {
							cancelClicked.onClick(alertDialog,
									DialogInterface.BUTTON_NEGATIVE);
						}
					}
				});

		alertDialog.setCancelable(true);
		// Showing Alert Message
		alertDialog.show();
		alertDialog.getWindow().setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
		return alertDialog;
	}

	/**
	 * 自定义Toast
	 * 
	 * 
	 */
	public static void showTipMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void showAAsiant(View paView, Context context,
			final OnBtnClickListener btnClickListener,
			final OnBtnClickListener parClickListener) {

		final PopupWindow window = new PopupWindow(context);
		View view = View.inflate(context, R.layout.pop_my_yindao, null);

		window.setContentView(view);
		window.setWidth(LayoutParams.MATCH_PARENT);
		window.setHeight(LayoutParams.MATCH_PARENT);
		window.setBackgroundDrawable(new BitmapDrawable());
		window.setFocusable(true);
		window.setTouchable(true);
		window.setOutsideTouchable(true);
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		final RelativeLayout rl_partent = (RelativeLayout) view
				.findViewById(R.id.rl_partent_wanshan);
		rl_partent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				parClickListener.OnClick(rl_partent);
				window.dismiss();
			}
		});
		// rl_partent.getBackground().setAlpha(50);
		final ImageView ivView = (ImageView) view.findViewById(R.id.iv_wanshan);
		ivView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				btnClickListener.OnClick(ivView);
				window.dismiss();
			}
		});

		window.showAtLocation(paView, Gravity.CENTER, 0, 0);
	}

	// 1．定义接口
	public interface OnBtnClickListener {

		public void OnClick(View v);

	}
}
