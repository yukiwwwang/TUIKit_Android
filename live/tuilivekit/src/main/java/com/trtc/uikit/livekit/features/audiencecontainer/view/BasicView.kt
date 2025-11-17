package com.trtc.uikit.livekit.features.audiencecontainer.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager
import com.trtc.uikit.livekit.features.audiencecontainer.store.IMState
import com.trtc.uikit.livekit.features.audiencecontainer.store.IMStore
import com.trtc.uikit.livekit.features.audiencecontainer.store.MediaState
import com.trtc.uikit.livekit.features.audiencecontainer.store.MediaStore
import com.trtc.uikit.livekit.features.audiencecontainer.store.ViewState
import io.trtc.tuikit.atomicxcore.api.live.BattleStore
import io.trtc.tuikit.atomicxcore.api.live.CoGuestState
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.CoHostState
import io.trtc.tuikit.atomicxcore.api.live.LiveListState
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatState
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import kotlinx.coroutines.Job

abstract class BasicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    protected lateinit var audienceManager: AudienceManager
    protected lateinit var liveListStore: LiveListStore
    protected lateinit var liveSeatStore: LiveSeatStore
    protected lateinit var coGuestStore: CoGuestStore
    protected lateinit var battleStore: BattleStore
    protected lateinit var imStore: IMStore
    protected lateinit var mediaStore: MediaStore
    protected lateinit var liveListState: LiveListState
    protected lateinit var liveSeatState: LiveSeatState
    protected lateinit var coGuestState: CoGuestState
    protected lateinit var coHostState: CoHostState
    protected lateinit var imState: IMState
    protected lateinit var mediaState: MediaState
    protected lateinit var viewState: ViewState
    protected var subscribeStateJob: Job? = null
    private var isAddObserver = false

    init {
        initView()
    }

    open fun init(audienceManager: AudienceManager) {
        this.audienceManager = audienceManager
        liveListStore = audienceManager.getLiveListStore()
        liveListState = audienceManager.getLiveListState()
        liveSeatStore = audienceManager.getLiveSeatStore()
        battleStore = audienceManager.getBattleStore()
        liveSeatState = audienceManager.getLiveSeatState()
        coGuestStore = audienceManager.getCoGuestStore()
        coHostState = audienceManager.getCoHostState()
        imStore = audienceManager.getIMStore()
        mediaStore = audienceManager.getMediaStore()
        coGuestState = audienceManager.getCoGuestState()
        imState = audienceManager.getIMState()
        mediaState = audienceManager.getMediaState()
        viewState = audienceManager.getViewState()
        refreshView()
        if (!isAddObserver) {
            addObserver()
            isAddObserver = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!::audienceManager.isInitialized) {
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

    protected open fun refreshView() {
    }

    protected abstract fun addObserver()

    protected abstract fun removeObserver()
}
