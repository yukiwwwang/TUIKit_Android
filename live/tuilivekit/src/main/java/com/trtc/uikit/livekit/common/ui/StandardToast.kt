package com.trtc.uikit.livekit.common.ui

import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.R

object StandardToast {

    fun toastShortMessage(tips: String) {
        showToast(tips, Toast.LENGTH_SHORT)
    }

    fun toastLongMessage(tips: String) {
        showToast(tips, Toast.LENGTH_LONG)
    }

    private fun showToast(message: String, duration: Int) {
        val context = ContextProvider.getApplicationContext()
        val view = LayoutInflater.from(context).inflate(R.layout.livekit_standard_toast, null, false)
        val text = view.findViewById<TextView>(R.id.tv_describe)
        text.text = message

        val toast = Toast(view.context).apply {
            this.duration = duration
            setView(view)
        }
        toast.show()
    }
}