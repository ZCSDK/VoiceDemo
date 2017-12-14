package com.sobot.voice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

public class SobotVoiceView extends LinearLayout {

    public void setMySendVoice(MySendVoice mySendVoice) {
        this.mySendVoice = mySendVoice;
    }

    private MySendVoice mySendVoice;

    private LayoutInflater inflater;
    private Context context;
    private View mView;

    private LinearLayout btn_press_to_speak; // 说话view ;
    private TextView txt_speak_content; // 发送语音的文字
    private SelectPicPopupWindow menuWindow;

    private ExtAudioRecorder extAudioRecorder;
    private MediaRecorder mRecorder = null;
    private String voiceMsgId = "";//  语音消息的Id
    private String mFileName = null;//录音文件
    public static final String mVoicePath = "/sdcard/Record/";

    public SobotVoiceView(Context context) {
        super(context);
        this.context = context;
        initSobotVoiceView();
        regBroadcastReceiver();
    }

    public SobotVoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initSobotVoiceView();
        regBroadcastReceiver();
    }

    public SobotVoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initSobotVoiceView();
        regBroadcastReceiver();
    }

    private void initSobotVoiceView() {

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(ResourceUtils.getIdByName(context, "layout", "sobot_voice_layout1"), null);
        btn_press_to_speak = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_btn_press_to_speak"));
        txt_speak_content = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_txt_speak_content"));
        txt_speak_content.setText("按住    说话");
        btn_press_to_speak.setOnTouchListener(new PressToSpeakListen());
        this.addView(mView, p);
    }

    private class PressToSpeakListen implements View.OnTouchListener {
        @SuppressLint({"ClickableViewAccessibility", "Wakelock"})
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    voiceMsgId = System.currentTimeMillis() + "";
                    view.setPressed(true);
                    if (mySendVoice != null) {
                        menuWindow = new SelectPicPopupWindow((Activity) context, mySendVoice);
                        menuWindow.getBackground().setAlpha(120);
                        menuWindow.showAtLocation(txt_speak_content, Gravity.CENTER, 0, 0); // 设置layout在PopupWindow中显示的位置
                        menuWindow.setOutsideTouchable(false);//点击PopupWindow以外的区域，不消失
                        txt_speak_content.setText("松开    发送");
                        menuWindow.setDownVoiceHint();
                        startVoice();
                    }
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    return true;
                case MotionEvent.ACTION_POINTER_UP:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (motionEvent.getY() < 10) {
                        txt_speak_content.setText("松开手指，取消发送");
                        menuWindow.setMoveVoiceHint(true);
                    } else {
                        if (menuWindow.voiceTimerLong != 0) {
                            Log.i("tag", "voiceTimerLong--------" + menuWindow.voice_time_long.getText());
                            txt_speak_content.setText("松开    发送");
                            menuWindow.setMoveVoiceHint(false);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    view.setPressed(false);
                    txt_speak_content.setText("按住    说话");
                    menuWindow.setUpVoiceHint();
                    if (motionEvent.getY() < 0) {
                        Log.i("tag", "bbbbbbbbbbbbbbbb");
                        if (mySendVoice != null) {
                            mySendVoice.sendVoice(2, voiceMsgId, mFileName, getVoiceTime());
                        }
                    } else {
                        Log.i("tag", "aaaaaaaaaaaaaa");
                        if (mySendVoice != null) {
                            int num = getVoiceTime();
                            if (num > 1) {
                                Log.i("tag", "cccccccccccccc");
                                mySendVoice.sendVoice(1, voiceMsgId, mFileName, num);
                            } else {
                                Log.i("tag", "ddddddddddddddd");
                                mySendVoice.sendVoice(2, voiceMsgId, mFileName, num);
                            }
                        }
                    }
                    stopVoice();
                    return true;
            }
            return true;
        }
    }

    private void regBroadcastReceiver() {
        Log.i("tag", "注册广播");
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.sobot.quxiao");
        context.registerReceiver(broadcastReceiver, filter);
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.sobot.quxiao".equals(intent.getAction())) {
                btn_press_to_speak.setPressed(false);
                txt_speak_content.setText("按住    说话");
                if (menuWindow != null) {
                    menuWindow.stopVoiceTimeTask();
                    menuWindow.dismiss();
                }
            }
        }
    };

    private void startVoice() {
        try {
            stopVoice();
            mFileName = mVoicePath + UUID.randomUUID().toString() + ".wav";
            String state = android.os.Environment.getExternalStorageState();
            if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
                Log.i("tag", "sd卡被卸载了");
            }
            File directory = new File(mFileName).getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
                Log.i("tag", "文件夹创建失败");
            }
            extAudioRecorder = ExtAudioRecorder.getInstanse(false);
            extAudioRecorder.setOutputFile(mFileName);
            extAudioRecorder.prepare();
            extAudioRecorder.start(new ExtAudioRecorder.AudioRecorderListener() {
                @Override
                public void onHasPermission() {
                    if (mySendVoice != null) {
                        mySendVoice.sendVoice(0, voiceMsgId, mFileName, getVoiceTime());
                    }
                }

                @Override
                public void onNoPermission() {
                    Toast.makeText(context, "没有麦克风权限", Toast.LENGTH_LONG);
                }
            });
        } catch (Exception e) {
            Log.i("tag", "prepare() failed");
        }
    }

    /* 停止录音 */
    private void stopVoice() {
        /* 布局的变化 */
        try {
            if (extAudioRecorder != null) {
                extAudioRecorder.stop();
                extAudioRecorder.release();
            }
        } catch (Exception e) {
            mRecorder = null;
        }
    }

    private int getVoiceTime() {
        if (menuWindow != null && menuWindow.voice_time_long != null) {
            int voiceTime;
            if (menuWindow.voice_time_long.getText().toString().length() == 4) {
                voiceTime = Integer.parseInt(menuWindow.voice_time_long.getText().toString().substring(0, 2));
            } else {
                voiceTime = Integer.parseInt(menuWindow.voice_time_long.getText().toString().substring(0, 1));
            }

            if (voiceTime <= 10 && voiceTime >= 0 && "2".equals(menuWindow.voice_time_long.getTag())) {
                if (voiceTime == 10) {
                    return 50;
                } else if (voiceTime == 9) {
                    return 51;
                } else if (voiceTime == 8) {
                    return 52;
                } else if (voiceTime == 7) {
                    return 53;
                } else if (voiceTime == 6) {
                    return 54;
                } else if (voiceTime == 5) {
                    return 55;
                } else if (voiceTime == 4) {
                    return 56;
                } else if (voiceTime == 3) {
                    return 57;
                } else if (voiceTime == 2) {
                    return 58;
                } else if (voiceTime == 1) {
                    return 59;
                }
            } else if (voiceTime < 50) {
                return voiceTime;
            }
        }
        return 0;
    }
}