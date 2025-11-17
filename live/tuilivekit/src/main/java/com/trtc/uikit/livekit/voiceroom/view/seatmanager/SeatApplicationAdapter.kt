package com.trtc.uikit.livekit.voiceroom.view.seatmanager

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.completionHandler
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveUserInfo

class SeatApplicationAdapter(
    private val context: Context,
) : RecyclerView.Adapter<SeatApplicationAdapter.ViewHolder>() {

    private val coGuestStore =
        CoGuestStore.create(LiveListStore.shared().liveState.currentLive.value.liveID)
    private val data: MutableList<LiveUserInfo> =
        coGuestStore.coGuestState.applicants.value.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.livekit_layout_voiceroom_item_seat_application,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = data[position]
        if (!TextUtils.isEmpty(request.userName)) {
            holder.textName.text = request.userName
        } else {
            holder.textName.text = request.userID
        }
        if (TextUtils.isEmpty(request.avatarURL)) {
            holder.imageHead.setImageResource(R.drawable.livekit_ic_avatar)
        } else {
            ImageLoader.load(
                context,
                holder.imageHead,
                request.avatarURL,
                R.drawable.livekit_ic_avatar
            )
        }
        holder.textAccept.setOnClickListener { acceptApplication(request.userID) }
        holder.textReject.setOnClickListener { rejectApplication(request.userID) }
    }

    private fun acceptApplication(userId: String) {
        coGuestStore.acceptApplication(userId, completionHandler {
            onError { code, desc ->
                LOGGER.error("acceptApplication failed,error:$code,message:$desc")
                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
            }
        })
    }

    private fun rejectApplication(userId: String) {
        coGuestStore.rejectApplication(userId, completionHandler {
            onError { code, desc ->
                LOGGER.error("rejectApplication failed,error:$code,message:$desc")
                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData() {
        data.clear()
        data.addAll(coGuestStore.coGuestState.applicants.value)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageHead: ImageFilterView = itemView.findViewById(R.id.iv_head)
        val textName: TextView = itemView.findViewById(R.id.tv_name)
        val textAccept: TextView = itemView.findViewById(R.id.tv_accept)
        val textReject: TextView = itemView.findViewById(R.id.tv_reject)
    }

    companion object {
        private val LOGGER = LiveKitLogger.getVoiceRoomLogger("SeatApplicationAdapter")
    }
}