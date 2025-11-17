package com.trtc.uikit.livekit.voiceroom.interaction.connection

import android.content.Context
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.StandardToast
import com.trtc.uikit.livekit.common.ErrorLocalized
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
        if (TextUtils.isEmpty(recommendUser.liveOwner.avatarURL)) {
            holder.imageHead.setImageResource(R.drawable.livekit_ic_avatar)
        } else {
            ImageLoader.load(
                context,
                holder.imageHead,
                recommendUser.liveOwner.avatarURL,
                R.drawable.livekit_ic_avatar
            )
        }
    }

    private fun updateInviteButtonStateAndAction(
        holder: RecommendViewHolder,
        recommendUser: LiveInfo,
    ) {
        val inviteesList = coHostStore.coHostState.invitees.value
        val isThisUserInvited = inviteesList.any { it.liveID == recommendUser.liveID }

        holder.textConnect.isEnabled = true

        if (isThisUserInvited) {
            holder.textConnect.setText(R.string.seat_cancel_invite)
            holder.textConnect.setBackgroundResource(R.drawable.livekit_btn_grey_edge_bg)
        } else {
            holder.textConnect.setText(R.string.seat_request_host)
            holder.textConnect.setBackgroundResource(R.drawable.livekit_link_mic_accept_background)
        }

        holder.textConnect.setOnClickListener {
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
            StandardToast.toastShortMessage(context.getString(R.string.seat_repeat_invite_tips))
            return
        }
        coHostStore.requestHostConnection(
            recommendUser.liveID,
            CoHostLayoutTemplate.HOST_STATIC_VOICE_6V6,
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

    class RecommendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageHead: ImageFilterView = itemView.findViewById(R.id.iv_head)
        var textName: TextView = itemView.findViewById(R.id.tv_name)
        var textConnect: TextView = itemView.findViewById(R.id.tv_connect)
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