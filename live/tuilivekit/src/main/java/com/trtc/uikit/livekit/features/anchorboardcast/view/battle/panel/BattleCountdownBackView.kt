package com.trtc.uikit.livekit.features.anchorboardcast.view.battle.panel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class BattleCountdownBackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val viewPadding = 80
    private val circlePaint = Paint().apply {
        color = Color.WHITE
        alpha = 0xCC
        isAntiAlias = true
    }
    
    private val mArcPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }
    
    private val mRipplePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val mArcRect = RectF()
    private val mArcPath = Path()

    private var mCircleRadius = 0f
    private var mRotationAngle = 0

    private val mRippleCircles = mutableListOf<RippleCircle>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val size = min(width, height)
        
        if (mCircleRadius == 0f) {
            mCircleRadius = (size - viewPadding * 2) / 2f
            mArcRect.apply {
                left = (width - mCircleRadius * 2 - mArcPaint.strokeWidth / 2f) / 2f
                top = (height - mCircleRadius * 2 - mArcPaint.strokeWidth / 2f) / 2f
                right = left + mCircleRadius * 2
                bottom = top + mCircleRadius * 2
            }
        }
        
        drawCircle(canvas)
        draw2Arc(canvas)
        drawRipple(canvas)
        invalidate()
    }

    private fun drawCircle(canvas: Canvas) {
        val width = width
        val height = height
        canvas.drawCircle(width / 2f, height / 2f, mCircleRadius, circlePaint)
    }

    private fun draw2Arc(canvas: Canvas) {
        mRotationAngle++
        mRotationAngle %= 360
        mArcPath.apply {
            reset()
            addArc(mArcRect, 120f + mRotationAngle, 60f)
            addArc(mArcRect, -120f + mRotationAngle, 180f)
        }
        canvas.drawPath(mArcPath, mArcPaint)
    }

    private fun drawRipple(canvas: Canvas) {
        val width = width
        val height = height
        
        if (mRippleCircles.isEmpty()) {
            // Init with two circles
            var radius = mCircleRadius
            var alpha = genRippleCircleAlpha(radius)
            mRippleCircles.add(RippleCircle(radius, alpha))
            
            radius = mCircleRadius + viewPadding / 2f
            alpha = genRippleCircleAlpha(radius)
            mRippleCircles.add(RippleCircle(radius, alpha))
        }
        
        for (circle in mRippleCircles) {
            mRipplePaint.alpha = circle.alpha
            canvas.drawCircle(width / 2f, height / 2f, circle.radius, mRipplePaint)

            if (circle.radius >= width / 2f) {
                circle.radius = mCircleRadius
            } else {
                circle.radius += 1
            }
            circle.alpha = genRippleCircleAlpha(circle.radius)
        }
    }

    private fun genRippleCircleAlpha(radius: Float): Int {
        return ((1 - (radius - mCircleRadius) / viewPadding) * 0xFF).toInt()
    }

    private data class RippleCircle(
        var radius: Float,
        var alpha: Int
    )
}