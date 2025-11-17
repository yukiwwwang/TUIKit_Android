package com.trtc.uikit.livekit.features.endstatistics

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.endstatistics.manager.EndStatisticsManager
import com.trtc.uikit.livekit.features.endstatistics.state.EndStatisticsState
import java.util.Locale
import kotlin.math.max

class AnchorEndStatisticsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val logger = LiveKitLogger.getFeaturesLogger("AnchorEndStatisticsView")
    private val manager = EndStatisticsManager()
    private val state: EndStatisticsState = manager.getState()

    private val liveDurationObserver = Observer<Long> { onLiveDurationChange(it) }
    private val maxViewersCountObserver = Observer<Long> { onMaxViewersCountChange(it) }
    private val messageCountObserver = Observer<Long> { onMessageCountChange(it) }
    private val likeCountObserver = Observer<Long> { onLikeCountChange(it) }
    private val giftIncomeObserver = Observer<Long> { onGiftIncomeChange(it) }
    private val giftSenderCountObserver = Observer<Long> { onGiftSenderCountChange(it) }

    private lateinit var textDuration: TextView
    private lateinit var textViewersCount: TextView
    private lateinit var textMessageCount: TextView
    private lateinit var textGiftSenderCount: TextView
    private lateinit var textGiftIncome: TextView
    private lateinit var textLikeCount: TextView

    private var listener: EndStatisticsDefine.AnchorEndStatisticsViewListener? = null

    init {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.livekit_anchor_dashboard_view, this, true)
        textDuration = findViewById(R.id.tv_duration)
        textViewersCount = findViewById(R.id.tv_viewers)
        textMessageCount = findViewById(R.id.tv_message)
        textGiftIncome = findViewById(R.id.tv_gift_income)
        textGiftSenderCount = findViewById(R.id.tv_gift_people)
        textLikeCount = findViewById(R.id.tv_like)
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { onExitClick() }
    }

    fun init(info: EndStatisticsDefine.AnchorEndStatisticsInfo?) {
        if (info == null) {
            logger.error("init, info is null")
        } else {
            manager.setRoomId(info.roomId)
            manager.setLiveDuration(info.liveDurationMS)
            manager.setMaxViewersCount(max(0, info.maxViewersCount - 1))
            manager.setMessageCount(max(0, info.messageCount - 1))
            manager.setLikeCount(info.likeCount)
            manager.setGiftIncome(info.giftIncome)
            manager.setGiftSenderCount(info.giftSenderCount)
            logger.info("init, ${state}")
        }
    }

    fun setListener(listener: EndStatisticsDefine.AnchorEndStatisticsViewListener?) {
        this.listener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }

    private fun addObserver() {
        state.liveDurationMS.observeForever(liveDurationObserver)
        state.maxViewersCount.observeForever(maxViewersCountObserver)
        state.messageCount.observeForever(messageCountObserver)
        state.likeCount.observeForever(likeCountObserver)
        state.giftIncome.observeForever(giftIncomeObserver)
        state.giftSenderCount.observeForever(giftSenderCountObserver)
    }

    private fun removeObserver() {
        state.liveDurationMS.removeObserver(liveDurationObserver)
        state.maxViewersCount.removeObserver(maxViewersCountObserver)
        state.messageCount.removeObserver(messageCountObserver)
        state.likeCount.removeObserver(likeCountObserver)
        state.giftIncome.removeObserver(giftIncomeObserver)
        state.giftSenderCount.removeObserver(giftSenderCountObserver)
    }

    private fun onExitClick() {
        listener?.onCloseButtonClick()
    }

    private fun onLiveDurationChange(durationMS: Long) {
        val duration = (durationMS / 1000).toInt()
        val formatSeconds = manager.formatSeconds(duration)
        textDuration.text = formatSeconds
    }

    private fun onMaxViewersCountChange(count: Long) {
        val info = String.format(Locale.getDefault(), "%d", count)
        textViewersCount.text = info
    }

    private fun onMessageCountChange(count: Long) {
        val info = String.format(Locale.getDefault(), "%d", count)
        textMessageCount.text = info
    }

    private fun onLikeCountChange(count: Long) {
        val info = String.format(Locale.getDefault(), "%d", count)
        textLikeCount.text = info
    }

    private fun onGiftIncomeChange(count: Long) {
        val info = String.format(Locale.getDefault(), "%d", count)
        textGiftIncome.text = info
    }

    private fun onGiftSenderCountChange(count: Long) {
        val info = String.format(Locale.getDefault(), "%d", count)
        textGiftSenderCount.text = info
    }
}