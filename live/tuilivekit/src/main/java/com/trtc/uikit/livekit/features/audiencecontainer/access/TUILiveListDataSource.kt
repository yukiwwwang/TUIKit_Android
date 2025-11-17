package com.trtc.uikit.livekit.features.audiencecontainer.access

import android.text.TextUtils
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine.Error.SDK_NOT_INITIALIZED
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.completionHandler
import com.trtc.uikit.livekit.features.audiencecontainer.AudienceContainerViewDefine
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore

class TUILiveListDataSource : AudienceContainerViewDefine.LiveListDataSource {

    companion object {
        private val LOGGER = LiveKitLogger.getComponentLogger("AudienceDataSource")
        private const val FETCH_LIST_COUNT = 20
    }

    override fun fetchLiveList(
        param: AudienceContainerViewDefine.FetchLiveListParam,
        callback: AudienceContainerViewDefine.LiveListCallback
    ) {
        val userInfo = TUIRoomEngine.getSelfInfo()
        if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
            LOGGER.warn("TUIRoomEngine login first")
            callback.onError(SDK_NOT_INITIALIZED.value, "message")
            return
        }
        val liveListStore = LiveListStore.shared()
        liveListStore.fetchLiveList(param.cursor, FETCH_LIST_COUNT, completionHandler {
            onSuccess {
                callback.onSuccess(
                    liveListStore.liveState.liveListCursor.value,
                    liveListStore.liveState.liveList.value
                )
            }
            onError { code, message ->
                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                callback.onError(code, message)
            }
        })
    }
}
