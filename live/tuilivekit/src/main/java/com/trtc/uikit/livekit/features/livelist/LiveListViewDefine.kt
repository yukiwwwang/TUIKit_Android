package com.trtc.uikit.livekit.features.livelist

import android.view.View
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo

enum class Style {
    SINGLE_COLUMN,
    DOUBLE_COLUMN
}

data class FetchLiveListParam(
    var cursor: String
)

interface LiveListViewAdapter {
    fun createLiveInfoView(liveInfo: LiveInfo): View
    fun updateLiveInfoView(view: View, liveInfo: LiveInfo)
}

interface LiveListDataSource {
    fun fetchLiveList(param: FetchLiveListParam, callback: CompletionHandler?)
}

fun interface OnItemClickListener {
    fun onItemClick(view: View, liveInfo: LiveInfo)
}
