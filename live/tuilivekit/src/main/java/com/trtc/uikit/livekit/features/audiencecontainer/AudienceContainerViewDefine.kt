package com.trtc.uikit.livekit.features.audiencecontainer

import io.trtc.tuikit.atomicxcore.api.live.LiveInfo

object AudienceContainerViewDefine {
    interface AudienceContainerViewListener {
        fun onLiveEnded(roomId: String, ownerName: String, ownerAvatarUrl: String)
        fun onPictureInPictureClick()
    }

    class FetchLiveListParam {
        var cursor: String? = null
    }

    interface LiveListCallback {
        fun onSuccess(cursor: String, liveInfoList: List<LiveInfo>)
        fun onError(code: Int, message: String)
    }

    interface LiveListDataSource {
        fun fetchLiveList(param: FetchLiveListParam, callback: LiveListCallback)
    }
}
