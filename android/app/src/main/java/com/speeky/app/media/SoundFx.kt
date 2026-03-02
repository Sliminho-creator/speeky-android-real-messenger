package com.speeky.app.media

import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator

class SoundFx(private val context: Context) {
    private val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
    private var ringtone: Ringtone? = null
    fun playSent() { tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80) }
    fun playIncomingMessage() { tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 110) }
    fun startIncomingCall() { ringtone = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)); ringtone?.play() }
    fun stopIncomingCall() { ringtone?.stop(); ringtone = null }
}
