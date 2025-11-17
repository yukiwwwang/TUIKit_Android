package com.trtc.uikit.livekit.features.anchorboardcast.manager.api.impl

import com.google.gson.Gson
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine.ExtensionType.LIVE_LIST_MANAGER
import com.tencent.cloud.tuikit.engine.common.TUIVideoView
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager.LiveInfo
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.tencent.imsdk.v2.V2TIMFollowOperationResult
import com.tencent.imsdk.v2.V2TIMFollowTypeCheckResult
import com.tencent.imsdk.v2.V2TIMFriendshipListener
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.anchorboardcast.manager.api.IAnchorAPI

class AnchorAPIImpl : IAnchorAPI {
    private val logger = LiveKitLogger.getFeaturesLogger("AnchorAPIImpl")
    private val tuiRoomEngine: TUIRoomEngine = TUIRoomEngine.sharedInstance()
    private val tuiLiveListManager: TUILiveListManager =
        tuiRoomEngine.getExtension(LIVE_LIST_MANAGER) as TUILiveListManager

    override fun addRoomEngineObserver(observer: TUIRoomObserver) {
        logger.info("${hashCode()} addRoomEngineObserver:[observer:${observer.hashCode()}]")
        tuiRoomEngine.addObserver(observer)
    }

    override fun removeRoomEngineObserver(observer: TUIRoomObserver) {
        logger.info("${hashCode()} removeRoomEngineObserver:[observer:${observer.hashCode()}]")
        tuiRoomEngine.removeObserver(observer)
    }

    override fun addFriendListener(listener: V2TIMFriendshipListener) {
        logger.info("${hashCode()} addFriendListener:[listener:${listener.hashCode()}]")
        V2TIMManager.getFriendshipManager().addFriendListener(listener)
    }

    override fun removeFriendListener(listener: V2TIMFriendshipListener) {
        logger.info("${hashCode()} removeFriendListener:[observer:${listener.hashCode()}]")
        V2TIMManager.getFriendshipManager().removeFriendListener(listener)
    }

    override fun getLiveInfo(roomId: String, callback: TUILiveListManager.LiveInfoCallback) {
        tuiLiveListManager.getLiveInfo(roomId, object : TUILiveListManager.LiveInfoCallback {
            override fun onSuccess(liveInfo: LiveInfo) {
                logger.info("${hashCode()} getLiveInfo :[onSuccess:[liveInfo${Gson().toJson(liveInfo)}]]")
                callback.onSuccess(liveInfo)
            }

            override fun onError(error: TUICommonDefine.Error, message: String) {
                logger.error("${hashCode()} getLiveInfo:[onError:[error:$error,message:$message]]")
                callback.onError(error, message)
            }
        })
    }

    override fun getUserList(nextSequence: Long, callback: TUIRoomDefine.GetUserListCallback) {
        logger.info("${hashCode()} getUserList:[nextSequence:$nextSequence]")
        tuiRoomEngine.getUserList(nextSequence, object : TUIRoomDefine.GetUserListCallback {
            override fun onSuccess(userListResult: TUIRoomDefine.UserListResult) {
                logger.info("${hashCode()} getUserList:[onSuccess]")
                callback.onSuccess(userListResult)
            }

            override fun onError(error: TUICommonDefine.Error, message: String) {
                logger.error("${hashCode()} getUserList:[onError:[error:$error,message:$message]]")
                callback.onError(error, message)
            }
        })
    }

    override fun getUserInfo(userId: String, callback: TUIRoomDefine.GetUserInfoCallback) {
        logger.info("${hashCode()} getUserInfo:[userId:$userId]")
        tuiRoomEngine.getUserInfo(userId, object : TUIRoomDefine.GetUserInfoCallback {
            override fun onSuccess(userInfo: TUIRoomDefine.UserInfo) {
                logger.info("${hashCode()} getUserInfo:[onSuccess]")
                callback.onSuccess(userInfo)
            }

            override fun onError(error: TUICommonDefine.Error, message: String) {
                logger.error("${hashCode()} getUserInfo:[onError:[error:$error,message:$message]]")
                callback.onError(error, message)
            }
        })
    }

    override fun setLocalVideoView(videoView: TUIVideoView) {
        logger.info("${hashCode()} setLocalVideoView:[videoView:$videoView]")
        tuiRoomEngine.setLocalVideoView(videoView)
    }

    override fun followUser(userIDList: List<String>, callback: V2TIMValueCallback<List<V2TIMFollowOperationResult>>) {
        logger.info("${hashCode()} followUser:[userIDList:$userIDList]")
        V2TIMManager.getFriendshipManager().followUser(
            userIDList,
            object : V2TIMValueCallback<List<V2TIMFollowOperationResult>> {
                override fun onSuccess(results: List<V2TIMFollowOperationResult>) {
                    logger.info("${hashCode()} followUser:[onSuccess:[results:${Gson().toJson(results)}]]")
                    callback.onSuccess(results)
                }

                override fun onError(code: Int, message: String) {
                    logger.error("${hashCode()} followUser:[onSuccess:[code:$code,message:$message]]")
                    callback.onError(code, message)
                }
            })
    }

    override fun unfollowUser(
        userIDList: List<String>,
        callback: V2TIMValueCallback<List<V2TIMFollowOperationResult>>
    ) {
        logger.info("${hashCode()} unfollowUser:[userIDList:$userIDList]")
        V2TIMManager.getFriendshipManager().unfollowUser(
            userIDList,
            object : V2TIMValueCallback<List<V2TIMFollowOperationResult>> {
                override fun onSuccess(results: List<V2TIMFollowOperationResult>) {
                    logger.info("${hashCode()} unfollowUser:[onSuccess:[results:${Gson().toJson(results)}]]")
                    callback.onSuccess(results)
                }

                override fun onError(code: Int, message: String) {
                    logger.error("${hashCode()} unfollowUser:[onSuccess:[code:$code,message:$message]]")
                    callback.onError(code, message)
                }
            })
    }

    override fun checkFollowType(
        userIDList: List<String>,
        callback: V2TIMValueCallback<List<V2TIMFollowTypeCheckResult>>
    ) {
        logger.info("${hashCode()} checkFollowType:[userIDList:$userIDList]")
        V2TIMManager.getFriendshipManager().checkFollowType(
            userIDList,
            object : V2TIMValueCallback<List<V2TIMFollowTypeCheckResult>> {
                override fun onSuccess(results: List<V2TIMFollowTypeCheckResult>) {
                    logger.info("${hashCode()} checkFollowType:[onSuccess:[results:${Gson().toJson(results)}]]")
                    callback.onSuccess(results)
                }

                override fun onError(code: Int, message: String) {
                    logger.error("${hashCode()} checkFollowType:[onSuccess:[code:$code,message:$message]]")
                    callback.onError(code, message)
                }
            })
    }

    override fun fetchLiveList(cursor: String, count: Int, callback: TUILiveListManager.LiveInfoListCallback) {
        logger.info("${hashCode()} fetchLiveList:[cursor:$cursor,count:$count]")
        tuiLiveListManager.fetchLiveList(cursor, count, object : TUILiveListManager.LiveInfoListCallback {
            override fun onSuccess(liveInfoListResult: TUILiveListManager.LiveInfoListResult) {
                logger.info(
                    "${hashCode()} fetchLiveList:[onSuccess:[liveInfoListResult:${
                        Gson().toJson(
                            liveInfoListResult
                        )
                    }"
                )
                callback.onSuccess(liveInfoListResult)
            }

            override fun onError(error: TUICommonDefine.Error, s: String) {
                logger.error("${hashCode()} fetchLiveList:[onError:[error:$error,s:$s]]")
                callback.onError(error, s)
            }
        })
    }

}