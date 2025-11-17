package com.trtc.uikit.livekit.features.anchorprepare.view.startlive

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.features.anchorprepare.manager.AnchorPrepareManager

class StartLiveButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var manager: AnchorPrepareManager? = null

    init {
        initView()
    }

    fun init(manager: AnchorPrepareManager) {
        this.manager = manager
    }

    private fun initView() {
        background = AppCompatResources.getDrawable(context, R.drawable.anchor_prepare_round_button_background)
        setText(R.string.common_start_live)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        setTextColor(resources.getColor(android.R.color.white))
        gravity = Gravity.CENTER
        isAllCaps = false
        setTypeface(null, Typeface.BOLD)

        setOnClickListener {
            manager?.startLive()
        }
    }
}