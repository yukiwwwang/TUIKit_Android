package com.trtc.uikit.livekit.component.dashboard.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.trtc.TRTCStatistics
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.component.dashboard.store.StreamDashboardUserState

class StreamInfoAdapter(
    private val mContext: Context,
    private val mDataList: MutableList<StreamDashboardUserState>
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
        val title = if (streamDashboardUserState.isLocal) {
            mContext.getString(R.string.common_dashboard_local_user).also {
                holder.mUserInfoLayout.visibility = if (itemCount == 1) View.GONE else View.VISIBLE
            }
        } else {
            "${mContext.getString(R.string.common_dashboard_remote_user)} : ${streamDashboardUserState.userId}".also {
                holder.mUserInfoLayout.visibility = View.VISIBLE
            }
        }
        
        holder.mTextUserId.text = title
        holder.mTextVideoResolution.text = streamDashboardUserState.videoResolution
        holder.mTextVideoBitrate.text = "${streamDashboardUserState.videoBitrate} kbps"
        holder.mTextVideoFps.text = "${streamDashboardUserState.videoFrameRate} FPS"
        holder.mTextAudioSampleRate.text = "${streamDashboardUserState.audioSampleRate} HZ"
        holder.mTextAudioBitrate.text = "${streamDashboardUserState.audioBitrate} kbps"
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateLocalVideoStatus(localArray: ArrayList<TRTCStatistics.TRTCLocalStatistics>?) {
        localArray?.let { array ->
            for (localStatistics in array) {
                mDataList.clear()
                mDataList.add(
                    StreamDashboardUserState(
                        userId = "",
                        isLocal = true,
                        videoResolution = "${localStatistics.width}*${localStatistics.height}",
                        videoFrameRate = localStatistics.frameRate,
                        videoBitrate = localStatistics.videoBitrate,
                        audioSampleRate = localStatistics.audioSampleRate,
                        audioBitrate = localStatistics.audioBitrate
                    )
                )
            }
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateRemoteVideoStatus(remoteArray: ArrayList<TRTCStatistics.TRTCRemoteStatistics>?) {
        remoteArray?.let { array ->
            if (mDataList.isNotEmpty()) {
                if (mDataList[0].isLocal) {
                    mDataList.subList(1, mDataList.size).clear()
                } else {
                    mDataList.clear()
                }
            }
            
            for (remoteStatistics in array) {
                mDataList.add(
                    StreamDashboardUserState(
                        userId = remoteStatistics.userId,
                        isLocal = false,
                        videoResolution = "${remoteStatistics.width}*${remoteStatistics.height}",
                        videoFrameRate = remoteStatistics.frameRate,
                        videoBitrate = remoteStatistics.videoBitrate,
                        audioSampleRate = remoteStatistics.audioSampleRate,
                        audioBitrate = remoteStatistics.audioBitrate
                    )
                )
            }
            notifyDataSetChanged()
        }
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