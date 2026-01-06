package com.trtc.uikit.livekit.voiceroom.view.preview

import android.content.Context
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.voiceroom.manager.VoiceRoomManager
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover
import io.trtc.tuikit.atomicxcore.api.live.TakeSeatMode

class SettingsDialog(
    context: Context,
    private val voiceRoomManager: VoiceRoomManager
) : AtomicPopover(context) {

    private var switchCompat: SwitchCompat

    init {
        val rootView = View.inflate(context, R.layout.livekit_voiceroom_preview_settings, null)
        setContent(rootView)
        switchCompat = rootView.findViewById(R.id.need_request)
        switchCompat.isChecked =
            voiceRoomManager.prepareStore.prepareState.liveInfo.value.seatMode == TakeSeatMode.APPLY
        switchCompat.setOnCheckedChangeListener { _, enable -> onSeatModeClicked(enable) }
    }

    private fun onSeatModeClicked(enable: Boolean) {
        val seatMode = if (enable) {
            TakeSeatMode.APPLY
        } else {
            TakeSeatMode.FREE
        }
        updateSeatMode(seatMode)
        voiceRoomManager.prepareStore.updateSeatMode(seatMode)
    }

    private fun updateSeatMode(seatMode: TakeSeatMode) {
        switchCompat.isChecked = seatMode == TakeSeatMode.APPLY
    }
}