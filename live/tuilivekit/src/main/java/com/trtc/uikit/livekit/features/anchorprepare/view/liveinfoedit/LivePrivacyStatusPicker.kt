package com.trtc.uikit.livekit.features.anchorprepare.view.liveinfoedit

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.content.res.ResourcesCompat
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.features.anchorprepare.LiveStreamPrivacyStatus
import com.trtc.uikit.livekit.features.anchorprepare.store.AnchorPrepareStore
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover

class LivePrivacyStatusPicker(context: Context, private val store: AnchorPrepareStore) : AtomicPopover(context) {

    private lateinit var listView: ListView

    init {
        initView(context)
        initListItemClickListener()
    }

    private fun initView(context: Context) {
        listView = ListView(context)

        val dataList = getDataList(context)
        val adapter = ArrayAdapter(
            context,
            R.layout.anchor_prepare_layout_stream_privacy_status_pick_item,
            dataList
        )
        listView.adapter = adapter
        listView.divider = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.anchor_prepare_line_divider,
            null
        )
        listView.dividerHeight = 1
        setContent(listView)
    }

    private fun initListItemClickListener() {
        listView.setOnItemClickListener { _, _, position, _ ->
            store.setLiveMode(LiveStreamPrivacyStatus.values()[position])
            dismiss()
        }
    }

    private fun getDataList(context: Context): List<String> {
        return context.resources.getStringArray(R.array.common_stream_privacy_status).toList()
    }
}