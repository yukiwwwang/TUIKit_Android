package com.trtc.uikit.livekit.voiceroom.view.basic

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.trtc.uikit.livekit.voiceroom.manager.VoiceRoomManager
import com.trtc.uikit.livekit.voiceroomcore.SeatGridView
import kotlinx.coroutines.Job

abstract class BasicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    protected var subscribeStateJob: Job? = null
    protected var liveID: String = ""
    protected var voiceRoomManager: VoiceRoomManager? = null
    protected var seatGridView: SeatGridView? = null

    private var isAddObserver = false

    open fun init(liveID: String, voiceRoomManager: VoiceRoomManager, seatGridView: SeatGridView) {
        this.seatGridView = seatGridView
        init(liveID, voiceRoomManager)
    }

    open fun init(liveID: String, voiceRoomManager: VoiceRoomManager) {
        this.voiceRoomManager = voiceRoomManager
        this.liveID = liveID
        setupLifecycleIfNeeded()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupLifecycleIfNeeded()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isAddObserver) {
            removeObserver()
            isAddObserver = false
        }
    }

    protected abstract fun initStore()

    protected abstract fun addObserver()

    protected abstract fun removeObserver()

    private fun setupLifecycleIfNeeded() {
        if (liveID.isEmpty()) {
            return
        }
        initStore()
        if (!isAddObserver) {
            addObserver()
            isAddObserver = true
        }
    }
}