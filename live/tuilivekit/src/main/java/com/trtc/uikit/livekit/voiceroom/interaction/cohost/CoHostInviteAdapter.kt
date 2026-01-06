package com.trtc.uikit.livekit.voiceroom.interaction.cohost

import android.content.Context
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import io.trtc.tuikit.atomicx.widget.basicwidget.avatar.AtomicAvatar
import io.trtc.tuikit.atomicx.widget.basicwidget.avatar.AtomicAvatar.AvatarContent
import io.trtc.tuikit.atomicx.widget.basicwidget.button.AtomicButton
import io.trtc.tuikit.atomicx.widget.basicwidget.button.ButtonColorType
import io.trtc.tuikit.atomicx.widget.basicwidget.button.ButtonVariant
import io.trtc.tuikit.atomicx.widget.basicwidget.toast.AtomicToast
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.CoHostLayoutTemplate
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore

class CoHostInviteAdapter(private val context: Context) :
    ListAdapter<LiveInfo, CoHostInviteAdapter.RecommendViewHolder>(DIFF_CALLBACK) {

    private val coHostStore: CoHostStore
    private val liveSeatStore: LiveSeatStore

    init {
        val liveID = LiveListStore.shared().liveState.currentLive.value.liveID
        coHostStore = CoHostStore.create(liveID)
        liveSeatStore = LiveSeatStore.create(liveID)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.livekit_voiceroom_co_host_invite_item, parent, false
        )
        return RecommendViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        val recommendUser = getItem(position) ?: return
        setUserName(holder, recommendUser)
        setAvatar(holder, recommendUser)
        updateInviteButtonStateAndAction(holder, recommendUser)
    }

    private fun setUserName(holder: RecommendViewHolder, recommendUser: LiveInfo) {
        holder.textName.text =
            recommendUser.liveOwner.userName.ifEmpty { recommendUser.liveOwner.userID }
    }

    private fun setAvatar(holder: RecommendViewHolder, recommendUser: LiveInfo) {
        holder.imageHead.apply {
            setContent(
                AvatarContent.URL(
                    url = recommendUser.liveOwner.avatarURL,
                    placeImage = R.drawable.livekit_ic_avatar
                )
            )
        }
    }

    private fun updateInviteButtonStateAndAction(
        holder: RecommendViewHolder,
        recommendUser: LiveInfo,
    ) {
        val inviteesList = coHostStore.coHostState.invitees.value
        val isThisUserInvited = inviteesList.any { it.liveID == recommendUser.liveID }

        holder.buttonConnect.isEnabled = true

        if (isThisUserInvited) {
            holder.buttonConnect.text = context.getString(R.string.seat_cancel_invite)
            holder.buttonConnect.variant = ButtonVariant.OUTLINED
            holder.buttonConnect.colorType = ButtonColorType.SECONDARY
        } else {
            holder.buttonConnect.text = context.getString(R.string.seat_request_host)
            holder.buttonConnect.variant = ButtonVariant.FILLED
            holder.buttonConnect.colorType = ButtonColorType.PRIMARY
        }

        holder.buttonConnect.setOnClickListener {
            val currentTime = SystemClock.elapsedRealtime()
            if (currentTime - holder.lastClickTime < DEBOUNCE_INTERVAL_MS) {
                return@setOnClickListener
            }
            holder.lastClickTime = currentTime
            val position = holder.adapterPosition
            if (position == RecyclerView.NO_POSITION) return@setOnClickListener

            val currentInvitees = coHostStore.coHostState.invitees.value
            val isCurrentlyInvited = currentInvitees.any { it.liveID == recommendUser.liveID }

            if (isCurrentlyInvited) {
                cancelInvitation(recommendUser, holder, position)
            } else {
                sendInvitation(recommendUser, holder, position)
            }
        }
    }

    private fun cancelInvitation(
        recommendUser: LiveInfo,
        holder: RecommendViewHolder,
        position: Int,
    ) {
        coHostStore.cancelHostConnection(recommendUser.liveID, object : CompletionHandler {
            override fun onSuccess() {}

            override fun onFailure(code: Int, desc: String) {
                ErrorLocalized.onError(code)
                holder.itemView.post { notifyItemChanged(position) }
            }
        })
    }

    private fun sendInvitation(
        recommendUser: LiveInfo,
        holder: RecommendViewHolder,
        position: Int,
    ) {
        if (coHostStore.coHostState.invitees.value.isNotEmpty()) {
            AtomicToast.show(context, context.getString(R.string.seat_repeat_invite_tips), AtomicToast.Style.WARNING)
            return
        }

        if (isBackSeatsOccupied()) {
            AtomicToast.show(context, context.getString(R.string.common_back_seats_occupied), AtomicToast.Style.WARNING)
            return
        }

        coHostStore.requestHostConnection(
            recommendUser.liveID,
            CoHostLayoutTemplate.HOST_VOICE_CONNECTION,
            CONNECTION_REQUEST_TIMEOUT,
            "",
            object : CompletionHandler {
                override fun onSuccess() {}

                override fun onFailure(code: Int, desc: String) {
                    ErrorLocalized.onError(code)
                    holder.itemView.post { notifyItemChanged(position) }
                }
            })
    }

    private fun isBackSeatsOccupied(): Boolean {
        val seatList = liveSeatStore.liveSeatState.seatList.value
        return seatList.any { it.index >= 6 && it.userInfo.userID.isNotEmpty() }
    }

    class RecommendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageHead: AtomicAvatar = itemView.findViewById(R.id.iv_head)
        var textName: TextView = itemView.findViewById(R.id.tv_name)
        var buttonConnect: AtomicButton = itemView.findViewById(R.id.atomic_btn_connect)
        var lastClickTime: Long = 0L
    }

    companion object {
        private const val DEBOUNCE_INTERVAL_MS = 500L
        const val CONNECTION_REQUEST_TIMEOUT = 10

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LiveInfo>() {
            override fun areItemsTheSame(oldItem: LiveInfo, newItem: LiveInfo): Boolean {
                return oldItem.liveID == newItem.liveID
            }

            override fun areContentsTheSame(oldItem: LiveInfo, newItem: LiveInfo): Boolean {
                return oldItem.liveOwner.userID == newItem.liveOwner.userID &&
                        oldItem.liveOwner.userName == newItem.liveOwner.userName &&
                        oldItem.liveOwner.avatarURL == newItem.liveOwner.avatarURL
            }
        }
    }
}