import Flutter
import UIKit
import AVFoundation

public class SwiftFlutterAudioSpeakerPlugin: NSObject, FlutterPlugin {
    var category: AVAudioSession.Category?
    var theChan: FlutterMethodChannel?
    override init() {
        super.init()
        category = AVAudioSession.sharedInstance().category
    }
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_audio_speaker_plugin", binaryMessenger: registrar.messenger())
      let instance = SwiftFlutterAudioSpeakerPlugin()
      instance.theChan = channel
    registrar.addMethodCallDelegate(instance, channel: channel)
      NotificationCenter.default.addObserver(self,
                                             selector: #selector(audioRouteChangeListenerCallback),
                                             name: AVAudioSession.routeChangeNotification,
                                             object: AVAudioSession.sharedInstance)
  }
    
    @objc func audioRouteChangeListenerCallback(notification: NSNotification) {
        guard let userInfo = notification.userInfo,
              let reasonVal = userInfo[AVAudioSessionRouteChangeReasonKey] as? UInt,
              let reason = AVAudioSession.RouteChangeReason(rawValue: reasonVal) else {
                  return
              }
        switch reason {
        case .newDeviceAvailable:
            theChan?.invokeMethod("headSetStatus", arguments: 1)
        case .oldDeviceUnavailable:
            theChan?.invokeMethod("headSetStatus", arguments: 0)
        default: ()
        }
    }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      switch call.method {
      case "setSpeakerPhoneOn":
          print(call.arguments!)
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
      case "isHeadSetOn":
        let route: AVAudioSessionRouteDescription = AVAudioSession.sharedInstance().currentRoute
        for desc in route.outputs {
            if desc.portType == AVAudioSession.Port.headphones || desc.portType == AVAudioSession.Port.bluetoothA2DP || desc.portType == AVAudioSession.Port.usbAudio {
                result(1)
                return
            }
        }
                 
        result(0)
    case "setMode":
        result("ok")
      case "getMode":
          result("ok")
      default:
          result(FlutterMethodNotImplemented)
      }
    
  }
}
