package com.trtc.uikit.livekit.features.anchorboardcast.manager.module

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.tencent.cloud.tuikit.engine.extension.TUILiveBattleManager
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.rtmp.TXLiveBase
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.features.anchorboardcast.manager.api.IAnchorAPI
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorState
import com.trtc.uikit.livekit.features.anchorboardcast.state.BattleState
import io.trtc.tuikit.atomicxcore.api.live.BattleConfig
import io.trtc.tuikit.atomicxcore.api.live.BattleInfo
import io.trtc.tuikit.atomicxcore.api.live.BattleStore
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import io.trtc.tuikit.atomicxcore.api.login.LoginStore

class BattleManager(state: AnchorState, service: IAnchorAPI) : BaseManager(state, service) {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun onRequestBattle(battleId: String, requestedUserIdList: List<String>) {
        battleState.battleId = battleId
        battleState.isInWaiting.value = true
        val currentRequests = battleState.sentBattleRequests.value?.toMutableList() ?: mutableListOf()
        currentRequests.addAll(requestedUserIdList)
        battleState.sentBattleRequests.value = currentRequests
    }

    fun onCanceledBattle() {
        battleState.isInWaiting.value = false
        battleState.sentBattleRequests.value = arrayListOf()
    }

    fun onResponseBattle() {
        removeBattleRequestReceived()
    }

    fun onExitBattle() {
        resetState()
    }

    fun resetOnDisplayResult() {
        mainHandler.removeCallbacksAndMessages(null)
        if (battleState.isOnDisplayResult.value == true) {
            battleState.isOnDisplayResult.value = false
        }
    }

    fun isBattleDraw(): Boolean {
        val list = battleState.battledUsers.value
        if (list.isEmpty()) {
            return false
        }
        val firstUser = list[0]
        val lastUser = list[list.size - 1]
        return firstUser.ranking == lastUser.ranking
    }

    fun isSelfInBattle(): Boolean {
        val userList = battleState.battledUsers.value
        val selfUserId = TUILogin.getUserId()
        return userList.any { TextUtils.equals(selfUserId, it.userId) }
    }

    fun onBattleStarted(battleInfo: BattleInfo?, inviter: SeatUserInfo, invitees: List<SeatUserInfo>) {
        if (battleInfo == null || battleState.isBattleRunning.value == true) {
            return
        }
        battleState.battleId = battleInfo.battleID
        battleState.battleConfig = battleInfo.config
        var duration = (battleInfo.config.duration + battleInfo.startTime - getCurrentTimestamp() / 1000).toInt()
        duration = minOf(duration, battleInfo.config.duration)
        duration = maxOf(duration, 0)
        battleState.durationCountDown.value = duration

        val countdownRunnable = object : Runnable {
            override fun run() {
                val t = battleState.durationCountDown.value ?: 0
                if (t > 0) {
                    battleState.durationCountDown.value = t - 1
                    mainHandler.postDelayed(this, 1000)
                }
            }
        }
        mainHandler.postDelayed(countdownRunnable, 1000)

        val users = arrayListOf<SeatUserInfo>().apply {
            addAll(invitees)
            add(inviter)
        }
        val list = battleState.battledUsers.value?.toMutableList() ?: mutableListOf()
        for (user in users) {
            val battleUser = BattleState.BattleUser(user).apply {
                score = BattleStore.create(state.roomId).battleState.battleScore.value[user.userID] ?: 0
            }
            list.add(battleUser)
        }
        sortBattleUsersByScore(list)
        battleState.isInWaiting.value = false
        battleState.isBattleRunning.value = true
        battleState.battledUsers.value = list
        battleState.isShowingStartView = true
    }

    fun onBattleEnded(battleInfo: BattleInfo?) {
        mainHandler.removeCallbacksAndMessages(null)
        battleState.isShowingStartView = false
        battleState.battleId = ""
        battleState.battleConfig = BattleConfig()
        battleState.sentBattleRequests.value = arrayListOf()

        battleInfo?.let {
            val list = battleState.battledUsers.value?.toMutableList() ?: mutableListOf()
            for (battleUser in list) {
                battleUser.score =
                    BattleStore.create(state.roomId).battleState.battleScore.value[battleUser.userId] ?: 0
            }
            sortBattleUsersByScore(list)
            battleState.battledUsers.value = list
        }
        battleState.isBattleRunning.value = false
        mainHandler.removeCallbacksAndMessages(null)

        val connectedList = CoHostStore.create(state.roomId).coHostState.connected.value
        if (connectedList.isEmpty()) {
            battleState.isOnDisplayResult.value = false
            resetState()
            return
        }
        battleState.isOnDisplayResult.value = true
        mainHandler.postDelayed({
            battleState.isOnDisplayResult.value = false
            mainHandler.postDelayed({ resetState() }, 100)
        }, BattleState.BATTLE_END_INFO_DURATION * 1000L)
    }

    fun onBattleScoreChanged(users: List<TUILiveBattleManager.BattleUser>?) {
        if (users == null || users.isEmpty()) {
            return
        }
        val list = battleState.battledUsers.value?.toMutableList() ?: mutableListOf()
        for (user in users) {
            for (battleUser in list) {
                if (battleUser.userId == user.userId) {
                    battleUser.score = user.score
                    break
                }
            }
        }
        sortBattleUsersByScore(list)
        battleState.battledUsers.value = list
    }

    fun onUserExitBattle(user: SeatUserInfo?) {
        if (user == null) {
            return
        }
        val users = battleState.battledUsers.value?.toMutableList() ?: mutableListOf()
        var exitUser: BattleState.BattleUser? = null
        for (battleUser in users) {
            if (battleUser.userId == user.userID) {
                exitUser = battleUser
                break
            }
        }
        if (users.size == 2) {
            return
        }
        exitUser?.let {
            users.remove(it)
            sortBattleUsersByScore(users)
            battleState.battledUsers.value = users
        }
    }

    fun onBattleRequestReceived(battleId: String?, inviter: SeatUserInfo?) {
        battleId?.let {
            battleState.battleId = it
        }
        inviter?.let {
            battleState.receivedBattleRequest.value = BattleState.BattleUser(it)
        }
    }

    fun onBattleRequestCancelled(inviter: SeatUserInfo?) {
        removeBattleRequestReceived()
        val context = ContextProvider.getApplicationContext()
        val content = context.getString(
            R.string.common_battle_inviter_cancel,
            inviter?.userName
        )
        showToast(content)
    }

    fun onBattleRequestAccept(invitee: SeatUserInfo?) {
        invitee?.let {
            removeSentBattleRequest(it.userID)
        }
    }

    fun onBattleRequestReject(invitee: SeatUserInfo?) {
        invitee?.let {
            removeSentBattleRequest(it.userID)
            val context = ContextProvider.getApplicationContext()
            val content = context.getString(
                R.string.common_battle_invitee_reject,
                it.userName
            )
            showToast(content)
        }
    }

    fun onBattleRequestTimeout(inviter: SeatUserInfo?, invitee: SeatUserInfo?) {
        val selfUserId = LoginStore.shared.loginState.loginUserInfo.value?.userID
        if (TextUtils.equals(inviter?.userID, selfUserId)) {
            battleState.sentBattleRequests.value = arrayListOf()
            battleState.isInWaiting.value = false
        } else {
            removeBattleRequestReceived()
            invitee?.let {
                removeSentBattleRequest(it.userID)
            }
        }
        val context = ContextProvider.getApplicationContext()
        showToast(context.getString(R.string.common_battle_invitation_timeout))
    }

    override fun destroy() {
        mainHandler.removeCallbacksAndMessages(null)
        resetState()
    }

    private fun getCurrentTimestamp(): Long {
        val networkTimestamp = TXLiveBase.getNetworkTimestamp()
        val localTimestamp = System.currentTimeMillis()
        return if (networkTimestamp > 0) networkTimestamp else localTimestamp
    }

    private fun sortBattleUsersByScore(users: MutableList<BattleState.BattleUser>) {
        users.sortByDescending { it.score }
        for (i in users.indices) {
            val user = users[i]
            user.ranking = if (i == 0) {
                1
            } else {
                val preUser = users[i - 1]
                if (preUser.score == user.score) preUser.ranking else preUser.ranking + 1
            }
        }
    }

    private fun removeBattleRequestReceived() {
        battleState.receivedBattleRequest.value = null
    }

    private fun removeSentBattleRequest(userId: String) {
        val sendRequests = battleState.sentBattleRequests.value?.toMutableList() ?: mutableListOf()
        val iterator = sendRequests.iterator()
        while (iterator.hasNext()) {
            val sendUserId = iterator.next()
            if (TextUtils.equals(sendUserId, userId)) {
                iterator.remove()
                break
            }
        }
        if (sendRequests.isEmpty()) {
            battleState.isInWaiting.value = false
        }
        battleState.sentBattleRequests.value = sendRequests
    }

    private fun resetState() {
        battleState.battledUsers.value = arrayListOf()
        battleState.sentBattleRequests.value = arrayListOf()
        battleState.receivedBattleRequest.value = null
        battleState.isInWaiting.value = null
        battleState.isBattleRunning.value = null
        battleState.isOnDisplayResult.value = null
        battleState.durationCountDown.value = 0
        battleState.battleConfig = BattleConfig()
        battleState.battleId = ""
        battleState.isShowingStartView = false
    }

    companion object {
        private fun showToast(tips: String) {
            val context = ContextProvider.getApplicationContext()
            val view = LayoutInflater.from(context).inflate(R.layout.livekit_connection_toast, null, false)

            val text = view.findViewById<TextView>(R.id.tv_toast_text)
            text.text = tips
            val image = view.findViewById<ImageView>(R.id.iv_toast_image)
            image.setImageResource(R.drawable.livekit_connection_toast_icon)

            val toast = Toast(view.context).apply {
                duration = Toast.LENGTH_SHORT
                setView(view)
            }
            toast.show()
        }
    }
}