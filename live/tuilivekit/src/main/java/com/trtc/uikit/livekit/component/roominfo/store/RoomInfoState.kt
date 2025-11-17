package com.trtc.uikit.livekit.component.roominfo.store

import androidx.lifecycle.MutableLiveData

class RoomInfoState {
    var selfUserId: String = ""
    var roomId: String = ""
    var enableFollow: Boolean = true
    val ownerId = MutableLiveData("")
    val ownerName = MutableLiveData("")
    val ownerAvatarUrl = MutableLiveData("")
    val fansNumber = MutableLiveData(0L)
    val followingList = MutableLiveData<Set<String>>(LinkedHashSet())
}