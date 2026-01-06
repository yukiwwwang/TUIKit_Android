package com.trtc.uikit.livekit.voiceroom.view.preview

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.content.res.ResourcesCompat
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.voiceroom.manager.VoiceRoomManager
import com.trtc.uikit.livekit.voiceroom.store.LiveStreamPrivacyStatus
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover

class StreamPrivacyStatusPicker(
    context: Context,
    private val voiceRoomManager: VoiceRoomManager
) : AtomicPopover(context) {

    private lateinit var listView: ListView

    init {
        initView(context)
        initListItemClickListener()
    }

    private fun initView(context: Context) {
        listView = ListView(context)

        val dataList = getDataList(context)
        val adapter = ArrayAdapter(context, R.layout.livekit_layout_bottom_list_item, dataList)
        listView.adapter = adapter
        listView.divider = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.livekit_line_divider,
            null
        )
        listView.dividerHeight = 1
        setContent(listView)
    }

    private fun initListItemClickListener() {
        listView.setOnItemClickListener { _, _, position, _ ->
            voiceRoomManager.prepareStore
                .updateLiveMode(LiveStreamPrivacyStatus.values()[position])
            dismiss()
        }
    }

    private fun getDataList(context: Context): List<String> {
        return context.resources.getStringArray(R.array.common_stream_privacy_status).toList()
    }
}