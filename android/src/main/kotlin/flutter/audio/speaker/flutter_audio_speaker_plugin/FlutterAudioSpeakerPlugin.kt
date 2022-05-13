package flutter.audio.speaker.flutter_audio_speaker_plugin

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import cn.rongcloud.rtc.api.*

/** FlutterAudioSpeakerPlugin */
class FlutterAudioSpeakerPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var audioManager: AudioManager
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
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                    val state = intent.getIntExtra("state", 0)
                    channel.invokeMethod("headSetStatus", state)
                    if (state == 1) {
                        changeMode(PlayMode.Headset)
                    } else if (state == 0) {
                        changeMode(latestPlayMode)
                    }
                } else if (intent?.action == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0)
                    if (state != 0) {
                        changeMode(PlayMode.Headset)
                    } else {
                        changeMode(latestPlayMode)
                    }
                    channel.invokeMethod("bluetoothStatus", state)
                } else if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.ACTION_STATE_CHANGED, 0)
                    channel.invokeMethod("bluetoothStatus", state)
                    if (state != 0) {
                        changeMode(PlayMode.Headset)
                    } else {
                        changeMode(latestPlayMode)
                    }
                }
            }
        }, intentFilter)
    }

    private fun changeToHeadset() {
        audioManager.isSpeakerphoneOn = false
    }

    private fun changeToSpeaker() {
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = true
    }

    private fun changeToReceiver() {
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    private fun changeMode(pm: PlayMode) {
        latestPlayMode = playMode
        playMode = pm
        when (playMode) {
            PlayMode.Receiver -> changeToReceiver()
            PlayMode.Speaker -> changeToSpeaker()
            PlayMode.Headset -> changeToHeadset()
        }

        resetRongcloud()
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

        if (call.method == "setSpeakerPhoneOn") {
            if (call.hasArgument("isOn") && playMode != PlayMode.Headset) {
                var isOn: Boolean? = call.argument("isOn")
                if (isOn != null && isOn) changeMode(PlayMode.Speaker) else changeMode(PlayMode.Receiver)
            }
            result.success("ok")
        } else if (call.method == "resetSpeakerPhone") {
            if (playMode != PlayMode.Headset) {
                changeMode(latestPlayMode)
            }
        } else if (call.method == "isHeadSetOn") {
            if (isHeadSetOn()) {
                result.success("1");
            } else {
                result.success("0");
            }
        } else if (call.method == "rongcloudInit") {
            if (!RCRTCAudioRouteManager.getInstance().hasInit()) {
                RCRTCAudioRouteManager.getInstance().init(context)
                rongcloudAudioManager = RCRTCAudioRouteManager.getInstance()
            }

            result.success("ok")
        } else if (call.method == "rongcloudReset") {
            rongcloudAudioManager?.resetAudioRouteState()
        } else {
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
            if (audioManager == null) return false

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return audioManager.isWiredHeadsetOn || audioManager.isBluetoothScoOn || audioManager.isBluetoothA2dpOn
            } else {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

                for (device in devices) {
                    if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun resetRongcloud() {
        if (rongcloudAudioManager != null) {
            rongcloudAudioManager!!.resetAudioRouteState()
        } else {
            RCRTCAudioRouteManager.getInstance().resetAudioRouteState()
        }
    }
}