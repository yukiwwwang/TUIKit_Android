package com.trtc.uikit.livekit.features.anchorboardcast.state

import android.graphics.Rect
import com.tencent.cloud.tuikit.engine.extension.TUILiveBattleManager
import io.trtc.tuikit.atomicxcore.api.live.BattleConfig
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import kotlinx.coroutines.flow.MutableStateFlow

class BattleState {

    companion object {
        const val BATTLE_REQUEST_TIMEOUT = 10
        const val BATTLE_DURATION = 30
        const val BATTLE_END_INFO_DURATION = 5
    }

    val battledUsers = MutableStateFlow<List<BattleUser>>(arrayListOf())
    val sentBattleRequests = MutableStateFlow<List<String>>(arrayListOf())
    val receivedBattleRequest = MutableStateFlow<BattleUser?>(null)
    val isInWaiting = MutableStateFlow<Boolean?>(null)
    val isBattleRunning = MutableStateFlow<Boolean?>(null)
    val isOnDisplayResult = MutableStateFlow<Boolean?>(null)
    val durationCountDown = MutableStateFlow(0)
    var battleConfig = BattleConfig()
    var battleId = ""
    var isShowingStartView = false

    class BattleUser {
        var roomId: String = ""
        var userId: String = ""
        var userName: String = ""
        var avatarUrl: String = ""
        var score: Int = 0
        var ranking: Int = 0
        var rect: Rect = Rect()

        constructor()

        constructor(battleUser: TUILiveBattleManager.BattleUser) {
            roomId = battleUser.roomId
            userId = battleUser.userId
            userName = battleUser.userName
            avatarUrl = battleUser.avatarUrl
            score = battleUser.score
        }

        constructor(user: SeatUserInfo) {
            roomId = user.liveID
            userId = user.userID
            userName = user.userName
            avatarUrl = user.avatarURL
        }
    }
}