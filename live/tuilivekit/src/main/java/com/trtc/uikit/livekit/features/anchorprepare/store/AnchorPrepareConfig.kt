package com.trtc.uikit.livekit.features.anchorprepare.store

import kotlinx.coroutines.flow.MutableStateFlow

object AnchorPrepareConfig {
    val disableFeatureMenu = MutableStateFlow(false)
    val disableMenuSwitchButton = MutableStateFlow(false)
    val disableMenuBeautyButton = MutableStateFlow(false)
    val disableMenuAudioEffectButton = MutableStateFlow(false)
}