package com.trtc.uikit.livekit.features.anchorboardcast.manager.module

import com.trtc.uikit.livekit.features.anchorboardcast.manager.api.IAnchorAPI
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorState
import com.trtc.uikit.livekit.features.anchorboardcast.state.BattleState
import com.trtc.uikit.livekit.features.anchorboardcast.state.CoHostState
import com.trtc.uikit.livekit.features.anchorboardcast.state.MediaState
import com.trtc.uikit.livekit.features.anchorboardcast.state.UserState

abstract class BaseManager(
    protected val state: AnchorState,
    protected val liveService: IAnchorAPI
) {
    internal val userState: UserState = state.userState
    internal val mediaState: MediaState = state.mediaState
    internal val coHostState: CoHostState = state.coHostState
    internal val battleState: BattleState = state.battleState

    internal abstract fun destroy()
}