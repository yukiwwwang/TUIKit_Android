package com.trtc.uikit.livekit.voiceroom.interaction.common

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.voiceroom.manager.VoiceRoomManager
import com.trtc.uikit.livekit.voiceroom.view.seatmanager.SeatActionSheetDialog
import com.trtc.uikit.livekit.voiceroom.view.seatmanager.SeatActionSheetGenerator
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.SeatInfo

class LocalCoHostEmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var imageSeat: ImageView
    private lateinit var layoutRoot: LinearLayout
    private lateinit var seatInfo: SeatInfo
    private lateinit var textLockStatus: TextView
    private lateinit var liveListStore: LiveListStore
    private lateinit var seatActionSheetGenerator: SeatActionSheetGenerator
    private var seatActionSheetDialog: SeatActionSheetDialog? = null

    init {
        inflate(context, R.layout.livekit_voiceroom_local_co_host_empty_view, this)
        bindViewId()
    }

    fun init(seatInfo: SeatInfo, voiceRoomManager: VoiceRoomManager?) {
        this.seatInfo = seatInfo
        this.liveListStore = LiveListStore.shared()
        this.seatActionSheetGenerator = SeatActionSheetGenerator(context, voiceRoomManager)
        initLayoutView()
        initSeatView()
    }

    private fun bindViewId() {
        imageSeat = findViewById(R.id.iv_seat)
        layoutRoot = findViewById(R.id.ll_root)
        textLockStatus = findViewById(R.id.tv_lock_status)
    }

    private fun initSeatView() {
        val imageRes = if (seatInfo.isLocked)
            R.drawable.livekit_voiceroom_co_host_lock_seat else R.drawable.livekit_voiceroom_invite_audience
        imageSeat.setImageResource(imageRes)
        val textRes = if (seatInfo.isLocked)
            R.string.seat_locked else R.string.seat_request_host
        textLockStatus.setText(textRes)
    }

    private fun initLayoutView() {
        layoutRoot.setOnClickListener {
            val isOwner =
                liveListStore.liveState.currentLive.value.liveOwner.userID == TUIRoomEngine.getSelfInfo().userId
            if (isOwner) {
                val seatManagementPanel = LocalCoHostViewManagerPanel(context).apply {
                    init(seatInfo)
                }
                AtomicPopover(context).apply {
                    setContent(seatManagementPanel)
                    seatManagementPanel.setOnInviteButtonClickListener(::hide)
                }.show()
            } else {
                val menuInfoList = seatActionSheetGenerator.generate(convertToTUISeatInfo(seatInfo))
                if (menuInfoList.isEmpty()) {
                    return@setOnClickListener
                }
                if (seatActionSheetDialog == null) {
                    seatActionSheetDialog = SeatActionSheetDialog(context)
                }
                seatActionSheetDialog?.updateActionButton(menuInfoList)
                seatActionSheetDialog?.show()
            }
        }
    }

    companion object {
        fun convertToTUISeatInfo(customSeatInfo: SeatInfo): TUIRoomDefine.SeatInfo {
            return TUIRoomDefine.SeatInfo().apply {
                index = customSeatInfo.index
                isLocked = customSeatInfo.isLocked
                customSeatInfo.userInfo?.let {
                    userId = it.userID
                    userName = it.userName
                    nameCard = it.userName
                    avatarUrl = it.avatarURL
                    roomId = it.liveID
                    isVideoLocked = !it.allowOpenCamera
                    isAudioLocked = !it.allowOpenMicrophone
                }
            }
        }
    }
}