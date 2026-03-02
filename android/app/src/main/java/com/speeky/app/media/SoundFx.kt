package com.speeky.app.media

import android.media.AudioManager
import android.media.ToneGenerator

object SoundFx {
    fun tap() {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60).apply {
            startTone(ToneGenerator.TONE_PROP_BEEP, 80)
            release()
        }
    }

    fun incoming() {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90).apply {
            startTone(ToneGenerator.TONE_PROP_ACK, 140)
            release()
        }
    }

    fun ringBurst() {
        ToneGenerator(AudioManager.STREAM_RING, 80).apply {
            startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
            release()
        }
    }
}
