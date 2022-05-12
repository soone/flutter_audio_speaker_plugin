import 'dart:async';

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

  static Future<String> isHeadSetOn() async {
    return await platform.invokeMethod("isHeadSetOn");
  }

  static Future<String> rongclousInit() async {
    return await platform.invokeMethod("rongcloudInit");
  }
}
