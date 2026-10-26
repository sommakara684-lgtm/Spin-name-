package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundHapticManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 70)
    } catch (_: Exception) {
        null
    }

    fun playTick(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) {
            try {
                // Short crisp tick tone
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 15)
            } catch (_: Exception) {}
        }

        if (hapticsEnabled && vibrator != null && vibrator.hasVibrator()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(12, 100))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(12)
                }
            } catch (_: Exception) {}
        }
    }

    fun playSpinStart(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 80)
            } catch (_: Exception) {}
        }
        if (hapticsEnabled && vibrator != null && vibrator.hasVibrator()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(35)
                }
            } catch (_: Exception) {}
        }
    }

    fun playCelebration(scope: CoroutineScope, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) {
            scope.launch(Dispatchers.Default) {
                try {
                    // Play a 3-note celebration chime
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 120)
                    delay(140)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_B, 150)
                    delay(170)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 320)
                } catch (_: Exception) {}
            }
        }

        if (hapticsEnabled && vibrator != null && vibrator.hasVibrator()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Celebratory rhythm: bump, bump, long pulse!
                    val timings = longArrayOf(0, 80, 60, 100, 60, 250)
                    val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 80, 60, 100, 60, 250), -1)
                }
            } catch (_: Exception) {}
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
