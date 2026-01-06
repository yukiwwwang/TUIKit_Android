package com.trtc.uikit.livekit.component.networkInfo.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover

class AudioModePanel(context: Context) : AtomicPopover(context) {

    private lateinit var textDefault: TextView
    private lateinit var textSpeech: TextView
    private lateinit var textMusic: TextView
    private lateinit var textCancel: TextView
    private var audioModeListener: NetworkInfoPanel.OnAudioModeListener? = null

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.network_info_audio_mode_panel, null)
        bindViewId(view)
        setClickListener()
        setContent(view)
    }

    fun setAudioModeListener(audioModeListener: NetworkInfoPanel.OnAudioModeListener) {
        this@AudioModePanel.audioModeListener = audioModeListener
    }

    private fun bindViewId(view: View) {
        textDefault = view.findViewById(R.id.tv_default)
        textSpeech = view.findViewById(R.id.tv_speech)
        textMusic = view.findViewById(R.id.tv_music)
        textCancel = view.findViewById(R.id.tv_cancel)
    }

    private fun setClickListener() {
        val listener = View.OnClickListener { v ->
            when (v.id) {
                R.id.tv_default -> audioModeListener?.onAudioModeChecked(TUIRoomDefine.AudioQuality.DEFAULT)
                R.id.tv_speech -> audioModeListener?.onAudioModeChecked(TUIRoomDefine.AudioQuality.SPEECH)
                R.id.tv_music -> audioModeListener?.onAudioModeChecked(TUIRoomDefine.AudioQuality.MUSIC)
            }
            hide()
        }
        
        textDefault.setOnClickListener(listener)
        textSpeech.setOnClickListener(listener)
        textMusic.setOnClickListener(listener)
        textCancel.setOnClickListener(listener)
    }
}