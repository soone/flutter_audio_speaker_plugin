import Flutter
import UIKit
import AVFoundation

public class SwiftFlutterAudioSpeakerPlugin: NSObject, FlutterPlugin {
    var category: AVAudioSession.Category?
    override init() {
        super.init()
        category = AVAudioSession.sharedInstance().category
    }
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_audio_speaker_plugin", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterAudioSpeakerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      switch call.method {
      case "setSpeakerPhoneOn":
          let args = call.arguments as? Dictionary<String, AnyObject>
          if args?["isOn"] != nil {
              let isOn = args?["isOn"] as! Bool
              
              do {
                  category = AVAudioSession.sharedInstance().category
                  
                  if isOn {
                      try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
//                      try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSession.PortOverride.speaker)
                  } else {
                      try AVAudioSession.sharedInstance().setCategory(.playAndRecord, mode: .default)
//                      try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSession.PortOverride.none)
                  }
                  
                  try AVAudioSession.sharedInstance().setActive(true)
              } catch {
                  NSLog("error====%@", error.localizedDescription)
              }
          }
          result("ok")
      case "resetSpeakerPhone":
          if category != nil {
            try? AVAudioSession.sharedInstance().setCategory(category!, mode: .default)
            try? AVAudioSession.sharedInstance().setActive(true)
          }
          
          result("resetSpeakerPhone")
      default:
          result(FlutterMethodNotImplemented)
      }
    
  }
}
