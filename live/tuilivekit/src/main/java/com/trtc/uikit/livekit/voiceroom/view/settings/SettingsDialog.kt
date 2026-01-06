package com.trtc.uikit.livekit.voiceroom.view.settings

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover

class SettingsDialog(
    context: Context
) : AtomicPopover(context) {

    private val mContext: Context = context

    init {
        initView()
    }

    private fun initView() {
        val rootView = View.inflate(mContext, R.layout.livekit_voiceroom_settings_panel, null)
        val recycleSettingsList = rootView.findViewById<RecyclerView>(R.id.rv_settings_list)
        val adapter = SettingsListAdapter(mContext)
        recycleSettingsList.layoutManager = GridLayoutManager(mContext, adapter.itemCount)
        recycleSettingsList.addItemDecoration(SettingsListAdapter.SpaceItemDecoration(mContext))
        recycleSettingsList.adapter = adapter
        setContent(rootView)
    }
}