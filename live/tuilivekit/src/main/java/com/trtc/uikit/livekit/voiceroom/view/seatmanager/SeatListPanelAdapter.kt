package com.trtc.uikit.livekit.voiceroom.view.seatmanager

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.completionHandler
import io.trtc.tuikit.atomicx.widget.basicwidget.avatar.AtomicAvatar
import io.trtc.tuikit.atomicx.widget.basicwidget.avatar.AtomicAvatar.AvatarContent
import io.trtc.tuikit.atomicx.widget.basicwidget.button.AtomicButton
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import io.trtc.tuikit.atomicxcore.api.live.SeatInfo
import java.util.concurrent.CopyOnWriteArrayList

class SeatListPanelAdapter(
    private val context: Context,
) : RecyclerView.Adapter<SeatListPanelAdapter.LinkMicViewHolder>() {

    private val dataList = CopyOnWriteArrayList<SeatInfo>()
    private val liveSeatStore =
        LiveSeatStore.create(LiveListStore.shared().liveState.currentLive.value.liveID)

    init {
        initData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkMicViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.livekit_voiceroom_item_seat_list_panel,
            parent,
            false
        )
        return LinkMicViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinkMicViewHolder, position: Int) {
        if (TextUtils.isEmpty(dataList[position].userInfo.userName)) {
            holder.textName.text = dataList[position].userInfo.userID
        } else {
            holder.textName.text = dataList[position].userInfo.userName
        }

        holder.imageHead.setContent(
            AvatarContent.URL(
                dataList[position].userInfo.avatarURL,
                R.drawable.livekit_ic_avatar
            )
        )

        holder.textSeatIndex.text = (dataList[position].index + 1).toString()
        holder.textHangUp.tag = dataList[position]
        holder.textHangUp.setOnClickListener { view ->
            val seatInfo = view.tag as SeatInfo
            liveSeatStore.kickUserOutOfSeat(seatInfo.userInfo.userID, completionHandler {
                onError { code, desc -> ErrorLocalized.onError(code) }
            })
        }
    }

    private fun initData() {
        val selfUserId = TUIRoomEngine.getSelfInfo().userId
        val selfLiveId = LiveListStore.shared().liveState.currentLive.value.liveID

        val updatedList = liveSeatStore.liveSeatState.seatList.value
            .filterNot { it.userInfo.userID.isEmpty() }
            .filter {
                val userInfo = it.userInfo
                userInfo.userID != selfUserId && userInfo.liveID == selfLiveId
            }

        dataList.clear()
        dataList.addAll(updatedList)
    }

    val data: List<SeatInfo>
        get() = dataList

    @SuppressLint("NotifyDataSetChanged")
    fun updateData() {
        initData()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class LinkMicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageHead: AtomicAvatar = itemView.findViewById(R.id.iv_head)
        val textName: TextView = itemView.findViewById(R.id.tv_name)
        val textLevel: TextView = itemView.findViewById(R.id.tv_level)
        val textHangUp: AtomicButton = itemView.findViewById(R.id.atomic_btn_hang_up)
        val textSeatIndex: TextView = itemView.findViewById(R.id.tv_seat_index)
    }
}