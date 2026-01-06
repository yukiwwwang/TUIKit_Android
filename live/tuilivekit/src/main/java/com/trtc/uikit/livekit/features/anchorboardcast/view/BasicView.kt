package com.trtc.uikit.livekit.features.anchorboardcast.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.trtc.uikit.livekit.features.anchorboardcast.store.AnchorBattleState
import com.trtc.uikit.livekit.features.anchorboardcast.store.AnchorBattleStore
import com.trtc.uikit.livekit.features.anchorboardcast.store.AnchorCoHostState
import com.trtc.uikit.livekit.features.anchorboardcast.store.AnchorCoHostStore
import com.trtc.uikit.livekit.features.anchorboardcast.store.AnchorState
import com.trtc.uikit.livekit.features.anchorboardcast.store.AnchorStore
import com.trtc.uikit.livekit.features.anchorboardcast.store.MediaState
import com.trtc.uikit.livekit.features.anchorboardcast.store.MediaStore
import com.trtc.uikit.livekit.features.anchorboardcast.store.UserState
import com.trtc.uikit.livekit.features.anchorboardcast.store.UserStore

abstract class BasicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    protected val baseContext: Context = context
    protected var anchorState: AnchorState? = null
    protected var coHostState: AnchorCoHostState? = null
    protected var battleState: AnchorBattleState? = null
    protected var userState: UserState? = null
    protected var mediaState: MediaState? = null
    protected var anchorStore: AnchorStore? = null
    protected var anchorCoHostStore: AnchorCoHostStore? = null
    protected var anchorBattleStore: AnchorBattleStore? = null
    protected var userStore: UserStore? = null
    protected var mediaStore: MediaStore? = null
    private var isAddObserver = false

    init {
        initView()
    }

    fun init(anchorStore: AnchorStore) {
        this@BasicView.anchorStore = anchorStore
        userStore = anchorStore.getUserStore()
        mediaStore = anchorStore.getMediaStore()
        anchorCoHostStore = anchorStore.getAnchorCoHostStore()
        anchorBattleStore = anchorStore.getAnchorBattleStore()
        anchorState = anchorStore.getState()
        userState = anchorStore.getUserState()
        mediaState = anchorStore.getMediaState()
        coHostState = anchorStore.getCoHostState()
        battleState = anchorStore.getBattleState()

        refreshView()
        if (!isAddObserver) {
            addObserver()
            isAddObserver = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (anchorStore == null) {
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