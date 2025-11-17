package com.trtc.uikit.livekit.features.anchorboardcast.manager.api

import com.tencent.cloud.tuikit.engine.common.TUIVideoView
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.tencent.imsdk.v2.V2TIMFollowOperationResult
import com.tencent.imsdk.v2.V2TIMFollowTypeCheckResult
import com.tencent.imsdk.v2.V2TIMFriendshipListener
import com.tencent.imsdk.v2.V2TIMValueCallback

interface IAnchorAPI {
    fun addRoomEngineObserver(observer: TUIRoomObserver)

    fun removeRoomEngineObserver(observer: TUIRoomObserver)

    fun addFriendListener(listener: V2TIMFriendshipListener)

    fun removeFriendListener(listener: V2TIMFriendshipListener)

    fun getLiveInfo(roomId: String, callback: TUILiveListManager.LiveInfoCallback)

    /****************************************** User Business *******************************************/
    fun getUserList(nextSequence: Long, callback: TUIRoomDefine.GetUserListCallback)

    fun getUserInfo(userId: String, callback: TUIRoomDefine.GetUserInfoCallback)

    /****************************************** Media Business *******************************************/

    fun setLocalVideoView(view: TUIVideoView)

    /****************************************** IM Business *******************************************/
    fun followUser(userIDList: List<String>, callback: V2TIMValueCallback<List<V2TIMFollowOperationResult>>)

    fun unfollowUser(userIDList: List<String>, callback: V2TIMValueCallback<List<V2TIMFollowOperationResult>>)

    fun checkFollowType(userIDList: List<String>, callback: V2TIMValueCallback<List<V2TIMFollowTypeCheckResult>>)

    fun fetchLiveList(cursor: String, count: Int, callback: TUILiveListManager.LiveInfoListCallback)
}