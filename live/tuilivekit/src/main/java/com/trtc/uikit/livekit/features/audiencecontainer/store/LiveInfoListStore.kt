package com.trtc.uikit.livekit.features.audiencecontainer.store

import android.text.TextUtils
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.audiencecontainer.AudienceContainerViewDefine
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo

class LiveInfoListStore(private val liveListDataSource: AudienceContainerViewDefine.LiveListDataSource) {

    companion object {
        private val LOGGER = LiveKitLogger.getComponentLogger("LiveInfoListStore")
    }

    private var cursor: String = ""
    private var firstLiveInfo: LiveInfo? = null
    private val liveInfoList: MutableList<LiveInfo> = ArrayList()

    fun setFirstData(liveInfo: LiveInfo) {
        firstLiveInfo = liveInfo
    }

    fun refreshLiveList(callback: AudienceContainerViewDefine.LiveListCallback) {
        fetchLiveList(true, callback)
    }

    fun fetchLiveList(callback: AudienceContainerViewDefine.LiveListCallback) {
        fetchLiveList(false, callback)
    }

    private fun fetchLiveList(
        isRefresh: Boolean,
        callback: AudienceContainerViewDefine.LiveListCallback
    ) {
        LOGGER.info("fetchLiveList start,isRefresh:$isRefresh")
        val param = AudienceContainerViewDefine.FetchLiveListParam()
        val currentCursor = if (isRefresh) "" else cursor
        param.cursor = currentCursor
        cursor = currentCursor
        liveListDataSource.fetchLiveList(
            param,
            object : AudienceContainerViewDefine.LiveListCallback {
                override fun onSuccess(cursor: String, liveInfoList: List<LiveInfo>) {
                    LOGGER.info("fetchLiveList onSuccess. result.liveInfoList.size:${liveInfoList.size}")
                    val list = ArrayList<LiveInfo>()
                    for (liveInfo in liveInfoList) {
                        if (firstLiveInfo != null) {
                            if (TextUtils.equals(liveInfo.liveID, firstLiveInfo!!.liveID)) {
                                continue
                            }
                        }
                        list.add(liveInfo)
                    }

                    if (isRefresh) {
                        this@LiveInfoListStore.liveInfoList.clear()
                    }
                    this@LiveInfoListStore.liveInfoList.addAll(list)
                    this@LiveInfoListStore.cursor = cursor
                    callback.onSuccess(cursor, list)
                }

                override fun onError(code: Int, message: String) {
                    LOGGER.error("fetchLiveList onError. code:$code,message:$message")
                    callback.onError(code, message)
                }
            })
    }

    fun getLiveList(): List<LiveInfo> {
        return liveInfoList
    }

    fun getLiveListDataCursor(): String {
        return cursor
    }
}
