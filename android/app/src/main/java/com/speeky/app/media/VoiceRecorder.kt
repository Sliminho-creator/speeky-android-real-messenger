package com.speeky.app.media

import android.content.Context
import android.media.MediaRecorder
import java.io.File

class VoiceRecorder {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun start(context: Context): String {
        val output = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        currentFile = output

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(output.absolutePath)
            prepare()
            start()
        }

        return output.absolutePath
    }

    fun stop(): String? {
        return runCatching {
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            val result = currentFile?.absolutePath
            recorder = null
            currentFile = null
            result
        }.getOrNull()
    }

    fun cancel() {
        val path = currentFile?.absolutePath
        runCatching {
            recorder?.stop()
        }
        recorder?.reset()
        recorder?.release()
        recorder = null
        currentFile = null
        if (path != null) {
            runCatching { File(path).delete() }
        }
    }
}
