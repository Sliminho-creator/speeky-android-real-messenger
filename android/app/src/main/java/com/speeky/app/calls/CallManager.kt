package com.speeky.app.calls

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CallManager {
    private val _state = MutableStateFlow("idle")
    val state: StateFlow<String> = _state
    fun startOutgoingAudio() { _state.value = "calling-audio" }
    fun startOutgoingVideo() { _state.value = "calling-video" }
    fun acceptIncoming() { _state.value = "in-call" }
    fun end() { _state.value = "idle" }
}
