import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_audio_speaker_plugin/flutter_audio_speaker_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_audio_speaker_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterAudioSpeakerPlugin.platformVersion, '42');
  });
}
