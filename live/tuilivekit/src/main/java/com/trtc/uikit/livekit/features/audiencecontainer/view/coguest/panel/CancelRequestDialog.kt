package com.trtc.uikit.livekit.features.audiencecontainer.view.coguest.panel

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.completionHandler
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager

class CancelRequestDialog(
    context: Context,
    private val audienceManager: AudienceManager
) : PopupDialog(context), AudienceManager.AudienceViewListener {

    init {
        initView()
    }

    @SuppressLint("InflateParams")
    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.livekit_dialog_co_guest_cancel, null)
        val textCancelCoGuest = view.findViewById<TextView>(R.id.tv_cancel_co_guest)
        val textDismiss = view.findViewById<TextView>(R.id.tv_dismiss)
        textCancelCoGuest.setOnClickListener { v ->
            if (!v.isEnabled) {
                return@setOnClickListener
            }
            v.isEnabled = false
            audienceManager.getCoGuestStore().cancelApplication(completionHandler {
                onSuccess {
                    audienceManager.getViewStore().updateTakeSeatState(false)
                }
                onError { code, _ ->
                    ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                }
            })
            dismiss()
        }

        textDismiss.setOnClickListener {
            dismiss()
        }

        setView(view)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        audienceManager.addAudienceViewListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        audienceManager.removeAudienceViewListener(this)
    }

    override fun onRoomDismissed(roomId: String) {
        dismiss()
    }
}
