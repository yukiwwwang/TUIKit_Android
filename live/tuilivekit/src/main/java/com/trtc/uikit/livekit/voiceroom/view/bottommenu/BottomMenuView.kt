package com.trtc.uikit.livekit.voiceroom.view.bottommenu

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.completionHandler
import com.trtc.uikit.livekit.component.barrage.BarrageInputView
import com.trtc.uikit.livekit.voiceroom.manager.VoiceRoomManager
import com.trtc.uikit.livekit.voiceroom.view.basic.BasicView
import io.trtc.tuikit.atomicxcore.api.device.DeviceStatus
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class BottomMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BasicView(context, attrs, defStyleAttr), LifecycleOwner {
    private var barrageInputView: BarrageInputView
    private var microphoneContainer: View
    private var microphoneButton: ImageView

    private lateinit var liveListStore: LiveListStore
    private lateinit var deviceStore: DeviceStore
    private lateinit var liveSeatStore: LiveSeatStore
    private lateinit var coGuestStore: CoGuestStore

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val collectJobs = mutableListOf<Job>()
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        LayoutInflater.from(context)
            .inflate(R.layout.livekit_voiceroom_layout_bottom_menu, this, true)
        barrageInputView = findViewById(R.id.rl_barrage_button)
        microphoneContainer = findViewById(R.id.microphone_container)
        microphoneButton = findViewById(R.id.iv_microphone)
        microphoneButton.setOnClickListener { onMicrophoneButtonClick() }
    }

    override fun addObserver() {
        val job = lifecycleScope.launch {
            launch {
                liveSeatStore.liveSeatState.seatList
                    .map { seatList ->
                        seatList.find { it.userInfo.userID == TUIRoomEngine.getSelfInfo().userId }?.userInfo?.microphoneStatus
                    }.distinctUntilChanged()
                    .collect { microphoneStatus ->
                        if (microphoneStatus == null) return@collect
                        onSeatMutedStateChanged(microphoneStatus == DeviceStatus.OFF)
                    }
            }
            launch {
                coGuestStore.coGuestState.connected.collect { connected ->
                    onLinkStateChanged(connected)
                }
            }
        }
        collectJobs.add(job)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun removeObserver() {
        collectJobs.forEach { it.cancel() }
        collectJobs.clear()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun init(liveID: String, voiceRoomManager: VoiceRoomManager) {
        super.init(liveID, voiceRoomManager)
        val functionView =
            if (TextUtils.equals(
                    TUIRoomEngine.getSelfInfo().userId,
                    liveListStore.liveState.currentLive.value.liveOwner.userID
                )
            ) {
                AnchorFunctionView(context)
            } else {
                AudienceFunctionView(context)
            }.apply {
                init(liveID, voiceRoomManager)
            }

        findViewById<RelativeLayout>(R.id.function_container).apply {
            removeAllViews()
            addView(functionView, RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
        barrageInputView.init(liveID)
    }

    override fun initStore() {
        liveListStore = LiveListStore.shared()
        deviceStore = DeviceStore.shared()
        liveSeatStore = LiveSeatStore.create(liveID)
        coGuestStore = CoGuestStore.create(liveID)
    }

    private fun onLinkStateChanged(connected: List<SeatUserInfo>) {
        microphoneContainer.isVisible =
            connected.find { it.userID == TUIRoomEngine.getSelfInfo().userId } != null
    }

    private fun onMicrophoneButtonClick() {
        val seatInfo =
            liveSeatStore.liveSeatState.seatList.value.firstOrNull { it.userInfo.userID == TUIRoomEngine.getSelfInfo().userId }
        seatInfo?.let {
            if (it.userInfo.microphoneStatus == DeviceStatus.ON) {
                muteMicrophone()
            } else {
                unMuteMicrophone()
            }
        }
    }

    private fun muteMicrophone() {
        liveSeatStore.muteMicrophone()
    }

    private fun unMuteMicrophone() {
        liveSeatStore.unmuteMicrophone(completionHandler {
            onError { code, _ ->
                ErrorLocalized.onError(code)
            }
        })
    }

    private fun onSeatMutedStateChanged(isMicrophoneMuted: Boolean) {
        microphoneButton.setImageResource(
            if (isMicrophoneMuted) R.drawable.livekit_ic_mic_closed
            else R.drawable.livekit_ic_mic_opened
        )
    }
}
