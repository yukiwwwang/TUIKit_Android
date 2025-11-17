package com.trtc.uikit.livekit.component.videoquality

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.PopupDialog
import io.trtc.tuikit.atomicxcore.api.device.VideoQuality

class VideoQualitySelectPanel(
    context: Context,
    private val videoQualityLists: List<VideoQuality>
) : PopupDialog(context) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cancelButton: TextView
    private var listener: OnVideoQualitySelectedListener? = null

    private val roomObserver = object : TUIRoomObserver() {
        override fun onRoomDismissed(roomId: String, reason: TUIRoomDefine.RoomDismissedReason) {
            dismiss()
        }
    }

    interface OnVideoQualitySelectedListener {
        fun onVideoQualitySelected(videoQuality: VideoQuality)
    }

    init {
        initView()
    }

    fun setOnVideoQualitySelectedListener(listener: OnVideoQualitySelectedListener) {
        this.listener = listener
    }

    override fun onStart() {
        super.onStart()
        TUIRoomEngine.sharedInstance().addObserver(roomObserver)
    }

    override fun onStop() {
        TUIRoomEngine.sharedInstance().removeObserver(roomObserver)
        super.onStop()
    }

    private fun initView() {
        val view = View.inflate(context, R.layout.livekit_layout_video_quality_select_panel, null)
        recyclerView = view.findViewById(R.id.rv_resolution_options)
        cancelButton = view.findViewById(R.id.tv_cancel)

        setupRecyclerView()
        setupCancelButton()

        setView(view)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = ResolutionAdapter(videoQualityLists)
        recyclerView.adapter = adapter
    }

    private fun setupCancelButton() {
        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private inner class ResolutionAdapter(
        private val data: List<VideoQuality>
    ) : RecyclerView.Adapter<ResolutionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.livekit_recycler_item_video_quality, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val videoQuality = data[position]
            holder.textView.text = videoQualityToString(videoQuality)

            if (position == data.size - 1) {
                holder.divider.visibility = View.GONE
            } else {
                holder.divider.visibility = View.VISIBLE
            }

            holder.textView.setOnClickListener {
                listener?.onVideoQualitySelected(videoQuality)
                dismiss()
            }
        }

        override fun getItemCount(): Int = data.size

        private fun videoQualityToString(quality: VideoQuality): String {
            return when (quality) {
                VideoQuality.QUALITY_1080P -> "1080P"
                VideoQuality.QUALITY_720P -> "720P"
                VideoQuality.QUALITY_540P -> "540P"
                VideoQuality.QUALITY_360P -> "360P"
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.tv_resolution_text)
            val divider: View = itemView.findViewById(R.id.divider)
        }
    }
}