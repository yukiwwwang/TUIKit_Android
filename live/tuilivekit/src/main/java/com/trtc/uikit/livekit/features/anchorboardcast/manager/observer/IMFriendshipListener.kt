package com.trtc.uikit.livekit.features.anchorboardcast.manager.observer

import com.tencent.imsdk.v2.V2TIMFriendshipListener
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import java.lang.ref.WeakReference

class IMFriendshipListener(liveStreamManager: AnchorManager) : V2TIMFriendshipListener() {
    private val liveManager = WeakReference(liveStreamManager)

    override fun onMyFollowingListChanged(userInfoList: List<V2TIMUserFullInfo>, isAdd: Boolean) {
        liveManager.get()?.getUserManager()?.onMyFollowingListChanged(userInfoList, isAdd)
    }
}