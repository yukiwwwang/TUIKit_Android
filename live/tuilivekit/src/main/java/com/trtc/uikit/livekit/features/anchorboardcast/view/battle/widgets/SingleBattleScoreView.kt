package com.trtc.uikit.livekit.features.anchorboardcast.view.battle.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.tencent.qcloud.tuicore.util.ScreenUtil
import com.trtc.uikit.livekit.R
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class SingleBattleScoreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val mLineDivider: View
    private val mImageDivider: View
    private val mTextScoreLeft: TextView
    private val mTextScoreRight: TextView

    init {
        inflate(context, R.layout.livekit_battle_single_battle_score_view, this)
        mLineDivider = findViewById(R.id.v_divider)
        mImageDivider = findViewById(R.id.iv_divider)
        mTextScoreLeft = findViewById(R.id.tv_score_left)
        mTextScoreRight = findViewById(R.id.tv_score_right)
        mTextScoreLeft.text = "0"
        mTextScoreRight.text = "0"
    }

    fun updateScores(scoreLeft: Int, scoreRight: Int) {
        if (scoreLeft + scoreRight < 0 || width == 0) {
            return
        }
        
        mTextScoreLeft.text = scoreLeft.toString()
        mTextScoreRight.text = scoreRight.toString()
        
        val textWidthLeft = mTextScoreLeft.paint.measureText(mTextScoreLeft.text.toString()) +
                2 * mTextScoreLeft.paddingLeft
        val textWidthRight = mTextScoreRight.paint.measureText(mTextScoreRight.text.toString()) +
                2 * mTextScoreRight.paddingLeft
        
        val width = width
        val ratio = if (scoreLeft + scoreRight == 0) 0.5f else 1.0f * scoreLeft / (scoreLeft + scoreRight)
        var dividerX = (width * ratio).toInt()
        dividerX = max(textWidthLeft, dividerX.toFloat()).toInt()
        dividerX = min(width - textWidthRight, dividerX.toFloat()).toInt()
        updateDivider(dividerX)
    }

    private fun updateDivider(dividerX: Int) {
        val vDividerParams = mLineDivider.layoutParams as RelativeLayout.LayoutParams
        vDividerParams.removeRule(RelativeLayout.CENTER_HORIZONTAL)
        vDividerParams.leftMargin = dividerX
        mLineDivider.layoutParams = vDividerParams

        val imageDividerParams = mImageDivider.layoutParams as RelativeLayout.LayoutParams
        imageDividerParams.removeRule(RelativeLayout.CENTER_HORIZONTAL)
        imageDividerParams.leftMargin = dividerX - imageDividerParams.width / 2 - ScreenUtil.dip2px(1f)
        mImageDivider.layoutParams = imageDividerParams

        invalidate()
    }
}