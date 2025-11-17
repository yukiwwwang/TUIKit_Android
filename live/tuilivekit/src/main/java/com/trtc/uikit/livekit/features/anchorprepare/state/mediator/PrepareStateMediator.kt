package com.trtc.uikit.livekit.features.anchorprepare.state.mediator

import androidx.lifecycle.LiveData
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.ReadOnlyLiveData
import com.trtc.uikit.livekit.features.anchorprepare.LiveStreamPrivacyStatus
import com.trtc.uikit.livekit.features.anchorprepare.state.AnchorPrepareState

class PrepareStateMediator(state: AnchorPrepareState) {
    companion object {
        private val LOGGER = LiveKitLogger.getFeaturesLogger("PrepareStateMediator")
    }

    val coverURL: LiveData<String> = ReadOnlyLiveData(state.coverURL) { value ->
        LOGGER.info(" AnchorPrepareState coverURL Change:$value")
        value
    }

    val liveMode: LiveData<LiveStreamPrivacyStatus> = ReadOnlyLiveData(state.liveMode) { value ->
        LOGGER.info(" AnchorPrepareState liveMode Change:$value")
        value
    }

    val roomName: LiveData<String> = ReadOnlyLiveData(state.roomName) { value ->
        LOGGER.info(" AnchorPrepareState roomName Change:$value")
        value
    }

    val coGuestTemplateId: LiveData<Int> = ReadOnlyLiveData(state.coGuestTemplateId) { value ->
        LOGGER.info(" AnchorPrepareState coGuestTemplateId Change:$value")
        value
    }

    val coHostTemplateId: LiveData<Int> = ReadOnlyLiveData(state.coHostTemplateId) { value ->
        LOGGER.info(" AnchorPrepareState coHostTemplateId Change:$value")
        value
    }

    fun destroy() {
        LOGGER.info("PrepareStateMediator State destroy")
        (coverURL as ReadOnlyLiveData<*>).destroy()
        (liveMode as ReadOnlyLiveData<*>).destroy()
        (roomName as ReadOnlyLiveData<*>).destroy()
        (coGuestTemplateId as ReadOnlyLiveData<*>).destroy()
        (coHostTemplateId as ReadOnlyLiveData<*>).destroy()
    }
}