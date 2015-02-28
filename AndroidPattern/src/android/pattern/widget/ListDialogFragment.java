package android.pattern.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.pattern.adapter.BaseListAdapter;


public class ListDialogFragment<E> extends DialogFragment {
    private final BaseListAdapter<E> mListAdapter;
    private String mTitle;
    private DialogInterface.OnClickListener mOnChooseListener;

    public ListDialogFragment(BaseListAdapter<E> fontListAdapter, String title, DialogInterface.OnClickListener listener) {
        mListAdapter = fontListAdapter;
        mTitle = title;
        mOnChooseListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(mListAdapter, 0, mOnChooseListener);
        builder.setTitle(mTitle);
        //            if (mPositiveListener != null) {
        //            	builder.setPositiveButton("确定", mPositiveListener);
        //            }
        //                    new DialogInterface.OnClickListener() {
        //                        @Override
        //                        public void onClick(DialogInterface dialog, int whichButton) {
        //                           dismiss();
        //                        }
        //                    });

        builder.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}