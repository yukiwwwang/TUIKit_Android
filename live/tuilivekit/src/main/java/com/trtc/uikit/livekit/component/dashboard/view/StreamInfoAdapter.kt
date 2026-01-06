package com.trtc.uikit.livekit.component.dashboard.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicxcore.api.live.AVStatistics

class StreamInfoAdapter(
    private val mContext: Context,
    private val mDataList: MutableList<AVStatistics>
) : RecyclerView.Adapter<StreamInfoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.livekit_adapter_item_stream_info,
            parent,
            false
        )
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val streamDashboardUserState = mDataList[position]
        val title = if (streamDashboardUserState.userID.isEmpty()) {
            mContext.getString(R.string.common_dashboard_local_user).also {
                holder.mUserInfoLayout.visibility = if (itemCount == 1) View.GONE else View.VISIBLE
            }
        } else {
            "${mContext.getString(R.string.common_dashboard_remote_user)} : ${streamDashboardUserState.userID}".also {
                holder.mUserInfoLayout.visibility = View.VISIBLE
            }
        }

        holder.mTextUserId.text = title
        holder.mTextVideoResolution.text =
            "${streamDashboardUserState.videoWidth}*${streamDashboardUserState.videoHeight}"
        holder.mTextVideoBitrate.text = "${streamDashboardUserState.videoBitrate} kbps"
        holder.mTextVideoFps.text = "${streamDashboardUserState.frameRate} FPS"
        holder.mTextAudioSampleRate.text = "${streamDashboardUserState.audioSampleRate} HZ"
        holder.mTextAudioBitrate.text = "${streamDashboardUserState.audioBitrate} kbps"
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateRemoteVideoStatus(statisticsList: List<AVStatistics>) {
        mDataList.clear()
        for (statistics in statisticsList) {
            mDataList.add(statistics)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mUserInfoLayout: LinearLayout = itemView.findViewById(R.id.ll_user_info)
        val mTextUserId: TextView = itemView.findViewById(R.id.tv_user_id)
        val mTextVideoResolution: TextView = itemView.findViewById(R.id.tv_video_resolution)
        val mTextVideoBitrate: TextView = itemView.findViewById(R.id.tv_video_bitrate)
        val mTextVideoFps: TextView = itemView.findViewById(R.id.tv_video_fps)
        val mTextAudioSampleRate: TextView = itemView.findViewById(R.id.tv_audio_sample_rate)
        val mTextAudioBitrate: TextView = itemView.findViewById(R.id.tv_audio_bitrate)
    }
}