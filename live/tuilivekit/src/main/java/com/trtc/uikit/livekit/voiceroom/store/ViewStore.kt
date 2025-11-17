package com.trtc.uikit.livekit.voiceroom.store

import kotlinx.coroutines.flow.MutableStateFlow

data class ViewState(
    val isApplyingToTakeSeat: MutableStateFlow<Boolean>
)

class ViewStore {
    private val _isApplyingToTakeSeat = MutableStateFlow<Boolean>(false)
    val viewState = ViewState(_isApplyingToTakeSeat)

    fun updateTakeSeatState(isApplying: Boolean) {
        _isApplyingToTakeSeat.value = isApplying
    }

    fun destroy() {
        _isApplyingToTakeSeat.value = false
    }
}