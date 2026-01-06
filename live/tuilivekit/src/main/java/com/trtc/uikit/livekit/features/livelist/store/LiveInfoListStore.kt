package com.trtc.uikit.livekit.features.livelist.store

import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.livelist.FetchLiveListParam
import com.trtc.uikit.livekit.features.livelist.LiveListDataSource
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore


class LiveInfoListStore(private val liveListDataSource: LiveListDataSource) {

    companion object {
        private val LOGGER = LiveKitLogger.getComponentLogger("LiveInfoListStore")
    }
    private val liveInfoList: MutableList<LiveInfo> = mutableListOf()

    fun refreshLiveList(callback: CompletionHandler?) {
        fetchLiveList(true, callback)
    }

    fun fetchLiveList(callback: CompletionHandler?) {
        fetchLiveList(false, callback)
    }

    private fun fetchLiveList(isRefresh: Boolean, callback: CompletionHandler?) {
        LOGGER.info("fetchLiveList start,isRefresh:$isRefresh")
        val cursor = LiveListStore.shared().liveState.liveListCursor.value
        val param = FetchLiveListParam(cursor = if (isRefresh || cursor.isEmpty()) "" else cursor)
        liveListDataSource.fetchLiveList(param, object : CompletionHandler {
            override fun onSuccess() {
                LOGGER.info("fetchLiveList onSuccess.")
                callback?.onSuccess()
            }

            override fun onFailure(code: Int, desc: String) {
                LOGGER.error("fetchLiveList onFailure. code:$code,desc:$desc")
                callback?.onFailure(code, desc)
            }
        })
    }

    fun getLiveList(): List<LiveInfo> = liveInfoList

    fun getLiveListDataCursor(): String = LiveListStore.shared().liveState.liveListCursor.value
}
