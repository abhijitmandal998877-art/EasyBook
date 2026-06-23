package com.example.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object EffectsManager {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)
        } catch (e: Exception) {
            Log.e("EffectsManager", "Failed to initialize ToneGenerator", e)
        }
    }

    /**
     * Play a small bip/touch sound effect if enabled.
     */
    fun playSound(enabled: Boolean, type: Int = ToneGenerator.TONE_PROP_BEEP) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(type, 80)
        } catch (e: Exception) {
            Log.e("EffectsManager", "Failed to play sound: ${e.message}")
        }
    }

    /**
     * Vibrate the device for a given duration if enabled.
     */
    fun vibrate(context: Context, enabled: Boolean, durationMs: Long = 50) {
        if (!enabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            Log.e("EffectsManager", "Failed to trigger vibration: ${e.message}")
        }
    }
}
