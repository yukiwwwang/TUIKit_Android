package com.trtc.uikit.livekit.voiceroom.interaction.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.voiceroom.interaction.battle.BattleInviteAdapter
import com.trtc.uikit.livekit.voiceroom.view.seatmanager.SeatInvitationDialog
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import io.trtc.tuikit.atomicxcore.api.live.SeatInfo

class LocalCoHostViewManagerPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var textLock: TextView
    private lateinit var imageLock: ImageView
    private lateinit var imageInvite: ImageView
    private lateinit var layoutInvite: LinearLayout
    private lateinit var layoutLock: LinearLayout

    private var listener: BattleInviteAdapter.OnInviteButtonClickListener? = null
    private var seatInfo: SeatInfo? = null
    private var seatInvitationDialog: SeatInvitationDialog? = null
    private var liveSeatStore: LiveSeatStore? = null

    init {
        initView(context)
    }

    fun init(seatInfo: SeatInfo) {
        this.seatInfo = seatInfo
        this.liveSeatStore = LiveListStore.shared().liveState.currentLive.value?.liveID?.let {
            LiveSeatStore.create(it)
        }
        initLockView()
        initInviteView()
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.livekit_voiceroom_local_co_host_view_manager_panel, this, true)
        bindViewId()
    }

    private fun bindViewId() {
        imageLock = findViewById(R.id.iv_lock)
        textLock = findViewById(R.id.tv_lock)
        imageInvite = findViewById(R.id.iv_invite)
        layoutInvite = findViewById(R.id.ll_invite)
        layoutLock = findViewById(R.id.ll_lock)
    }

    fun setOnInviteButtonClickListener(listener: BattleInviteAdapter.OnInviteButtonClickListener) {
        this.listener = listener
    }

    private fun initLockView() {
        val currentSeatInfo = seatInfo ?: return
        layoutLock.setOnClickListener {
            if (currentSeatInfo.isLocked) {
                liveSeatStore?.unlockSeat(currentSeatInfo.index, object : CompletionHandler {
                    override fun onSuccess() {
                        imageLock.setImageResource(R.drawable.livekit_ic_lock)
                        textLock.setText(R.string.common_voiceroom_lock)
                    }

                    override fun onFailure(code: Int, desc: String) {
                        ErrorLocalized.onError(code)
                    }
                })
            } else {
                liveSeatStore?.lockSeat(currentSeatInfo.index, object : CompletionHandler {
                    override fun onSuccess() {
                        imageLock.setImageResource(R.drawable.livekit_ic_unlock)
                        textLock.setText(R.string.common_voiceroom_unlock)
                    }

                    override fun onFailure(code: Int, desc: String) {
                        ErrorLocalized.onError(code)
                    }
                })
            }
            listener?.onInviteClicked()
        }
        if (currentSeatInfo.isLocked) {
            imageLock.setImageResource(R.drawable.livekit_ic_unlock)
            textLock.setText(R.string.common_voiceroom_unlock)
        } else {
            imageLock.setImageResource(R.drawable.livekit_ic_lock)
            textLock.setText(R.string.common_voiceroom_lock)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        removeObserver()
        addObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }

    private fun addObserver() {
    }

    private fun removeObserver() {
    }

    private fun initInviteView() {
        val currentSeatInfo = seatInfo ?: return
        if (currentSeatInfo.isLocked) {
            layoutInvite.visibility = GONE
        } else {
            layoutInvite.visibility = VISIBLE
        }
        imageInvite.setOnClickListener {
            if (seatInvitationDialog == null) {
                seatInvitationDialog = SeatInvitationDialog(context)
                seatInvitationDialog?.setTargetIndex(seatInfo?.index)
            }
            listener?.onInviteClicked()
            seatInvitationDialog?.show()
        }
    }
}