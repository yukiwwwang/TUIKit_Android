package com.trtc.uikit.livekit.voiceroom.interaction.common

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo

class BattleParticipantAdapter(private val context: Context) :
    ListAdapter<SeatUserInfo, BattleParticipantAdapter.InPKViewHolder>(DIFF_CALLBACK) {

    @SuppressLint("NotifyDataSetChanged")
    var isInPK: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InPKViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.livekit_voice_in_connection_pk_item, parent, false
        )
        return InPKViewHolder(view)
    }

    override fun onBindViewHolder(holder: InPKViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, position, itemCount) }
    }

    inner class InPKViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageAvatar: ImageFilterView = itemView.findViewById(R.id.iv_avatar)
        private val textName: TextView = itemView.findViewById(R.id.tv_name)
        private val textDescribe: TextView = itemView.findViewById(R.id.tv_describe)
        private val divider: View = itemView.findViewById(R.id.divider)

        fun bind(user: SeatUserInfo, position: Int, totalItemCount: Int) {
            setUserName(user)
            setAvatar(user)
            setDescribe()
            setDividerVisibility(position, totalItemCount)
        }

        private fun setUserName(user: SeatUserInfo) {
            textName.text = user.userName.takeIf { !it.isNullOrEmpty() } ?: user.userID
        }

        private fun setAvatar(user: SeatUserInfo) {
            val avatarUrl = user.avatarURL
            if (avatarUrl.isNullOrEmpty()) {
                imageAvatar.setImageResource(R.drawable.livekit_ic_avatar)
            } else {
                ImageLoader.load(context, imageAvatar, avatarUrl, R.drawable.livekit_ic_avatar)
            }
        }

        private fun setDescribe() {
            textDescribe.visibility = if(isInPK) VISIBLE else GONE
        }

        private fun setDividerVisibility(position: Int, totalItemCount: Int) {
            val isLastItem = (position == totalItemCount - 1)
            if (!isInPK && isLastItem) {
                divider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SeatUserInfo>() {
            override fun areItemsTheSame(oldItem: SeatUserInfo, newItem: SeatUserInfo): Boolean {
                return oldItem.userID == newItem.userID
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: SeatUserInfo, newItem: SeatUserInfo): Boolean {
                return oldItem == newItem
            }
        }
    }
}