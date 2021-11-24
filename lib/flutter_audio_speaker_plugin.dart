import 'dart:async';

import 'package:flutter/services.dart';

class FlutterAudioSpeakerPlugin {
  static const MethodChannel _channel = MethodChannel('flutter_audio_speaker_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<void> setSpeakerOn(bool isOn) async {
    await _channel.invokeMethod("setSpeakerPhoneOn", {"isOn": isOn});
  }
}
