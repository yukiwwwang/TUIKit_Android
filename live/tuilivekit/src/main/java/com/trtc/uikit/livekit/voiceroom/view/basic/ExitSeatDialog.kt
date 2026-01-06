package com.trtc.uikit.livekit.voiceroom.view.basic

import android.content.Context
import android.view.View
import android.widget.TextView
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover

class ExitSeatDialog(
    context: Context,
    private val onConfirmListener: OnConfirmListener
) : AtomicPopover(context) {

    init {
        initView()
    }

    protected fun initView() {
        val view = View.inflate(context, R.layout.livekit_voiceroom_exit_seat_dialog, null)
        setContent(view)
        val textCancel = view.findViewById<TextView>(R.id.tv_cancel)
        textCancel.setOnClickListener { dismiss() }

        val textExitLive = view.findViewById<TextView>(R.id.tv_exit_room)
        textExitLive.setOnClickListener {
            dismiss()
            onConfirmListener.onExitRoom()
        }

        val textExitSeat = view.findViewById<TextView>(R.id.tv_exit_seat)
        textExitSeat.setOnClickListener {
            dismiss()
            onConfirmListener.onExitSeat()
        }
    }

    interface OnConfirmListener {
        fun onExitRoom()
        fun onExitSeat()
    }
}