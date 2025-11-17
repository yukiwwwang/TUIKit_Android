package com.trtc.uikit.livekit.features.anchorboardcast.state

import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine.Role.GENERAL_USER
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Objects

class UserState {
    val userList = MutableStateFlow<LinkedHashSet<UserInfo>>(LinkedHashSet())
    val followingUserList = MutableStateFlow<LinkedHashSet<String>>(LinkedHashSet())

    class UserInfo {
        var userId: String = ""
        val name = MutableStateFlow("")
        val avatarUrl = MutableStateFlow("")
        val role = MutableStateFlow(GENERAL_USER)
        val isMessageDisabled = MutableStateFlow(false)

        constructor()

        constructor(userId: String) {
            this.userId = userId
        }

        constructor(userInfo: TUIRoomDefine.UserInfo) {
            updateState(userInfo)
        }

        fun updateState(userInfo: TUIRoomDefine.UserInfo) {
            this.userId = userInfo.userId
            this.name.value = userInfo.userName
            this.avatarUrl.value = userInfo.avatarUrl
            this.role.value = userInfo.userRole
            this.isMessageDisabled.value = userInfo.isMessageDisabled
        }
    }
}