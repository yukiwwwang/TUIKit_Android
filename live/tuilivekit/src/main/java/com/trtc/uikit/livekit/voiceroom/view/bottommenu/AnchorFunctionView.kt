package com.trtc.uikit.livekit.voiceroom.view.bottommenu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.PackageService
import com.trtc.uikit.livekit.voiceroom.interaction.common.CrossRoomInteractionDialog
import com.trtc.uikit.livekit.voiceroom.store.LayoutType
import com.trtc.uikit.livekit.voiceroom.view.basic.BasicView
import com.trtc.uikit.livekit.voiceroom.view.seatmanager.SeatManagerDialog
import com.trtc.uikit.livekit.voiceroom.view.settings.SettingsDialog
import io.trtc.tuikit.atomicx.karaoke.KaraokeControlView
import io.trtc.tuikit.atomicx.karaoke.KaraokeFloatingView
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore.Companion.create
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveUserInfo
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AnchorFunctionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BasicView(context, attrs, defStyleAttr) {

    private var seatApplicationCountText: TextView
    private var settingsDialog: SettingsDialog? = null
    private var seatManagerDialog: SeatManagerDialog? = null
    private lateinit var liveListStore: LiveListStore
    private var mCoHostStore: CoHostStore? = null
    private lateinit var coGuestStore: CoGuestStore
    private lateinit var mImageBattle: ImageView
    private var mImageKTV: ImageView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.livekit_voiceroom_anchor_function, this, true)
        initBattleIcon()
        initKTVView()
        seatApplicationCountText = findViewById(R.id.application_count)
        findViewById<View>(R.id.iv_settings).setOnClickListener { showSettingsPanel() }
        val imageKTV: ImageView = findViewById(R.id.iv_song_request)
        imageKTV.setVisibility(if (PackageService.isRTCubeOrTencentRTC) GONE else VISIBLE)
        imageKTV.setOnClickListener { showSongRequestPanel() }
        findViewById<View>(R.id.iv_seat_management).setOnClickListener { showSeatManagementPanel() }
    }

    override fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                coGuestStore.coGuestState.applicants.collect {
                    updateSeatApplicationCountText(it)
                }
            }
        }
        addConnectionObserver()
    }

    fun addConnectionObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            mCoHostStore?.coHostState?.connected?.collect {
                onConnectedListChanged(it)
            }
        }
    }

    fun onConnectedListChanged(connectedRoomList: List<SeatUserInfo>) {
        val currentLiveId = liveID
        if (currentLiveId.isEmpty()) return
        val isConnected = connectedRoomList.any { it.liveID == currentLiveId }
        if (isConnected) {
            mImageBattle.setImageResource(R.drawable.livekit_voiceroom_connected_icon)
            if (!PackageService.isRTCubeOrTencentRTC) mImageKTV?.visibility = GONE
        } else {
            mImageBattle.setImageResource(R.drawable.livekit_function_voice_room_pk)
            if (!PackageService.isRTCubeOrTencentRTC) mImageKTV?.visibility = VISIBLE
        }

    }

    private fun initKTVView() {
        mImageKTV = findViewById(R.id.iv_song_request)
        mImageKTV?.setVisibility(if (PackageService.isRTCubeOrTencentRTC) GONE else VISIBLE)
        mImageKTV?.setOnClickListener { showSongRequestPanel() }
    }


    override fun removeObserver() {
        subscribeStateJob?.cancel()
    }

    override fun initStore() {
        liveListStore = LiveListStore.shared()
        coGuestStore = CoGuestStore.create(liveID)
        mCoHostStore = create(liveID)
    }

    private fun initBattleIcon() {
        mImageBattle = findViewById(R.id.iv_pk)
        mImageBattle.setOnClickListener { showPKPanel() }
    }

    private fun showPKPanel() {
        val crossRoomInteractionDialog = CrossRoomInteractionDialog(context)
        crossRoomInteractionDialog.show()
    }

    private fun showSettingsPanel() {
        if (voiceRoomManager == null) return
        settingsDialog = settingsDialog ?: SettingsDialog(context)
        settingsDialog?.show()
    }

    private fun showSongRequestPanel() {
        when (voiceRoomManager?.prepareStore?.prepareState?.layoutType?.value) {
            LayoutType.KTV_ROOM -> KaraokeControlView(context).apply {
                init(
                    liveID,
                    TUIRoomEngine.getSelfInfo().userId == liveListStore.liveState.currentLive.value.liveOwner.userID
                )
                showSongRequestPanel()
            }

            else -> if (!PackageService.isRTCubeOrTencentRTC) {
                KaraokeFloatingView(context).apply {
                    init(
                        liveID,
                        TUIRoomEngine.getSelfInfo().userId == liveListStore.liveState.currentLive.value.liveOwner.userID
                    )
                    showSongRequestPanel()
                }
            }
        }
    }

    private fun showSeatManagementPanel() {
        if (seatManagerDialog == null) {
            seatManagerDialog = SeatManagerDialog(context)
        }
        seatManagerDialog?.show()
    }

    private fun updateSeatApplicationCountText(list: List<LiveUserInfo>) {
        seatApplicationCountText.apply {
            isVisible = list.isNotEmpty()
            text = list.size.toString()
        }
    }
}
