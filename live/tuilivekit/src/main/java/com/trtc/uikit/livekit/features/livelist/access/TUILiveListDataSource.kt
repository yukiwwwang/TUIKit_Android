package com.trtc.uikit.livekit.features.livelist.access

import android.text.TextUtils
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine.Error.SDK_NOT_INITIALIZED
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.livelist.FetchLiveListParam
import com.trtc.uikit.livekit.features.livelist.LiveListDataSource
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore

class TUILiveListDataSource : LiveListDataSource {

    companion object {
        private val LOGGER = LiveKitLogger.getComponentLogger("TUILiveListDataSource")
        private const val FETCH_LIST_COUNT = 6
    }

    override fun fetchLiveList(param: FetchLiveListParam, callback: CompletionHandler?) {
        val userInfo = TUIRoomEngine.getSelfInfo()
        if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
            LOGGER.warn("TUIRoomEngine login first")
            callback?.onFailure(SDK_NOT_INITIALIZED.value, "message")
            return
        }
        val liveListStore = LiveListStore.shared()
        liveListStore.fetchLiveList(param.cursor, FETCH_LIST_COUNT, object : CompletionHandler {
            override fun onSuccess() {
                LOGGER.info("fetchLiveList onSuccess.")
                callback?.onSuccess()
            }

            override fun onFailure(code: Int, desc: String) {
                LOGGER.error("fetchLiveList failed:code:$code,desc:$desc")
                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                callback?.onFailure(code, desc)
            }
        })
    }
}
