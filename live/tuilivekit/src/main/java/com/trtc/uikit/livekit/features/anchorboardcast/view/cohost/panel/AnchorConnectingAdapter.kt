package com.trtc.uikit.livekit.features.anchorboardcast.view.cohost.panel

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.qcloud.tuicore.TUILogin
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import java.util.concurrent.CopyOnWriteArrayList

class AnchorConnectingAdapter(
    private val context: Context,
    private val anchorManager: AnchorManager
) : RecyclerView.Adapter<AnchorConnectingAdapter.LinkMicViewHolder>() {

    private val data = CopyOnWriteArrayList<SeatUserInfo>()

    init {
        initData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkMicViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.livekit_layout_anchor_connecting_item, parent, false
        )
        return LinkMicViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinkMicViewHolder, position: Int) {
        val connectionUser = data[position]
        
        if (TextUtils.isEmpty(connectionUser.userName)) {
            holder.textName.text = connectionUser.userID
        } else {
            holder.textName.text = connectionUser.userName
        }
        
        if (TextUtils.isEmpty(connectionUser.avatarURL)) {
            holder.imageHead.setImageResource(R.drawable.livekit_ic_avatar)
        } else {
            ImageLoader.load(context, holder.imageHead, connectionUser.avatarURL, R.drawable.livekit_ic_avatar)
        }
    }

    private fun initData() {
        data.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(connectionUsers: List<SeatUserInfo>) {
        data.clear()
        val selfUserId = TUILogin.getUserId()
        for (user in connectionUsers) {
            if (!TextUtils.equals(user.userID, selfUserId)) {
                data.add(user)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class LinkMicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageHead: ImageFilterView = itemView.findViewById(R.id.iv_head)
        val textName: TextView = itemView.findViewById(R.id.tv_name)
    }
}