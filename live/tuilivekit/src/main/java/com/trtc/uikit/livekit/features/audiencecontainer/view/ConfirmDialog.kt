package com.trtc.uikit.livekit.features.audiencecontainer.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.trtc.uikit.livekit.R

class ConfirmDialog(context: Context) : Dialog(context, R.style.LiveKitConfirmDialogTheme) {

    private var contentText: String? = null
    private var positiveText: String? = null
    private var positiveClickListener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livekit_alert_confirm_dialog)
        setCancelable(false)
        initText()
        initButtonPositive()
        initButtonNegative()
    }

    private fun initText() {
        val message = findViewById<TextView>(R.id.content)
        message.text = contentText
    }

    private fun initButtonPositive() {
        val buttonPositive = findViewById<Button>(R.id.btn_positive)
        if (!TextUtils.isEmpty(positiveText)) {
            buttonPositive.text = positiveText
        }
        buttonPositive.setOnClickListener { v ->
            positiveClickListener?.onClick(v)
            dismiss()
        }
    }

    private fun initButtonNegative() {
        val buttonNegative = findViewById<Button>(R.id.btn_negative)
        buttonNegative.setOnClickListener { dismiss() }
    }

    fun setContent(content: String) {
        contentText = content
    }

    fun setPositiveText(text: String) {
        positiveText = text
    }

    fun setPositiveListener(listener: View.OnClickListener) {
        positiveClickListener = listener
    }
}
