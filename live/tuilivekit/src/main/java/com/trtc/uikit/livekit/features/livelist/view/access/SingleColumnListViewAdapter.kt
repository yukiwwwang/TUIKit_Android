package com.trtc.uikit.livekit.features.livelist.view.access

import android.content.Context
import android.view.View
import com.trtc.uikit.livekit.features.livelist.LiveListViewAdapter
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo

class SingleColumnListViewAdapter(private val context: Context) : LiveListViewAdapter {

    override fun createLiveInfoView(liveInfo: LiveInfo): View {
        val widgetView = SingleColumnWidgetView(context)
        widgetView.init(liveInfo)
        return widgetView
    }

    override fun updateLiveInfoView(view: View, liveInfo: LiveInfo) {
        val widgetView = view as SingleColumnWidgetView
        widgetView.updateLiveInfoView(liveInfo)
    }
}
