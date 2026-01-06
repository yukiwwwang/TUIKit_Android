package com.trtc.uikit.livekit.component.roominfo.store

import kotlinx.coroutines.flow.MutableStateFlow

class RoomInfoState {
    var selfUserId: String = ""
    var roomId: String = ""
    var enableFollow: Boolean = true
    val ownerId = MutableStateFlow("")
    val ownerName = MutableStateFlow("")
    val ownerAvatarUrl = MutableStateFlow("")
    val fansNumber = MutableStateFlow(0L)
    val followingList = MutableStateFlow<Set<String>>(LinkedHashSet())
}