package com.trtc.uikit.livekit.features.anchorboardcast.view.cohost.panel

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R

class StandardDialog(context: Context) : Dialog(context) {
    
    private var avatarUrl: String? = null
    private var content: String? = null
    private var positiveText: String? = null
    private var negativeText: String? = null
    private var positiveTextColor: Int = 0
    private var positiveClickListener: View.OnClickListener? = null
    private var negativeClickListener: View.OnClickListener? = null

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        positiveTextColor = context.resources.getColor(R.color.common_design_standard_b1)
    }

    fun setContent(content: String) {
        this.content = content
    }

    fun setAvatar(avatarUrl: String?) {
        this.avatarUrl = avatarUrl
    }

    fun setPositiveText(positiveText: String, listener: View.OnClickListener) {
        this.positiveText = positiveText
        this.positiveClickListener = listener
    }

    fun setNegativeText(negativeText: String, listener: View.OnClickListener) {
        this.negativeText = negativeText
        this.negativeClickListener = listener
    }

    fun setPositiveTextColor(color: Int) {
        this.positiveTextColor = color
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livekit_connection_dialog)
        setCancelable(false)

        initText()
        initAvatarUrl()
        initButtonPositive()
        initButtonNegative()
    }

    private fun initText() {
        val textContent = findViewById<TextView>(R.id.tv_content)
        if (content == null) {
            textContent.visibility = View.GONE
        } else {
            textContent.text = content
            textContent.visibility = View.VISIBLE
        }
    }

    private fun initAvatarUrl() {
        val imageAvatar = findViewById<ImageView>(R.id.iv_picture)
        if (TextUtils.isEmpty(avatarUrl)) {
            imageAvatar.visibility = View.GONE
        } else {
            ImageLoader.load(context, imageAvatar, avatarUrl, R.drawable.livekit_ic_avatar)
            imageAvatar.visibility = View.VISIBLE
        }
    }

    private fun initButtonPositive() {
        val buttonPositive = findViewById<Button>(com.trtc.tuikit.common.R.id.btn_positive)

        if (positiveClickListener == null) {
            buttonPositive.visibility = View.GONE
            return
        }
        if (!TextUtils.isEmpty(positiveText)) {
            buttonPositive.text = positiveText
        }
        buttonPositive.setTextColor(positiveTextColor)
        buttonPositive.setOnClickListener(positiveClickListener)
    }

    private fun initButtonNegative() {
        val buttonNegative = findViewById<Button>(com.trtc.tuikit.common.R.id.btn_negative)

        if (negativeClickListener == null) {
            buttonNegative.visibility = View.GONE
            return
        }
        if (!TextUtils.isEmpty(negativeText)) {
            buttonNegative.text = negativeText
        }
        buttonNegative.setOnClickListener(negativeClickListener)
    }
}