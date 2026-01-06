package com.trtc.uikit.livekit.features.audiencecontainer.store.access

import android.text.TextUtils
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.audiencecontainer.AudienceContainerViewDefine
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.login.LoginStore

class TUILiveListDataSource : AudienceContainerViewDefine.LiveListDataSource {

    companion object {
        private val LOGGER = LiveKitLogger.getComponentLogger("AudienceDataSource")
        private const val FETCH_LIST_COUNT = 20
        private const val SDK_NOT_INITIALIZED = -1002
    }

    override fun fetchLiveList(
        param: AudienceContainerViewDefine.FetchLiveListParam,
        callback: AudienceContainerViewDefine.LiveListCallback
    ) {
        val userInfo = LoginStore.shared.loginState.loginUserInfo.value
        if (userInfo == null || TextUtils.isEmpty(userInfo.userID)) {
            LOGGER.warn("TUIRoomEngine login first")
            callback.onError(SDK_NOT_INITIALIZED, "message")
            return
        }
        val liveListStore = LiveListStore.shared()
        liveListStore.fetchLiveList(param.cursor, FETCH_LIST_COUNT, object: CompletionHandler {
            override fun onSuccess() {
                callback.onSuccess(
                    liveListStore.liveState.liveListCursor.value,
                    liveListStore.liveState.liveList.value
                )
            }

            override fun onFailure(code: Int, desc: String) {
                ErrorLocalized.onError(code)
                callback.onError(code, desc)
            }
        })
    }
}
