package com.trtc.uikit.livekit.voiceroom.view.preview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.voiceroom.manager.VoiceRoomManager
import com.trtc.uikit.livekit.voiceroom.store.LayoutType
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LayoutSettingPanel(
    private val context: Context,
    private val voiceRoomManager: VoiceRoomManager
) : AtomicPopover(context) {

    private lateinit var layoutKTVRoom: LinearLayout
    private lateinit var layoutVoiceRoom: LinearLayout
    private var subscribeStateJob: Job? = null

    init {
        initView()
    }

    private fun initView() {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.livekit_voiceroom_anchor_preview_layout, null)
        initChatRoomView(view)
        initKTVView(view)
        setContent(view)
    }

    private fun initChatRoomView(view: View) {
        layoutVoiceRoom = view.findViewById(R.id.ll_voice_room)
        layoutVoiceRoom.setOnClickListener {
            voiceRoomManager.prepareStore.updateLayoutType(LayoutType.VOICE_ROOM)
            dismiss()
        }
    }

    private fun initKTVView(view: View) {
        layoutKTVRoom = view.findViewById(R.id.ll_ktv)
        layoutKTVRoom.setOnClickListener {
            voiceRoomManager.prepareStore.updateLayoutType(LayoutType.KTV_ROOM)
            dismiss()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }

    private fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            voiceRoomManager.prepareStore.prepareState.layoutType.collect {
                onVoiceRoomLayoutChanged(it)
            }
        }
    }

    private fun removeObserver() {
        subscribeStateJob?.cancel()
    }

    private fun onVoiceRoomLayoutChanged(layoutType: LayoutType) {
        if (LayoutType.VOICE_ROOM == layoutType) {
            layoutVoiceRoom.setBackgroundResource(R.drawable.livekit_settings_item_select_background)
            layoutKTVRoom.setBackgroundResource(R.drawable.livekit_settings_item_not_select_background)
        } else {
            layoutVoiceRoom.setBackgroundResource(R.drawable.livekit_settings_item_not_select_background)
            layoutKTVRoom.setBackgroundResource(R.drawable.livekit_settings_item_select_background)
        }
    }
}