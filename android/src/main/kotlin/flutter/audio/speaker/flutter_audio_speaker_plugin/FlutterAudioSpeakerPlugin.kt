package flutter.audio.speaker.flutter_audio_speaker_plugin

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import cn.rongcloud.rtc.api.*
import cn.rongcloud.rtc.api.callback.IRCRTCAudioRouteListener
import cn.rongcloud.rtc.audioroute.RCAudioRouteType

/** FlutterAudioSpeakerPlugin */
class FlutterAudioSpeakerPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var audioManager: AudioManager
//    private lateinit var usbManager: UsbManager
    private var rongcloudAudioManager : RCRTCAudioRouteManager? = null

    enum class PlayMode {
        Speaker, // 外放
        Headset, // 耳机
        Receiver // 听筒
    }

    private var playMode = PlayMode.Speaker
    private var latestPlayMode = PlayMode.Speaker

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_audio_speaker_plugin")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.getApplicationContext()

        RCRTCAudioRouteManager.getInstance().init(context)
        RCRTCAudioRouteManager.getInstance().setOnAudioRouteChangedListener(object :
            IRCRTCAudioRouteListener {
            override fun onRouteChanged(type : RCAudioRouteType) {
                Log.e("====type", type.toString())
                channel.invokeMethod("onRouteChanged", type.toString())
            }

            override fun onRouteSwitchFailed(fromType: RCAudioRouteType, toType: RCAudioRouteType) {
                Log.e("=====typeFailed", "$fromType-$toType")
                channel.invokeMethod("onRouteSwitchFailed", toType.toString())
            }
        })

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Log.e("usbDevice", "typecStatus:14")
                // Log.e("usbDevice", intent?.action.toString())
                if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                    val state = intent.getIntExtra("state", 0)
                    // Log.e("usbDevice", state.toString())
                    channel.invokeMethod("headSetStatus", state)
                } else if (intent?.action == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0)
                    channel.invokeMethod("bluetoothStatus", state)
                } else if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.ACTION_STATE_CHANGED, 0)
                    channel.invokeMethod("bluetoothStatus", state)
//                } else if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
//                    Log.e("usbDevice", "typecStatus:13")
//                    usbManager.deviceList.values.indexOfFirst {
//                        it.deviceClass == UsbConstants.USB_CLASS_AUDIO
//                    }.takeIf { it > -1 }?.run {
//                        Log.e("usbDevice", "typecStatus:1")
//                        channel.invokeMethod("typecStatus", 1)
//                    }
//                } else if (intent?.action == UsbManager.ACTION_USB_ACCESSORY_DETACHED) {
//                    Log.e("usbDevice", "typecStatus:03")
//                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
//                    device?.apply {
//                        if (deviceClass == UsbConstants.USB_CLASS_AUDIO) {
//                            Log.e("usbDevice", "typecStatus:0")
//                            channel.invokeMethod("typecStatus", 0)
//                        }
//                    }
//                } else if (intent?.action == BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED) {
//                    val adapter = BluetoothAdapter.getDefaultAdapter()
//                    val state = adapter.getProfileConnectionState(BluetoothProfile.HEADSET)
//                    Log.e("bluetoothStatus", state.toString())
//                    if (state == BluetoothProfile.STATE_CONNECTED) {
//                        channel.invokeMethod("bluetoothStatus", 1)
//                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
//                        channel.invokeMethod("bluetoothStatus", 0)
//                    }
                }
            }
        }, intentFilter)


    }

    private fun changeToHeadset() {
        audioManager.isSpeakerphoneOn = false
    }

    private fun changeToSpeaker() {
        audioManager.isSpeakerphoneOn = true
    }

    private fun changeToReceiver() {
        audioManager.isSpeakerphoneOn = false
    }

    private fun changeMode(pm: PlayMode) {
        latestPlayMode = playMode
        playMode = pm
        Log.e("===mode==", pm.toString())
        when (playMode) {
            PlayMode.Receiver -> changeToReceiver()
            PlayMode.Speaker -> changeToSpeaker()
            PlayMode.Headset -> changeToHeadset()
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "setSpeakerPhoneOn") {
            if (call.hasArgument("isOn")) {
                var isOn: Boolean? = call.argument("isOn")
                if (isOn != null && isOn) changeMode(PlayMode.Speaker) else changeMode(PlayMode.Headset)
            }
            result.success("ok")
        } else if (call.method == "resetSpeakerPhone") {
            if (playMode != PlayMode.Headset) {
                changeMode(latestPlayMode)
            }
        } else if (call.method == "isHeadSetOn") {
            if (isHeadSetOn()) {
                result.success(1);
            } else {
                result.success(0);
            }
        } else if (call.method == "rongcloudInit") {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            if (!RCRTCAudioRouteManager.getInstance().hasInit()) {
                RCRTCAudioRouteManager.getInstance().init(context)
                rongcloudAudioManager = RCRTCAudioRouteManager.getInstance()
            }

            result.success("ok")
        } else if (call.method == "rongcloudReset") {
            resetRongcloud()
            result.success("ok")
        } else if (call.method == "setMode") {
            if (call.hasArgument("mode")) {
               if (call.argument<String>("mode") == "normal") {
                   audioManager.mode = AudioManager.MODE_NORMAL
               } else if (call.argument<String>("mode") == "communication") {
                   audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
               }
                Log.e("====mode==", audioManager.mode.toString())
            }

            rongcloudAudioManager?.resetAudioRouteState()
            result.success("ok")
        } else if (call.method == "getMode") {
            result.success(audioManager.mode.toString())
        }
        else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun isHeadSetOn() : Boolean{
        if (rongcloudAudioManager != null) {
            return rongcloudAudioManager!!.hasHeadSet() || rongcloudAudioManager!!.hasBluetoothA2dpConnected()
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return audioManager.isWiredHeadsetOn || audioManager.isBluetoothScoOn || audioManager.isBluetoothA2dpOn
            } else {
                // 判断有限连接
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

                for (device in devices) {
                    if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                        return true
                    }
                }

                // 判断type-c连接
//                usbManager.deviceList.values.indexOfFirst {
//                    it.deviceClass == UsbConstants.USB_CLASS_AUDIO
//                }.takeIf { it > -1 }?.run {
//                    Log.e("usbDevice", "ok")
//                    return true
//                }

                // 判断蓝牙连接
//                if (isBluetoothHeadsetOn() == 1) return true
            }
        }

        return false
    }

//    private fun isBluetoothHeadsetOn() : Int{
//        val adapter = BluetoothAdapter.getDefaultAdapter()
//        val state = adapter.getProfileConnectionState(BluetoothProfile.HEADSET)
//        if (state == BluetoothProfile.STATE_CONNECTED) {
//            return 1
//        } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
//            return 0
//        }
//
//        return -1
//    }

    private fun resetRongcloud() {
        if (rongcloudAudioManager != null) {
            rongcloudAudioManager!!.resetAudioRouteState()
            rongcloudAudioManager!!.unInit()
        } else {
            RCRTCAudioRouteManager.getInstance().init(context)
            if (RCRTCAudioRouteManager.getInstance().hasInit()) {
                RCRTCAudioRouteManager.getInstance().resetAudioRouteState()
                RCRTCAudioRouteManager.getInstance().unInit()
            }
        }
    }
}