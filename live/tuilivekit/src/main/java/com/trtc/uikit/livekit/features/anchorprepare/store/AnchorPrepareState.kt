package com.trtc.uikit.livekit.features.anchorprepare.store

import com.trtc.uikit.livekit.common.DEFAULT_COVER_URL
import com.trtc.uikit.livekit.features.anchorprepare.LiveStreamPrivacyStatus
import kotlinx.coroutines.flow.MutableStateFlow

class AnchorPrepareState {
    companion object {
        const val MAX_INPUT_BYTE_LENGTH = 100
        val COVER_URL_LIST = arrayOf(
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover1.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover2.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover3.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover4.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover5.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover6.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover7.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover8.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover9.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover10.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover11.png",
            "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover12.png"
        )
    }

    var selfUserId: String = ""
    var selfUserName: String = ""
    var roomId: String = ""
    val useFrontCamera = MutableStateFlow(true)
    val coverURL = MutableStateFlow(DEFAULT_COVER_URL)
    val liveMode = MutableStateFlow(LiveStreamPrivacyStatus.PUBLIC)
    val coGuestTemplateId = MutableStateFlow(600)
    val coHostTemplateId = MutableStateFlow(600)
    val roomName = MutableStateFlow("")
}