package com.trtc.uikit.livekit.features.livelist.view.access

import android.text.TextUtils
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.livelist.FetchLiveListParam
import com.trtc.uikit.livekit.features.livelist.LiveListDataSource
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.login.LoginStore

class TUILiveListDataSource : LiveListDataSource {

    companion object {
        private val LOGGER = LiveKitLogger.getComponentLogger("TUILiveListDataSource")
        private const val FETCH_LIST_COUNT = 6
        private const val SDK_NOT_INITIALIZED = -1002
    }

    override fun fetchLiveList(param: FetchLiveListParam, callback: CompletionHandler?) {
        val userInfo = LoginStore.shared.loginState.loginUserInfo.value
        if (userInfo == null || TextUtils.isEmpty(userInfo.userID)) {
            LOGGER.warn("TUIRoomEngine login first")
            callback?.onFailure(SDK_NOT_INITIALIZED, "message")
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
                ErrorLocalized.onError(code)
                callback?.onFailure(code, desc)
            }
        })
    }
}
