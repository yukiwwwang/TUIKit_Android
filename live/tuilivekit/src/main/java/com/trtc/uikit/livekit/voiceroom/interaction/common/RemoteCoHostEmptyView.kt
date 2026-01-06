package com.trtc.uikit.livekit.voiceroom.interaction.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicxcore.api.live.SeatInfo

class RemoteCoHostEmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var imageSeat: ImageView
    private var seatInfo: SeatInfo? = null
    private lateinit var textLockStatus: TextView

    init {
        initView()
    }

    fun init(seatInfo: SeatInfo) {
        this.seatInfo = seatInfo
        initSeatView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.livekit_voiceroom_remote_co_host_empty_view, this, true)
        imageSeat = findViewById(R.id.iv_seat)
        textLockStatus = findViewById(R.id.tv_lock_status)
    }

    private fun initSeatView() {
        seatInfo?.let {
            val imageRes = if (it.isLocked) R.drawable.livekit_voiceroom_co_host_lock_seat
            else R.drawable.livekit_voiceroom_co_host_empty_seat
            imageSeat.setImageResource(imageRes)
            val textRes = if (it.isLocked)
                R.string.seat_locked else R.string.seat_no_guest
            textLockStatus.setText(textRes)
        }
    }
}