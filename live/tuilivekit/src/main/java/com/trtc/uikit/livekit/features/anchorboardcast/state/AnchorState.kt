package com.trtc.uikit.livekit.features.anchorboardcast.state

import io.trtc.tuikit.atomicxcore.api.live.LiveInfo
import kotlinx.coroutines.flow.MutableStateFlow

class AnchorState {
    var roomId: String = ""
    var liveInfo: LiveInfo = LiveInfo()
    val lockAudioUserList = MutableStateFlow<LinkedHashSet<String>>(LinkedHashSet())
    val lockVideoUserList = MutableStateFlow<LinkedHashSet<String>>(LinkedHashSet())
    val coHostState = CoHostState()
    val userState = UserState()
    val mediaState = MediaState()
    val battleState = BattleState()
}