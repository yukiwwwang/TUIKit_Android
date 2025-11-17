package com.trtc.uikit.livekit.features.audiencecontainer.store

import kotlinx.coroutines.flow.MutableStateFlow

object AudienceContainerConfig {
    val disableSliding: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val disableHeaderFloatWin: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val disableHeaderLiveData: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val disableHeaderVisitorCnt: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val disableFooterCoGuest: MutableStateFlow<Boolean> = MutableStateFlow(false)
}