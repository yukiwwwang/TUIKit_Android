package com.trtc.uikit.livekit.features.anchorboardcast.manager.module

import android.text.TextUtils
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.anchorboardcast.manager.api.IAnchorAPI
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorState
import com.trtc.uikit.livekit.features.anchorboardcast.state.CoHostState
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import kotlinx.coroutines.flow.update

class CoHostManager(state: AnchorState, service: IAnchorAPI) : BaseManager(state, service) {
    companion object {
        private val logger = LiveKitLogger.getFeaturesLogger("CoHostManager")
        private const val FETCH_LIST_COUNT = 20
    }

    fun isSelfInCoHost(): Boolean {
        val userList = CoHostStore.create(state.roomId).coHostState.connected.value
        val selfUserId = TUILogin.getUserId() ?: return false
        return userList.any { TextUtils.equals(selfUserId, it.userID) }
    }

    fun fetchLiveList(isRefresh: Boolean) {
        if (isRefresh) {
            coHostState.recommendedCursor = ""
        }
        coHostState.isLoadMore = true
        liveService.fetchLiveList(
            coHostState.recommendedCursor, FETCH_LIST_COUNT,
            object : TUILiveListManager.LiveInfoListCallback {
                override fun onSuccess(result: TUILiveListManager.LiveInfoListResult) {
                    handlerFetchLiveListSuccess(result, isRefresh)
                    coHostState.isLoadMore = false
                }

                override fun onError(error: TUICommonDefine.Error, message: String) {
                    logger.error("fetchLiveList failed:error:$error,errorCode:${error.value}message:$message")
                    ErrorLocalized.onError(error)
                    coHostState.isLoadMore = false
                }
            })
    }

    fun isConnected(roomId: String): Boolean {
        val connectedList = CoHostStore.create(state.roomId).coHostState.connected.value
        return connectedList.any { TextUtils.equals(it.liveID, roomId) }
    }

    fun setCoHostTemplateId(id: Int) {
        coHostState.coHostTemplateId = id
    }

    /******************************************  Observer *******************************************/
    fun onConnectionRequestReceived(inviter: SeatUserInfo?) {
    }

    fun onConnectionRequestAccept(invitee: SeatUserInfo?) {
    }

    fun onConnectionRequestReject(invitee: SeatUserInfo?) {
        invitee?.let {
            updateRecommendListStatus(it)
            val userName = invitee.userName.ifEmpty {
                invitee.userID
            }
            ToastUtil.toastShortMessage(
                ContextProvider.getApplicationContext().resources
                    .getString(R.string.common_request_rejected, userName)
            )
        }
    }

    fun onConnectionRequestTimeout(inviter: SeatUserInfo?, invitee: SeatUserInfo?) {
        invitee?.let {
            updateRecommendListStatus(it)
        }

        inviter?.let {
            if (it.userID == TUIRoomEngine.getSelfInfo().userId) {
                ToastUtil.toastShortMessage(
                    ContextProvider.getApplicationContext().resources
                        .getString(R.string.common_connect_invitation_timeout)
                )
            }
        }
    }

    override fun destroy() {
    }

    private fun addLiveToRecommendList(
        result: TUILiveListManager.LiveInfoListResult,
        list: MutableList<CoHostState.ConnectionUser>
    ) {
        val currentRecommendUsers = coHostState.recommendUsers.value
        val sentRequestList = CoHostStore.create(state.roomId).coHostState.invitees.value

        for (user in currentRecommendUsers) {
            var isInviting = false
            for (requestUser in sentRequestList) {
                if (user.roomId == requestUser.liveID) {
                    user.connectionStatus = CoHostState.ConnectionStatus.INVITING
                    isInviting = true
                    break
                }
            }
            if (!isInviting) {
                user.connectionStatus = CoHostState.ConnectionStatus.UNKNOWN
            }
        }

        for (liveInfo in result.liveInfoList) {
            val user = CoHostState.ConnectionUser(liveInfo)
            if (!isConnected(liveInfo.roomId)) {
                if (isInviting(liveInfo.roomId)) {
                    user.connectionStatus = CoHostState.ConnectionStatus.INVITING
                }
                list.add(user)
            }
        }
    }

    private fun isInviting(roomId: String): Boolean {
        val sentRequestList = CoHostStore.create(state.roomId).coHostState.invitees.value
        return sentRequestList.any { TextUtils.equals(it.liveID, roomId) }
    }

    private fun updateRecommendListStatus(invitee: SeatUserInfo) {
        coHostState.recommendUsers.update { list ->
            list.map { user ->
                if (user.roomId == invitee.liveID) {
                    val newUser = CoHostState.ConnectionUser(user, CoHostState.ConnectionStatus.UNKNOWN)
                    newUser
                } else {
                    user
                }
            }
        }
    }

    private fun handlerFetchLiveListSuccess(result: TUILiveListManager.LiveInfoListResult, isRefresh: Boolean) {
        coHostState.isLastPage = TextUtils.isEmpty(result.cursor)
        val list = if (isRefresh) {
            mutableListOf()
        } else {
            coHostState.recommendUsers.value.toMutableList()
        }
        addLiveToRecommendList(result, list)
        coHostState.recommendedCursor = result.cursor
        val tempList = list.filterNot { isConnected(it.roomId) }
        coHostState.recommendUsers.value = tempList
    }
}