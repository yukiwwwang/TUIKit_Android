package com.trtc.uikit.livekit.features.anchorboardcast.store

import kotlinx.coroutines.flow.MutableStateFlow

object AnchorConfig {
    val disableHeaderLiveData = MutableStateFlow(false)
    val disableHeaderVisitorCnt = MutableStateFlow(false)
    val disableFooterCoGuest = MutableStateFlow(false)
    val disableFooterCoHost = MutableStateFlow(false)
    val disableFooterBattle = MutableStateFlow(false)
    val disableFooterSoundEffect = MutableStateFlow(false)
}