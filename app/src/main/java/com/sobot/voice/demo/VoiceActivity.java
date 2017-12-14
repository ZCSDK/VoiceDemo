package com.sobot.voice.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sobot.voice.MySendVoice;
import com.sobot.voice.SobotVoiceView;

/**
 *
 * Created by Administrator on 2017/11/29.
 */

public class VoiceActivity extends AppCompatActivity{

    SobotVoiceView sobotVoiceView;

    protected boolean has_write_external_storage_permission = false;
    protected boolean has_record_audio_permission = false;

    int SOBOT_RECORD_AUDIO_REQUEST_CODE = 193;
    int SOBOT_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 192;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_demo_layout);

        has_record_audio_permission= checkPermission(this,
                Manifest.permission.RECORD_AUDIO, SOBOT_RECORD_AUDIO_REQUEST_CODE);
        has_write_external_storage_permission= checkPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, SOBOT_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        if(!has_record_audio_permission || !has_write_external_storage_permission){
            Log.i("tag", "没有权限.............");
            return;
        }

        Log.i("tag", "有权限ppppppppppppppppppp");

        sobotVoiceView = (SobotVoiceView) findViewById(R.id.sobot_voice_view);
        sobotVoiceView.setMySendVoice(new MySendVoice() {
            @Override
            public void sendVoice(int sendType, String voiceMsgId, String mFileName, int voiceLongTime) {
                Log.i("tag-----", "发送类型是------" + sendType + "---语音消息id------"
                        + voiceMsgId + "---文件路径是------" + mFileName + "---录音长度是------" + voiceLongTime);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sobotVoiceView.broadcastReceiver);
    }

    /**
     * 检查6.0 动态权限
     * @param act activity
     * @param type 权限类型
     * @param code 返回码
     * @return 是否有权限
     */
    public static boolean checkPermission(Activity act, String type, int code){
        boolean isPermission=false;
        if(getTargetSdkVersion(act.getApplicationContext()) >= 23){
            if (ContextCompat.checkSelfPermission(act, type)
                    != PackageManager.PERMISSION_GRANTED) {
                isPermission=false;
                //申请权限
                ActivityCompat.requestPermissions(act, new String[]{type}, code);
            } else {
                isPermission = true;
            }
        } else {
            isPermission = true;
        }
        return isPermission;
    }

    public static int getTargetSdkVersion(Context context){
        int targetSdkVersion = 0;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return targetSdkVersion;
    }
}