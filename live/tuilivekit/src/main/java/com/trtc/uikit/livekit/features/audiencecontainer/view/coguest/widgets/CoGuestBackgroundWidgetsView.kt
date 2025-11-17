package com.trtc.uikit.livekit.features.audiencecontainer.view.coguest.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager
import com.trtc.uikit.livekit.features.audiencecontainer.view.BasicView
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoGuestBackgroundWidgetsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BasicView(context, attrs, defStyleAttr) {

    companion object {
        private val LOGGER = LiveKitLogger.getFeaturesLogger("coGuest-BackgroundWidgetsView")
    }

    private var seatUserInfo: SeatUserInfo = SeatUserInfo()
    private lateinit var imageAvatar: ImageFilterView

    fun init(manager: AudienceManager, userInfo: SeatUserInfo) {
        LOGGER.info("init userId:" + userInfo.userID)
        seatUserInfo = userInfo
        super.init(manager)
    }

    override fun initView() {
        LayoutInflater.from(context)
            .inflate(R.layout.livekit_co_guest_background_widgets_view, this, true)
        imageAvatar = findViewById(R.id.iv_avatar)
    }

    override fun refreshView() {
        ImageLoader.load(context, imageAvatar, seatUserInfo.avatarURL, R.drawable.livekit_ic_avatar)
    }

    override fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.IO).launch {
            mediaState.isPictureInPictureMode.collect {
                onPictureInPictureObserver(it)
            }
        }
    }

    override fun removeObserver() {
        subscribeStateJob?.cancel()
    }

    private fun onPictureInPictureObserver(isPipMode: Boolean?) {
        visibility = if (isPipMode == true) {
            GONE
        } else {
            VISIBLE
        }
    }
}
