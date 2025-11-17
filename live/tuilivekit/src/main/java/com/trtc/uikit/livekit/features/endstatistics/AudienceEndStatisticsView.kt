package com.trtc.uikit.livekit.features.endstatistics

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.lifecycle.Observer
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.endstatistics.manager.EndStatisticsManager
import com.trtc.uikit.livekit.features.endstatistics.state.EndStatisticsState

class AudienceEndStatisticsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val logger = LiveKitLogger.getFeaturesLogger("AudienceEndStatisticsView")
    private val manager = EndStatisticsManager()
    private val state: EndStatisticsState = manager.getState()

    private val ownerNameObserver = Observer<String> { onOwnerNameChange(it) }
    private val ownerAvatarUrlObserver = Observer<String> { onOwnerAvatarUrlChange(it) }

    private lateinit var textName: TextView
    private lateinit var imageHead: ImageFilterView

    private var listener: EndStatisticsDefine.AudienceEndStatisticsViewListener? = null

    init {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.livekit_audience_dashboard_view, this, true)
        textName = findViewById(R.id.tv_name)
        imageHead = findViewById(R.id.iv_head)
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { onExitClick() }
    }

    fun init(roomId: String?, ownerName: String?, ownerAvatarUrl: String?) {
        manager.setRoomId(if (TextUtils.isEmpty(roomId)) "" else roomId!!)
        manager.setOwnerName(if (TextUtils.isEmpty(ownerName)) "" else ownerName!!)
        manager.setOwnerAvatarUrl(if (TextUtils.isEmpty(ownerAvatarUrl)) "" else ownerAvatarUrl!!)
        logger.info("init, $state")
    }

    fun setListener(listener: EndStatisticsDefine.AudienceEndStatisticsViewListener?) {
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
        state.ownerName.observeForever(ownerNameObserver)
        state.ownerAvatarUrl.observeForever(ownerAvatarUrlObserver)
    }

    private fun removeObserver() {
        state.ownerName.removeObserver(ownerNameObserver)
        state.ownerAvatarUrl.removeObserver(ownerAvatarUrlObserver)
    }

    private fun onExitClick() {
        listener?.onCloseButtonClick()
    }

    private fun onOwnerNameChange(name: String) {
        textName.text = name
    }

    private fun onOwnerAvatarUrlChange(url: String) {
        val avatarUrl = if (TextUtils.isEmpty(url)) null else url
        ImageLoader.load(context, imageHead, avatarUrl, R.drawable.livekit_ic_avatar)
    }
}