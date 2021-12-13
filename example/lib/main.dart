import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_audio_speaker_plugin/flutter_audio_speaker_plugin.dart';
import 'package:flutter_sound/flutter_sound.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool speaker = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  void setSpeakerOn() async {
    await FlutterAudioSpeakerPlugin.setSpeakerOn(!speaker);
    speaker = !speaker;
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    FlutterSoundPlayer soundPlayer = FlutterSoundPlayer();
    await soundPlayer.openAudioSession(
      focus: AudioFocus.requestFocusTransient,
      category: SessionCategory.playAndRecord,
      mode: SessionMode.modeDefault,
      device: AudioDevice.speaker,
    );

    await FlutterAudioSpeakerPlugin.setSpeakerOn(speaker);
    await soundPlayer.setSubscriptionDuration(const Duration(milliseconds: 30));
    Uint8List? dataBuffer;
    ByteData data = await rootBundle.load("assets/audio/calling.mp3");
    dataBuffer = data.buffer.asUint8List();
    soundPlayer.startPlayer(
      fromDataBuffer: dataBuffer,
      codec: Codec.aacADTS,
      sampleRate: 16000,
    );

    // Future.delayed(const Duration(seconds: 5), () => FlutterAudioSpeakerPlugin.resetSpeaker());

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = "1111";
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: GestureDetector(
          onTap: setSpeakerOn,
          child: Center(
            child: Text('Running on: $_platformVersion\n'),
          ),
        ),
      ),
    );
  }
}
