package com.trtc.uikit.livekit.features.anchorboardcast.state

import com.tencent.cloud.tuikit.engine.extension.TUILiveConnectionManager
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import kotlinx.coroutines.flow.MutableStateFlow

class CoHostState {
    var recommendedCursor = ""
    val recommendUsers = MutableStateFlow<List<ConnectionUser>>(arrayListOf())
    var isLoadMore = false
    var isLastPage = false
    var coHostTemplateId = 600

    enum class ConnectionStatus {
        UNKNOWN,
        INVITING
    }

    class ConnectionUser {
        var roomId: String = ""
        var userId: String = ""
        var userName: String = ""
        var avatarUrl: String = ""
        var joinConnectionTime: Long = 0
        var connectionStatus: ConnectionStatus = ConnectionStatus.UNKNOWN

        constructor(liveUser: TUILiveListManager.LiveInfo) {
            this.roomId = liveUser.roomId
            this.userId = liveUser.ownerId
            this.userName = liveUser.ownerName
            this.avatarUrl = liveUser.ownerAvatarUrl
            this.joinConnectionTime = 0
            this.connectionStatus = ConnectionStatus.UNKNOWN
        }

        constructor(connectionUser: TUILiveConnectionManager.ConnectionUser, connectionStatus: ConnectionStatus) {
            this.roomId = connectionUser.roomId
            this.userId = connectionUser.userId
            this.userName = connectionUser.userName
            this.avatarUrl = connectionUser.avatarUrl
            this.joinConnectionTime = connectionUser.joinConnectionTime
            this.connectionStatus = connectionStatus
        }

        constructor(connectionUser: ConnectionUser, connectionStatus: ConnectionStatus) {
            this.roomId = connectionUser.roomId
            this.userId = connectionUser.userId
            this.userName = connectionUser.userName
            this.avatarUrl = connectionUser.avatarUrl
            this.joinConnectionTime = connectionUser.joinConnectionTime
            this.connectionStatus = connectionStatus
        }
    }
}