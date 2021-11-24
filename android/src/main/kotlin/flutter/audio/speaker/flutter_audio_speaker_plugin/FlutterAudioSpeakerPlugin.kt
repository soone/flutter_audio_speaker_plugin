package flutter.audio.speaker.flutter_audio_speaker_plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterAudioSpeakerPlugin */
class FlutterAudioSpeakerPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var audioManager: AudioManager

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
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                    val state = intent.getIntExtra("state", 0)
                    if (state == 1) {
                        changeMode(PlayMode.Headset)
                    } else if (state == 0) {
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
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

        if (call.method == "setSpeakerPhoneOn") {
            if (call.hasArgument("isOn") && playMode != PlayMode.Headset) {
                var isOn: Boolean? = call.argument("isOn")
                if (isOn != null && isOn) changeMode(PlayMode.Speaker) else changeMode(PlayMode.Receiver)
            }
            result.success("ok")
        } else if(call.method == "resetSpeakerPhone") {
            if (playMode != PlayMode.Headset) {
                changeMode(latestPlayMode)
            }
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}