package com.trtc.uikit.livekit.voiceroom.interaction.battle

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
import io.trtc.tuikit.atomicx.widget.basicwidget.toast.AtomicToast
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.CoHostLayoutTemplate
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import io.trtc.tuikit.atomicx.widget.basicwidget.button.ButtonVariant
import io.trtc.tuikit.atomicx.widget.basicwidget.button.ButtonColorType

class BattleInviteAdapter(private val context: Context) :
    ListAdapter<LiveInfo, BattleInviteAdapter.RecommendViewHolder>(DIFF_CALLBACK) {

    private val coHostStore: CoHostStore
    private val liveSeatStore: LiveSeatStore

    init {
        val liveID = LiveListStore.shared().liveState.currentLive.value.liveID
        coHostStore = CoHostStore.create(liveID)
        liveSeatStore = LiveSeatStore.create(liveID)
    }

    fun interface OnInviteButtonClickListener {
        fun onInviteClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.livekit_voiceroom_battle_participant_item, parent, false
        )
        return RecommendViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        val recommendUser = getItem(position) ?: return
        holder.bind(recommendUser)
    }

    inner class RecommendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageHead: AtomicAvatar = itemView.findViewById(R.id.iv_head)
        private val textName: TextView = itemView.findViewById(R.id.tv_name)
        private val buttonBattle: AtomicButton = itemView.findViewById(R.id.tv_battle)

        private var lastClickTime: Long = 0L

        fun bind(recommendUser: LiveInfo) {
            setUserName(recommendUser)
            setAvatar(recommendUser)
            updateInviteButtonStateAndAction(recommendUser)
        }

        private fun setUserName(recommendUser: LiveInfo) {
            textName.text = recommendUser.liveOwner.userName.takeIf { it.isNotEmpty() }
                ?: recommendUser.liveOwner.userID
        }

        private fun setAvatar(recommendUser: LiveInfo) {
            imageHead.setContent(
                AvatarContent.URL(
                    recommendUser.liveOwner.avatarURL,
                    R.drawable.livekit_ic_avatar
                )
            )
        }

        private fun updateInviteButtonStateAndAction(recommendUser: LiveInfo) {
            val inviteesList = coHostStore.coHostState.invitees.value
            val isThisUserInvited = inviteesList.any { it.liveID == recommendUser.liveID }

            buttonBattle.isEnabled = true

            if (isThisUserInvited) {
                buttonBattle.text = context.getString(R.string.seat_cancel_invite)
                buttonBattle.variant = ButtonVariant.OUTLINED
                buttonBattle.colorType = ButtonColorType.SECONDARY
            } else {
                buttonBattle.text = context.getString(R.string.seat_invite_battle)
                buttonBattle.variant = ButtonVariant.FILLED
                buttonBattle.colorType = ButtonColorType.PRIMARY
            }

            buttonBattle.setOnClickListener {
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastClickTime < DEBOUNCE_INTERVAL_MS) {
                    return@setOnClickListener
                }
                lastClickTime = currentTime
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val currentInvitees = coHostStore.coHostState.invitees.value
                val isCurrentlyInvited = currentInvitees.any { it.liveID == recommendUser.liveID }

                if (isCurrentlyInvited) {
                    cancelInvitation(recommendUser)
                } else {
                    sendInvitation(recommendUser, currentInvitees.isNotEmpty())
                }
            }
        }

        private fun cancelInvitation(recommendUser: LiveInfo) {
            coHostStore.cancelHostConnection(recommendUser.liveID, object : CompletionHandler {
                override fun onSuccess() {}

                override fun onFailure(code: Int, desc: String) {
                    ErrorLocalized.onError(code)
                }
            })
        }

        private fun sendInvitation(recommendUser: LiveInfo, isAnyoneElseInvited: Boolean) {
            if (isAnyoneElseInvited) {
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
                EXTENSION_INFO,
                object : CompletionHandler {
                    override fun onSuccess() {}

                    override fun onFailure(code: Int, desc: String) {
                        ErrorLocalized.onError(code)
                        itemView.post { notifyItemChanged(adapterPosition) }
                    }
                })
        }

        private fun isBackSeatsOccupied(): Boolean {
            val seatList = liveSeatStore.liveSeatState.seatList.value
            return seatList.any { it.index >= 6 && it.userInfo.userID.isNotEmpty() }
        }
    }

    companion object {
        private const val DEBOUNCE_INTERVAL_MS = 500L
        const val EXTENSION_INFO = "needRequestBattle"
        const val CONNECTION_REQUEST_TIMEOUT = 10

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LiveInfo>() {
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