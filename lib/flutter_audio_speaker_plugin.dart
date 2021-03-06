import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class FlutterAudioSpeakerPlugin {
  static const MethodChannel platform = MethodChannel('flutter_audio_speaker_plugin');

  static void setHandler(Future<dynamic> Function(MethodCall) handler) {
    return platform.setMethodCallHandler(handler);
  }

  static Future<String?> get platformVersion async {
    final String? version = await platform.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> setSpeakerOn(bool isOn) async {
    await platform.invokeMethod("setSpeakerPhoneOn", {"isOn": isOn});
  }

  static Future<void> resetSpeaker() async {
    await platform.invokeMethod("resetSpeakerPhone");
  }

  static Future<int> isHeadSetOn() async {
    return await platform.invokeMethod("isHeadSetOn");
  }

  static Future<String> rongcloudInit() async {
    if (Platform.isIOS) return Future.value('ok');
    return await platform.invokeMethod("rongcloudInit");
  }

  static Future<String> rongcloudReset() async {
    if (Platform.isIOS) return Future.value('ok');
    return await platform.invokeMethod("rongcloudReset");
  }

  static Future<String> setMode(String mode) async {
    if (Platform.isIOS) return Future.value('ok');
    return await platform.invokeMethod("setMode", {"mode": mode});
  }

  static Future<String> getMode() async {
    if (Platform.isIOS) return Future.value('ok');
    return await platform.invokeMethod("getMode");
  }
}
