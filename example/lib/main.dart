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
  // String _platformVersion = 'Unknown';
  bool speaker = false;

  @override
  void initState() {
    super.initState();
    FlutterAudioSpeakerPlugin.setHandler(handleMessage);
    initPlatformState();
  }

  Future<dynamic> handleMessage(MethodCall call) async {
    print("===call==" + call.toString());
  }

  void setSpeakerOn() async {
    FlutterAudioSpeakerPlugin.setSpeakerOn(!speaker);
    speaker = !speaker;
    setState(() {});
  }

  void resetSpeakerOn() async {
    await FlutterAudioSpeakerPlugin.resetSpeaker();
    setState(() {});
  }

  void isInCall() async {
    String isInCall = await FlutterAudioSpeakerPlugin.isHeadSetOn();
    print("===isHeadSetOn===" + isInCall);
  }

  void changeMode() async {
    String mode = await FlutterAudioSpeakerPlugin.getMode();
    print("===mode===" + mode);
    if (mode == "3") {
      await FlutterAudioSpeakerPlugin.setMode("normal");
    } else {
      await FlutterAudioSpeakerPlugin.setMode("communication");
    }
    String aftermode = await FlutterAudioSpeakerPlugin.getMode();
    print("===mode===" + aftermode);
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    FlutterSoundPlayer soundPlayer = FlutterSoundPlayer();
    await soundPlayer.openAudioSession(
      focus: AudioFocus.requestFocusTransient,
      category: SessionCategory.playback,
      mode: SessionMode.modeDefault,
    );

    await soundPlayer.setSubscriptionDuration(const Duration(milliseconds: 30));
    Uint8List? dataBuffer;
    ByteData data = await rootBundle.load("assets/audio/calling.mp3");
    dataBuffer = data.buffer.asUint8List();
    soundPlayer.startPlayer(
      fromDataBuffer: dataBuffer,
      codec: Codec.aacADTS,
      sampleRate: 16000,
      whenFinished: () {
        print("===finished===");
      },
    );

    // Future.delayed(const Duration(seconds: 5), () => FlutterAudioSpeakerPlugin.resetSpeaker());

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    // setState(() {
    //   _platformVersion = "1111";
    // });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              GestureDetector(
                onTap: setSpeakerOn,
                child: SizedBox(height: 120, child: Text("speak====$speaker")),
              ),
              GestureDetector(
                onTap: resetSpeakerOn,
                child: const SizedBox(height: 120, child: Text("reset")),
              ),
              GestureDetector(
                onTap: isInCall,
                child: const SizedBox(height: 120, child: Text("isINCall")),
              ),
              GestureDetector(
                onTap: changeMode,
                child: const SizedBox(height: 120, child: Text("changeMode")),
              ),
              GestureDetector(
                onTap: initPlatformState,
                child: const SizedBox(height: 120, child: Text("play again")),
              )
            ],
          ),
        ),
      ),
    );
  }
}
