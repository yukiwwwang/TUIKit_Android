package com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import com.trtc.uikit.livekit.features.anchorboardcast.view.BasicView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CoGuestBackgroundWidgetsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BasicView(context, attrs, defStyleAttr) {
    private val logger = LiveKitLogger.getFeaturesLogger("coGuest-BackgroundWidgetsView")
    private var state = TUIRoomDefine.SeatFullInfo()
    private lateinit var imageAvatar: ImageFilterView
    private var subscribeStateJob: Job? = null

    fun init(manager: AnchorManager, userInfo: TUIRoomDefine.SeatFullInfo) {
        logger.info("init userId:" + userInfo.userId)
        state = userInfo
        super.init(manager)
    }

    override fun initView() {
        LayoutInflater.from(baseContext).inflate(R.layout.livekit_co_guest_background_widgets_view, this, true)
        imageAvatar = findViewById(R.id.iv_avatar)
    }

    override fun refreshView() {
        initUserAvatarView()
    }

    private fun initUserAvatarView() {
        ImageLoader.load(baseContext, imageAvatar, state.userAvatar, R.drawable.livekit_ic_avatar)
    }

    override fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            onPipModeObserver()
        }
    }

    override fun removeObserver() {
        subscribeStateJob?.cancel()
    }

    private suspend fun onPipModeObserver() {
        mediaState?.isPipModeEnabled?.collect {
            visibility = if (it) GONE else VISIBLE
        }
    }
}