package com.trtc.uikit.roomkit.view.main.roomview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.trtc.uikit.roomkit.R
import com.trtc.uikit.roomkit.base.extension.getDisplayName
import io.trtc.tuikit.atomicxcore.api.device.DeviceStatus
import io.trtc.tuikit.atomicxcore.api.room.ParticipantRole
import io.trtc.tuikit.atomicxcore.api.room.RoomParticipant

/**
 * Overlay view displaying participant name, role icon, and microphone status on video items.
 */
class RoomVideoNameOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivUserAvatar: ImageView by lazy { findViewById(R.id.iv_user_manager) }
    private val tvUserName: TextView by lazy { findViewById(R.id.tv_user_name) }
    private val ivMicStatus: ImageView by lazy { findViewById(R.id.iv_mic_status) }

    init {
        LayoutInflater.from(context).inflate(R.layout.roomkit_view_video_name_overlay, this)
    }

    fun updateParticipant(participant: RoomParticipant) {
        tvUserName.text = participant.getDisplayName()
        updateRoleIcon(participant.role)

        if (participant.microphoneStatus == DeviceStatus.ON) {
            ivMicStatus.setImageResource(R.drawable.roomkit_ic_microphone_on)
        } else {
            ivMicStatus.setImageResource(R.drawable.roomkit_ic_microphone_off)
        }
    }

    private fun updateRoleIcon(role: ParticipantRole) {
        when (role) {
            ParticipantRole.OWNER -> {
                ivUserAvatar.visibility = VISIBLE
                ivUserAvatar.setImageResource(R.drawable.roomkit_ic_video_seat_owner)
            }

            ParticipantRole.ADMIN -> {
                ivUserAvatar.visibility = VISIBLE
                ivUserAvatar.setImageResource(R.drawable.roomkit_ic_video_seat_manager)
            }

            ParticipantRole.GENERAL_USER -> {
                ivUserAvatar.visibility = GONE
            }
        }
    }
}