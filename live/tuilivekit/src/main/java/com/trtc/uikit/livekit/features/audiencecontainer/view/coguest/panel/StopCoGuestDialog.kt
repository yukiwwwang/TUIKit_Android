package com.trtc.uikit.livekit.features.audiencecontainer.view.coguest.panel

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager

class StopCoGuestDialog(
    context: Context,
    private val audienceManager: AudienceManager
) : PopupDialog(context) {

    init {
        initView()
    }

    @SuppressLint("InflateParams")
    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.livekit_dialog_co_guest_stop, null)
        val textStopCoGuest = view.findViewById<TextView>(R.id.tv_stop_co_guest)
        val textDismiss = view.findViewById<TextView>(R.id.tv_dismiss)
        textStopCoGuest.setOnClickListener {
            audienceManager.getCoGuestStore().disconnect(null)
            audienceManager.getViewStore()
                .updateTakeSeatState(false)
            dismiss()
        }

        textDismiss.setOnClickListener {
            dismiss()
        }

        setView(view)
    }
}
