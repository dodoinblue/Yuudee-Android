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

package android.pattern.widget;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.pattern.R;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class VoiceRecorderLauncher extends Button {
    public VoiceRecorderLauncher(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setVoiceRecordListener(VoiceRecordListener listener) {
        mVoiceListener = listener;
    }

    private File mAudioStoredLocation = null;

    private File mAudioFile = null;

    private VoiceRecordListener mVoiceListener;

    private static final int MIN_INTERVAL_TIME = 2000;
    private long startTime;

    private static Dialog recordIndicator;

    private static int[] res = { R.drawable.amp1, R.drawable.amp2,
            R.drawable.amp3, R.drawable.amp4, R.drawable.amp5, R.drawable.amp6,
            R.drawable.amp7 };

    private static View view;
    private static ViewFlipper viewFlipper;
    private static ImageView volume_anim;
    private static TextView result_text;

    private MediaRecorder recorder;

    private ObtainDecibelThread thread;

    private Handler volumeHandler;

    private boolean mFakeToRecord = true;

    private void init() {
        volumeHandler = new ShowVolumeHandler();
    }

    boolean outside_move = false;
    boolean start_record = false;
    float remY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            setBackgroundResource(R.drawable.conv_msg_send_btn_normal);
            if (!start_record) {
                start_record = true;
                initDialogAndStartRecord();
                viewFlipper.setDisplayedChild(0);
                outside_move = false;
                setText(R.string.conversations_strings_press_to_release);
            }
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            setBackgroundResource(R.drawable.conv_msg_send_btn_pressed);
            if (start_record) {
                start_record = false;
                if (outside_move) {
                    cancelRecord();
                } else {
                    finishRecord();
                }
                setText(R.string.conversations_strings_hold_to_talk_txt);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            Rect rect = new Rect();
            getGlobalVisibleRect(rect);
            if (start_record) {
                if (event.getRawY() < rect.top) {
                    outside_move = true;
                    viewFlipper.setDisplayedChild(1);
                } else {
                    outside_move = false;
                    viewFlipper.setDisplayedChild(0);
                }
            break;
            }
        }



        return true;
    }

    private void initDialogAndStartRecord() {
        startTime = System.currentTimeMillis();

        if (mAudioStoredLocation == null) {
            mAudioStoredLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            if (!mAudioStoredLocation.exists()) {
                mAudioStoredLocation.mkdirs();
            }
        }

        view = View.inflate(getContext(), R.layout.message_voice_recorder_hint_window, null);
        viewFlipper = (ViewFlipper) view
                .findViewById(R.id.voice_recorder_viewflipper);
        result_text = (TextView) view.findViewById(R.id.voice_rcd_result_text);
        viewFlipper.setDisplayedChild(0);
        volume_anim = (ImageView) view.findViewById(R.id.voice_rcd_hint_anim);
        recordIndicator = new Dialog(getContext(), R.style.like_toast_dialog_style);
        recordIndicator.setContentView(view);

        mAudioFile = new File(mAudioStoredLocation, System.currentTimeMillis() + ".amr");

        if (startRecording()) {
            recordIndicator.show();
        } else {
            start_record = false;
        }
        if (mVoiceListener != null) {
            mVoiceListener.onStartedRecord();
        }
    }

    private void finishRecord() {
        stopRecording();

        long intervalTime = System.currentTimeMillis() - startTime;
        if (intervalTime < MIN_INTERVAL_TIME) {

            result_text.setText(R.string.conversations_strings_too_short_to_record);
            viewFlipper.setDisplayedChild(2);
            Message msg = new Message();
            msg.what = 7;
            volumeHandler.sendMessageDelayed(msg, 800);

            mAudioFile.delete();
            if (mVoiceListener != null) {
                mVoiceListener.onFinishedRecord(null);
            }
            return;
        }
        result_text.setText(R.string.conversations_strings_record_finished);
        viewFlipper.setDisplayedChild(2);
        Message msg = new Message();
        msg.what = 7;
        volumeHandler.sendMessageDelayed(msg, 800);
        if (mVoiceListener != null) {
            mVoiceListener.onFinishedRecord(Uri.fromFile(mAudioFile));
        }
    }

    private void cancelRecord() {
        stopRecording();
        result_text.setText(R.string.conversations_strings_record_canceled);
        viewFlipper.setDisplayedChild(2);
        Message msg = new Message();
        msg.what = 7;
        volumeHandler.sendMessageDelayed(msg, 800);
        mAudioFile.delete();
        if (mVoiceListener != null) {
            mVoiceListener.onFinishedRecord(null);
        }
    }

    private boolean startRecording() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(mAudioFile.getAbsolutePath());

            if (!mFakeToRecord) {
                recorder.prepare();
                recorder.start();
            }
            thread = new ObtainDecibelThread();
            thread.start();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void stopRecording() {
        if (thread != null) {
            thread.exit();
            thread = null;
        }
        if (recorder != null && !mFakeToRecord) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    private class ObtainDecibelThread extends Thread {

        private volatile boolean running = true;

        public void exit() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (recorder == null || !running) {
                    break;
                }
                int x = recorder.getMaxAmplitude();
                if (x != 0) {
                    int f = (int) (10 * Math.log(x) / Math.log(10));
                    if (f < 26)
                        volumeHandler.sendEmptyMessage(0);
                    else if (f < 29)
                        volumeHandler.sendEmptyMessage(1);
                    else if (f < 32)
                        volumeHandler.sendEmptyMessage(2);
                    else if (f < 35)
                        volumeHandler.sendEmptyMessage(3);
                    else if (f < 38)
                        volumeHandler.sendEmptyMessage(4);
                    else if (f < 41)
                        volumeHandler.sendEmptyMessage(5);
                    else
                        volumeHandler.sendEmptyMessage(6);
                }
            }
        }
    }

    static class ShowVolumeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what > -1 && msg.what < 7) {
                volume_anim.setImageResource(res[msg.what]);
            } else if (7 == msg.what) {
                recordIndicator.dismiss();
            }
        }
    }

    public interface VoiceRecordListener {
        public void onStartedRecord();

        public void onFinishedRecord(Uri uri);
    }
}