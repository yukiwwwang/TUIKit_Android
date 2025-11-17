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
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import com.trtc.uikit.livekit.features.anchorboardcast.state.CoHostState
import com.trtc.uikit.livekit.features.anchorboardcast.state.CoHostState.ConnectionStatus.INVITING
import com.trtc.uikit.livekit.features.anchorboardcast.state.CoHostState.ConnectionStatus.UNKNOWN
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.CoHostLayoutTemplate
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import kotlinx.coroutines.flow.update
import java.util.concurrent.CopyOnWriteArrayList

class AnchorRecommendedAdapter(
    private val context: Context,
    private val anchorManager: AnchorManager
) : RecyclerView.Adapter<AnchorRecommendedAdapter.RecommendViewHolder>() {

    private val logger = LiveKitLogger.getFeaturesLogger("AnchorRecommendedAdapter")
    private val data = CopyOnWriteArrayList<CoHostState.ConnectionUser>()

    init {
        initData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.livekit_layout_anchor_connection_recommendation_list, parent, false
        )
        return RecommendViewHolder(view)
    }

    fun updateData(recommendList: List<CoHostState.ConnectionUser>) {
        data.clear()
        val selfUserId = TUILogin.getUserId()
        for (recommendUser in recommendList) {
            if (!TextUtils.equals(recommendUser.userId, selfUserId)) {
                data.add(recommendUser)
            }
        }
    }

    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        val recommendUser = data[position]

        setUserName(holder, recommendUser)
        setAvatar(holder, recommendUser)
        setConnectionStatus(holder, recommendUser)
        setConnectionClickListener(holder, recommendUser)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun setUserName(holder: RecommendViewHolder, recommendUser: CoHostState.ConnectionUser) {
        holder.textName.text = if (TextUtils.isEmpty(recommendUser.userName)) {
            recommendUser.userId
        } else {
            recommendUser.userName
        }
    }

    private fun setAvatar(holder: RecommendViewHolder, recommendUser: CoHostState.ConnectionUser) {
        if (TextUtils.isEmpty(recommendUser.avatarUrl)) {
            holder.imageHead.setImageResource(R.drawable.livekit_ic_avatar)
        } else {
            ImageLoader.load(context, holder.imageHead, recommendUser.avatarUrl, R.drawable.livekit_ic_avatar)
        }
    }

    private fun setConnectionStatus(holder: RecommendViewHolder, recommendUser: CoHostState.ConnectionUser) {
        if (recommendUser.connectionStatus == INVITING) {
            holder.textConnect.setText(R.string.common_connect_inviting)
            holder.textConnect.alpha = 0.5f
        } else {
            holder.textConnect.setText(R.string.common_voiceroom_invite)
            holder.textConnect.alpha = 1f
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setConnectionClickListener(holder: RecommendViewHolder, recommendUser: CoHostState.ConnectionUser) {
        holder.textConnect.setOnClickListener {
            if (recommendUser.connectionStatus == UNKNOWN) {
                recommendUser.connectionStatus = INVITING
                notifyDataSetChanged()
                val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
                CoHostStore.create(currentLiveId).requestHostConnection(
                    recommendUser.roomId, CoHostLayoutTemplate.HOST_DYNAMIC_GRID, 10, "",
                    object : CompletionHandler {
                        override fun onSuccess() {
                            logger.error("AnchorRecommendedAdapter requestHostConnection onSuccess")
                        }

                        override fun onFailure(code: Int, desc: String) {
                            logger.error("AnchorRecommendedAdapter requestHostConnection failed:code:$code,desc:$desc")
                            anchorManager.getCoHostState().recommendUsers.update { list ->
                                list.map { user ->
                                    if (user.roomId == recommendUser.roomId) {
                                        val newUser =
                                            CoHostState.ConnectionUser(user, CoHostState.ConnectionStatus.UNKNOWN)
                                        newUser
                                    } else {
                                        user
                                    }
                                }
                            }
                            ErrorLocalized.onError(code)
                        }
                    })
            }
        }
    }

    private fun initData() {
        anchorManager.getCoHostState().recommendUsers.value.let {
            data.clear()
            val selfUserId = TUILogin.getUserId()
            for (recommendUser in it) {
                if (!TextUtils.equals(recommendUser.userId, selfUserId)) {
                    data.add(recommendUser)
                }
            }
        }
    }

    class RecommendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageHead: ImageFilterView = itemView.findViewById(R.id.iv_head)
        val textName: TextView = itemView.findViewById(R.id.tv_name)
        val textConnect: TextView = itemView.findViewById(R.id.tv_connect)
    }
}