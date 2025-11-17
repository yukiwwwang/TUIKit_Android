package com.trtc.uikit.livekit.features.anchorboardcast.manager.module

import android.text.TextUtils
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.imsdk.v2.V2TIMFollowOperationResult
import com.tencent.imsdk.v2.V2TIMFollowTypeCheckResult
import com.tencent.imsdk.v2.V2TIMFollowTypeCheckResult.V2TIM_FOLLOW_TYPE_IN_BOTH_FOLLOWERS_LIST
import com.tencent.imsdk.v2.V2TIMFollowTypeCheckResult.V2TIM_FOLLOW_TYPE_IN_MY_FOLLOWING_LIST
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.anchorboardcast.manager.api.IAnchorAPI
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorState
import com.trtc.uikit.livekit.features.anchorboardcast.state.UserState
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.LiveAudienceStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore

class UserManager(state: AnchorState, service: IAnchorAPI) : BaseManager(state, service) {
    private val logger = LiveKitLogger.getFeaturesLogger("UserManager")

    override fun destroy() {
    }

    fun getAudienceList() {
        liveService.getUserList(0, object : TUIRoomDefine.GetUserListCallback {
            override fun onSuccess(userListResult: TUIRoomDefine.UserListResult) {
                if (userListResult.userInfoList.isNotEmpty()) {
                    userState.userList.value.clear()
                    val userInfoSet = LinkedHashSet<UserState.UserInfo>()
                    for (userInfo in userListResult.userInfoList) {
                        if (userInfo.userId == LiveListStore.shared().liveState.currentLive.value.liveOwner.userID) {
                            continue
                        }
                        val liveUserInfo = UserState.UserInfo(userInfo)
                        userInfoSet.add(liveUserInfo)
                    }
                    addUserList(userInfoSet)
                }
            }

            override fun onError(error: TUICommonDefine.Error, message: String) {
                ErrorLocalized.onError(error)
            }
        })
    }

    fun getUserFromUserList(userId: String): UserState.UserInfo? {
        if (TextUtils.isEmpty(userId)) {
            return null
        }
        for (userInfo in userState.userList.value) {
            if (userId == userInfo.userId) {
                return userInfo
            }
        }
        return null
    }

    fun addUserInUserList(userInfo: TUIRoomDefine.UserInfo?): UserState.UserInfo? {
        if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
            return null
        }

        val user = UserState.UserInfo(userInfo)
        addUser(user)
        liveService.getUserInfo(user.userId, object : TUIRoomDefine.GetUserInfoCallback {
            override fun onSuccess(userInfo: TUIRoomDefine.UserInfo) {
                user.updateState(userInfo)
            }

            override fun onError(error: TUICommonDefine.Error, message: String) {
            }
        })
        return user
    }

    fun disableSendingMessageByAdmin(userId: String, isDisable: Boolean) {
        LiveAudienceStore.create(LiveListStore.shared().liveState.currentLive.value.liveID).disableSendMessage(
            userId, isDisable,
            object : CompletionHandler {
                override fun onSuccess() {
                    val userInfo = getUserFromUserList(userId)
                    userInfo?.isMessageDisabled?.value = isDisable
                }

                override fun onFailure(code: Int, desc: String) {
                    logger.error("disableSendingMessageByAdmin failed:code:$code,desc:$desc")
                    ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                }

            })
    }

    fun kickRemoteUserOutOfRoom(userId: String) {
        LiveAudienceStore.create(LiveListStore.shared().liveState.currentLive.value.liveID)
            .kickUserOutOfRoom(userId, object : CompletionHandler {
                override fun onSuccess() {
                    val userInfo = TUIRoomDefine.UserInfo().apply {
                        this.userId = userId
                    }
                    removeUser(userInfo)
                }

                override fun onFailure(code: Int, desc: String) {
                    logger.error("disableSendingMessageByAdmin failed:code:$code,desc:$desc")
                    ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                }
            })
    }

    fun followUser(userId: String) {
        val userIDList = listOf(userId)
        liveService.followUser(userIDList, object : V2TIMValueCallback<List<V2TIMFollowOperationResult>> {
            override fun onSuccess(v2TIMFollowOperationResults: List<V2TIMFollowOperationResult>) {
                updateFollowUserList(userId, true)
            }

            override fun onError(code: Int, desc: String) {
                logger.error("followUser failed:errorCode:$code message:$desc")
                ToastUtil.toastShortMessage("$code,$desc")
            }
        })
    }

    fun unfollowUser(userId: String) {
        val userIDList = listOf(userId)
        liveService.unfollowUser(userIDList, object : V2TIMValueCallback<List<V2TIMFollowOperationResult>> {
            override fun onSuccess(v2TIMFollowOperationResults: List<V2TIMFollowOperationResult>) {
                updateFollowUserList(userId, false)
            }

            override fun onError(code: Int, desc: String) {
                logger.error("unfollowUser failed:errorCode:$code message:$desc")
                ToastUtil.toastShortMessage("$code,$desc")
            }
        })
    }

    fun checkFollowUser(userId: String) {
        if (userId == TUILogin.getUserId()) {
            return
        }
        val userIDList = listOf(userId)
        checkFollowUserList(userIDList)
    }

    private fun checkFollowUserList(userIDList: List<String>) {
        liveService.checkFollowType(userIDList, object : V2TIMValueCallback<List<V2TIMFollowTypeCheckResult>> {
            override fun onSuccess(v2TIMFollowTypeCheckResults: List<V2TIMFollowTypeCheckResult>) {
                if (v2TIMFollowTypeCheckResults.isNotEmpty()) {
                    val result = v2TIMFollowTypeCheckResults[0]
                    val isAdd = V2TIM_FOLLOW_TYPE_IN_MY_FOLLOWING_LIST == result.followType ||
                            V2TIM_FOLLOW_TYPE_IN_BOTH_FOLLOWERS_LIST == result.followType
                    updateFollowUserList(result.userID, isAdd)
                }
            }

            override fun onError(code: Int, desc: String) {
                logger.error("checkFollowType failed:errorCode:$code message:$desc")
                ToastUtil.toastShortMessage("$code,$desc")
            }
        })
    }

    fun onRemoteUserEnterRoom(roomId: String, userInfo: TUIRoomDefine.UserInfo) {
        if (userInfo.userId == LiveListStore.shared().liveState.currentLive.value.liveOwner.userID) {
            return
        }
        val user = UserState.UserInfo(userInfo)
        addUser(user)
    }

    fun onRemoteUserLeaveRoom(roomId: String, userInfo: TUIRoomDefine.UserInfo) {
        removeUser(userInfo)
    }

    fun onUserInfoChanged(userInfo: TUIRoomDefine.UserInfo, modifyFlag: List<TUIRoomDefine.UserInfoModifyFlag>) {
        val userList = userState.userList.value
        for (info in userList) {
            if (TextUtils.equals(info.userId, userInfo.userId)) {
                if (modifyFlag.contains(TUIRoomDefine.UserInfoModifyFlag.USER_ROLE)) {
                    info.role.value = userInfo.userRole
                }
                break
            }
        }
    }

    fun onMyFollowingListChanged(userInfoList: List<V2TIMUserFullInfo>, isAdd: Boolean) {
        val userIdList = userInfoList.map { it.userID }
        checkFollowUserList(userIdList)
    }

    fun onSendMessageForUserDisableChanged(roomId: String, userId: String, isDisable: Boolean) {
        val userInfo = getUserFromUserList(userId)
        userInfo?.isMessageDisabled?.value = isDisable

        if (userId != TUILogin.getUserId()) {
            return
        }
        val context = ContextProvider.getApplicationContext()
        if (isDisable) {
            ToastUtil.toastShortMessage(context.resources.getString(R.string.common_send_message_disabled))
        } else {
            ToastUtil.toastShortMessage(context.resources.getString(R.string.common_send_message_enable))
        }
    }

    fun addUserList(list: Set<UserState.UserInfo>?) {
        if (list.isNullOrEmpty()) {
            return
        }
        val currentList = userState.userList.value
        currentList.addAll(list)
        userState.userList.value = currentList
    }

    fun addUser(userInfo: UserState.UserInfo?) {
        if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
            return
        }
        val currentList = userState.userList.value
        currentList.add(userInfo)
        userState.userList.value = currentList
    }

    fun removeUser(userInfo: TUIRoomDefine.UserInfo?) {
        if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
            return
        }
        val currentList = userState.userList.value
        currentList.remove(UserState.UserInfo(userInfo.userId))
        userState.userList.value = currentList
    }

    private fun updateFollowUserList(userId: String, isAdd: Boolean) {
        if (TextUtils.isEmpty(userId)) {
            return
        }
        val newList = userState.followingUserList.value.toMutableSet().apply {
            if (isAdd) {
                add(userId)
            } else {
                remove(userId)
            }
        }.let { LinkedHashSet(it) }

        userState.followingUserList.value = newList
    }
}