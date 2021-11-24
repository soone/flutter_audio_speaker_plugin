import Flutter
import UIKit

public class SwiftFlutterAudioSpeakerPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_audio_speaker_plugin", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterAudioSpeakerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
