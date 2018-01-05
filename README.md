# VoiceDemo 语音

VoiceActivity中是动态获取权限兼容6.0及以上安卓版本

使用方法：

在build.gradle文件中添加远程依赖：

compile 'com.sobot.voice:voice_tools:1.1'

在VoiceActivity的布局中添加自定义语音布局

    <com.sobot.voice.SobotVoiceView
        android:id="@+id/sobot_voice_view"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_alignParentBottom="true"/>

在VoiceActivity类中添加以下五个属性

    SobotVoiceView sobotVoiceView;//语音控件

    protected boolean has_write_external_storage_permission = false;//是否获取到存储权限
    protected boolean has_record_audio_permission = false;//是否获取到录音权限

    int SOBOT_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 192;//请求码
    int SOBOT_RECORD_AUDIO_REQUEST_CODE = 193;//请求码

在VoiceActivity的onCreate方法中添加以下代码

	//获取权限
	has_record_audio_permission= checkPermission(this,
                Manifest.permission.RECORD_AUDIO, SOBOT_RECORD_AUDIO_REQUEST_CODE);
    has_write_external_storage_permission= checkPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, SOBOT_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    if(!has_record_audio_permission || !has_write_external_storage_permission){
        Log.i("tag", "没有权限.............");
        return;
    }
	
	//注册控件
	sobotVoiceView = (SobotVoiceView) findViewById(R.id.sobot_voice_view);
	//调用回调
    sobotVoiceView.setMySendVoice(new MySendVoice() {
        @Override
        public void sendVoice(int sendType, String voiceMsgId, String mFileName, int voiceLongTime) {
            Log.i("tag-----", "发送类型是------" + sendType + "---语音消息id------"
            + voiceMsgId + "---文件路径是------" + mFileName + "---录音长度是------" + voiceLongTime);
			//TODO
			//使用者回调中完成
        }
    });

	调用sobotVoiceView.setMySendVoice回调以后，语音控件已经把语音发送的类型，语音消息的id
语音文件的路径和语音长度都返回。使用者可以通过判断发送类型来做逻辑开发。

	sendType == 0 正在录音

	sendType == 1 发送录音

	sendType == 2 取消录音
