package com.trtc.uikit.livekit.voiceroom.view.seatmanager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.LiveAudienceStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import io.trtc.tuikit.atomicxcore.api.live.LiveUserInfo
import java.util.concurrent.CopyOnWriteArrayList

class SeatInvitationAdapter(
    private val context: Context,
) : RecyclerView.Adapter<SeatInvitationAdapter.ViewHolder>() {

    private var onInviteButtonClickListener: OnInviteButtonClickListener? = null
    private val data = CopyOnWriteArrayList<LiveUserInfo>()
    private val liveListStore = LiveListStore.shared()
    private val liveSeatStore =
        LiveSeatStore.create(liveListStore.liveState.currentLive.value.liveID)
    private val coGuestStore = CoGuestStore.create(liveListStore.liveState.currentLive.value.liveID)

    init {
        initData()
    }

    private fun initData() {
        data.clear()
        val audienceList =
            LiveAudienceStore.create(LiveListStore.shared().liveState.currentLive.value.liveID)
                .liveAudienceState.audienceList.value
        val seatList = liveSeatStore.liveSeatState.seatList.value
        for (userInfo in audienceList) {
            if (!seatList.none { it.userInfo.userID == userInfo.userID }) {
                continue
            }
            data.add(userInfo)
        }
    }

    fun setOnInviteButtonClickListener(listener: OnInviteButtonClickListener) {
        onInviteButtonClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.livekit_voiceroom_item_invite_audience,
            parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userInfo = data[position]
        if (TextUtils.isEmpty(userInfo.avatarURL)) {
            holder.imageHead.setImageResource(R.drawable.livekit_ic_avatar)
        } else {
            ImageLoader.load(
                context,
                holder.imageHead,
                userInfo.avatarURL,
                R.drawable.livekit_ic_avatar
            )
        }

        if (TextUtils.isEmpty(userInfo.userName)) {
            holder.textName.text = userInfo.userID
        } else {
            holder.textName.text = userInfo.userName
        }

        if (coGuestStore.coGuestState.invitees.value.find { it.userID == userInfo.userID } != null) {
            holder.inviteButton.isSelected = true
            holder.inviteButton.setText(R.string.common_cancel)
            holder.inviteButton.setTextColor(context.resources.getColor(R.color.common_not_standard_red))
        } else {
            holder.inviteButton.isSelected = false
            holder.inviteButton.setText(R.string.common_voiceroom_invite)
            holder.inviteButton.setTextColor(Color.WHITE)
        }

        holder.inviteButton.setOnClickListener {
            onInviteButtonClickListener?.onItemClick(holder.inviteButton, userInfo)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData() {
        initData()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageHead: ImageFilterView = itemView.findViewById(R.id.iv_head)
        val textName: TextView = itemView.findViewById(R.id.tv_name)
        val textLevel: TextView = itemView.findViewById(R.id.tv_level)
        val inviteButton: TextView = itemView.findViewById(R.id.invite_button)
    }

    interface OnInviteButtonClickListener {
        fun onItemClick(inviteButton: TextView, userInfo: LiveUserInfo)
    }
}