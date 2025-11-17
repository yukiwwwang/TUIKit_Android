package com.trtc.uikit.livekit.features.anchorboardcast.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.BattleManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.CoHostManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.MediaManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.UserManager
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorState
import com.trtc.uikit.livekit.features.anchorboardcast.state.BattleState
import com.trtc.uikit.livekit.features.anchorboardcast.state.CoHostState
import com.trtc.uikit.livekit.features.anchorboardcast.state.MediaState
import com.trtc.uikit.livekit.features.anchorboardcast.state.UserState

abstract class BasicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    protected val baseContext: Context = context
    protected var anchorState: AnchorState? = null
    protected var coHostState: CoHostState? = null
    protected var battleState: BattleState? = null
    protected var userState: UserState? = null
    protected var mediaState: MediaState? = null
    protected var anchorManager: AnchorManager? = null
    protected var coHostManager: CoHostManager? = null
    protected var battleManager: BattleManager? = null
    protected var userManager: UserManager? = null
    protected var mediaManager: MediaManager? = null
    private var isAddObserver = false

    init {
        initView()
    }

    fun init(liveStreamManager: AnchorManager) {
        anchorManager = liveStreamManager
        userManager = liveStreamManager.getUserManager()
        mediaManager = liveStreamManager.getMediaManager()
        coHostManager = liveStreamManager.getCoHostManager()
        battleManager = liveStreamManager.getBattleManager()
        anchorState = liveStreamManager.getState()
        userState = liveStreamManager.getUserState()
        mediaState = liveStreamManager.getMediaState()
        coHostState = liveStreamManager.getCoHostState()
        battleState = liveStreamManager.getBattleState()

        refreshView()
        if (!isAddObserver) {
            addObserver()
            isAddObserver = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (anchorManager == null) {
            return
        }
        if (!isAddObserver) {
            addObserver()
            isAddObserver = true
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isAddObserver) {
            removeObserver()
            isAddObserver = false
        }
    }

    protected abstract fun initView()
    protected abstract fun refreshView()
    protected abstract fun addObserver()
    protected abstract fun removeObserver()
}