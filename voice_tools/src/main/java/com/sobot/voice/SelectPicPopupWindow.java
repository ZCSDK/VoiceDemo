package com.sobot.voice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.WindowManager.LayoutParams;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

class SelectPicPopupWindow extends PopupWindow {

    private LayoutInflater inflater;
    private Context context;
    private View mView;
    private AnimationDrawable animationDrawable;/* 语音的动画 */

    private LinearLayout voice_top_image;
    private ImageView mic_image;//麦克风图片
    private ImageView recording_timeshort;//提示录音太短的图片
    private ImageView mic_image_animate;//动画的图片
    private ImageView image_endVoice;//取消发送的图片
    public TextView voice_time_long;//录音多久的textview
    public TextView voice_time_long1;//倒计时的textview
    private TextView recording_hint;

    private int minRecordTime = 60;// 允许录音时间
    private int recordDownTime = minRecordTime - 10;// 允许录音时间 倒计时
    private int currentVoiceLong = 0;
    public int voiceTimerLong = 0;
    protected String voiceTimeLongStr = "00";// 时间的定时的任务

    /*录音的定时 */
    protected Timer voiceTimer;
    protected TimerTask voiceTimerTask;

    private MySendVoice mySendVoice;

    public SelectPicPopupWindow(final Activity context, MySendVoice sendVoice) {
        this.context = context;
        initView();
        this.mySendVoice = sendVoice;
    }

    private void initView() {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(ResourceUtils.getIdByName(context, "layout", "sobot_voice_layout"), null);
        voice_top_image = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_voice_top_image"));
        mic_image = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_mic_image"));
        recording_timeshort = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_recording_timeshort"));
        recording_timeshort.setVisibility(View.GONE);
        mic_image_animate = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_mic_image_animate"));
        image_endVoice = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_image_endVoice"));
        image_endVoice.setVisibility(View.GONE);
        voice_time_long = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_voiceTimeLong"));
        voice_time_long1 = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_voiceTimeLong1"));
        voice_time_long1.setVisibility(View.GONE);
        recording_hint = mView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_recording_hint"));

        // 设置SelectPicPopupWindow的View
        this.setContentView(mView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体是否可获取焦点，如果PopupWindow中没有Edittext就无所谓了。
        this.setFocusable(false);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(ResourceUtils.getIdByName(context, "style", "AnimBottom"));
        // 实例化一个ColorDrawable颜色为半透明
        Drawable drawable = context.getResources().getDrawable(R.drawable.sobot_recording_hint_bg);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(drawable);
    }

    void setMoveVoiceHint(boolean isShow) {
        if (isShow) {
            voice_top_image.setVisibility(View.GONE);
            image_endVoice.setVisibility(View.VISIBLE);
            recording_hint.setText("松开手指，取消发送");
            recording_hint.setBackgroundResource(getResDrawableId("sobot_recording_text_hint_bg"));
        } else {
            image_endVoice.setVisibility(View.GONE);
            voice_top_image.setVisibility(View.VISIBLE);
            recording_hint.setText("手指上滑，取消发送");
            recording_hint.setBackgroundResource(getResDrawableId("sobot_recording_text_hint_bg1"));
        }
    }

    void setUpVoiceHint() {
        if (animationDrawable != null) {
            animationDrawable.stop();
        }

        stopVoiceTimeTask();

        if (currentVoiceLong <= 1 * 1000) {
            mic_image.setVisibility(View.GONE);
            mic_image_animate.setVisibility(View.GONE);
            recording_timeshort.setVisibility(View.VISIBLE);
            recording_hint.setText("说话时间太短");
            recording_hint.setBackgroundResource(getResDrawableId("sobot_recording_text_hint_bg"));
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, 300);
    }

    void setDownVoiceHint() {
        voice_time_long.setText("00''");
        voice_time_long.setVisibility(View.VISIBLE);
        startVoiceTimeTask();
        startMicAnimate();
    }

    private void startMicAnimate() {
        mic_image_animate.setBackgroundResource(getResDrawableId("sobot_voice_animation"));
        animationDrawable = (AnimationDrawable) mic_image_animate.getBackground();
        mic_image_animate.post(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        });
        recording_hint.setText("手指上滑，取消发送");
        recording_hint.setBackgroundResource(getResDrawableId("sobot_recording_text_hint_bg1"));
    }

    private int getResDrawableId(String name) {
        return ResourceUtils.getIdByName(context, "drawable", name);
    }

    /* 录音的时间控制 */
    private void startVoiceTimeTask() {
        voiceTimerLong = 0;
        stopVoiceTimeTask();
        voiceTimer = new Timer();
        voiceTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 需要做的事:发送消息
                sendVoiceTimeTask(handler);
            }
        };

        // 500ms进行定时任务
        voiceTimer.schedule(voiceTimerTask, 0, 500);
    }

    void stopVoiceTimeTask() {
        if (voiceTimer != null) {
            voiceTimer.cancel();
            voiceTimer = null;
        }
        if (voiceTimerTask != null) {
            voiceTimerTask.cancel();
            voiceTimerTask = null;
        }
        voiceTimerLong = 0;
    }

    private Handler handler = new Handler() {
        public void handleMessage(final android.os.Message msg) {
            final int time = Integer.parseInt(msg.obj.toString());
            switch (msg.what) {
                case 1000:
                    if (voiceTimerLong >= minRecordTime * 1000) {
                        Intent intent = new Intent();
                        intent.setAction("com.sobot.quxiao");
                        context.sendBroadcast(intent);
                        voice_time_long1.setVisibility(View.VISIBLE);
                        voice_time_long.setVisibility(View.GONE);
                        voice_time_long1.setText("录音时间过长");
                    } else {
//					LogUtils.i("录音定时任务的时长：" + time);
                        currentVoiceLong = time;
                        if (time < recordDownTime * 1000) {
                            if (time % 1000 == 0) {
                                voice_time_long.setTag("1");
                                voice_time_long1.setVisibility(View.GONE);
                                voiceTimeLongStr = SobotTimeTools.instance.calculatTime(time);
                                voice_time_long.setText(voiceTimeLongStr.substring(3) + "''");
                            }
                        } else if (time < minRecordTime * 1000) {
                            if (time % 1000 == 0) {
                                voiceTimeLongStr = SobotTimeTools.instance.calculatTime(time);
                                voice_time_long1.setVisibility(View.VISIBLE);
                                voice_time_long.setText((minRecordTime * 1000 - time) / 1000 + "");
                                voice_time_long.setTag("2");
                            }
                        } else {

                        }
                    }
                    break;
            }
        }
    };

    /**
     * 发送声音的定时的任务
     */
    private void sendVoiceTimeTask(Handler handler) {
        Message message = handler.obtainMessage();
        message.what = 1000;
        voiceTimerLong = voiceTimerLong + 500;
        message.obj = voiceTimerLong;
        handler.sendMessage(message);
    }
}