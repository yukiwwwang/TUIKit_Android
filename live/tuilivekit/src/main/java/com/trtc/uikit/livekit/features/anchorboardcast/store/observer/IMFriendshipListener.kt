package com.trtc.uikit.livekit.features.anchorboardcast.store.observer

import com.tencent.imsdk.v2.V2TIMFriendshipListener
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import com.trtc.uikit.livekit.features.anchorboardcast.store.AnchorStore
import java.lang.ref.WeakReference

class IMFriendshipListener(liveStreamManager: AnchorStore) : V2TIMFriendshipListener() {
    private val liveManager = WeakReference(liveStreamManager)

    override fun onMyFollowingListChanged(userInfoList: List<V2TIMUserFullInfo>, isAdd: Boolean) {
        liveManager.get()?.getUserStore()?.onMyFollowingListChanged(userInfoList, isAdd)
    }
}