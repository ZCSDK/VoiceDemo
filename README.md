# VoiceDemo 语音
VoiceActivity中是动态获取权限兼容6.0及以上安卓版本

使用方法：
在build.gradle文件中添加远程依赖：
compile 'com.sobot.voice:voice_tools:1.1'

在VoiceActivity的布局中添加语音布局

sendType == 0 正在录音

sendType == 1 发送录音

sendType == 2 取消录音
