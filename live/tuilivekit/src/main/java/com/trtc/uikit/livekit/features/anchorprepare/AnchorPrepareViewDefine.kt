package com.trtc.uikit.livekit.features.anchorprepare

import androidx.lifecycle.LiveData
import com.trtc.uikit.livekit.R

data class PrepareState(
    @JvmField val coverURL: LiveData<String>,
    @JvmField val liveMode: LiveData<LiveStreamPrivacyStatus>,
    @JvmField val roomName: LiveData<String>,
    @JvmField val coGuestTemplateId: LiveData<Int>,
    @JvmField val coHostTemplateId: LiveData<Int>
)

interface AnchorPrepareViewListener {
    fun onClickStartButton()
    fun onClickBackButton()
}

enum class LiveStreamPrivacyStatus(val resId: Int) {
    PUBLIC(R.string.common_stream_privacy_status_default),
    PRIVACY(R.string.common_stream_privacy_status_privacy)
}