package android.pattern.widget;

import android.pattern.BaseActivity;
import android.pattern.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class BaseAlertDialogFragment extends DialogFragment {
    protected BaseActivity mActivity = null;
    public final static String ALERT_DIALOG_TITLE = "alert_dialog_title";
    public final static String ALERT_DIALOG_MESSAGE = "alert_dialog_message";

    public static BaseAlertDialogFragment newInstance(int title) {
        BaseAlertDialogFragment frag = new BaseAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ALERT_DIALOG_TITLE, title);
        frag.setArguments(args);
        return frag;
    }

    public static BaseAlertDialogFragment newInstance(Bundle args) {
        BaseAlertDialogFragment frag = new BaseAlertDialogFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        int title = getArguments().getInt(ALERT_DIALOG_TITLE);
        if (title == R.string.ui_activity_no_network) {
            builder.setMessage(R.string.ui_activity_no_network);
            builder.setTitle(R.string.ui_activity_warning);
            builder.setNegativeButton(R.string.ui_activity_cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    mActivity.dismissAlertDialog();
                }
            });
        }
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

}