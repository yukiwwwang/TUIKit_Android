package com.trtc.uikit.livekit.component.videoquality

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover
import io.trtc.tuikit.atomicxcore.api.device.MirrorType
import io.trtc.tuikit.atomicxcore.api.live.LiveEndedReason
import io.trtc.tuikit.atomicxcore.api.live.LiveListListener
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore

class LocalMirrorSelectPanel(
    context: Context,
    private val mirrorTypeList: List<MirrorType>
) : AtomicPopover(context) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cancelButton: TextView
    private var listener: OnMirrorTypeSelectedListener? = null

    private val liveListListener = object : LiveListListener() {
        override fun onLiveEnded(liveID: String, reason: LiveEndedReason, message: String) {
            dismiss()
        }
    }

    interface OnMirrorTypeSelectedListener {
        fun onVideoQualitySelected(mirrorType: MirrorType)
    }

    init {
        initView()
    }

    fun setOnMirrorTypeSelectedListener(listener: OnMirrorTypeSelectedListener) {
        this.listener = listener
    }

    override fun onStart() {
        super.onStart()
        LiveListStore.shared().addLiveListListener(liveListListener)
    }

    override fun onStop() {
        LiveListStore.shared().removeLiveListListener(liveListListener)
        super.onStop()
    }

    private fun initView() {
        val view = View.inflate(context, R.layout.livekit_layout_mirror_type_select_panel, null)
        recyclerView = view.findViewById(R.id.rv_mirror_options)
        cancelButton = view.findViewById(R.id.tv_cancel)

        setupRecyclerView()
        setupCancelButton()

        setContent(view)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = MirrorAdapter(mirrorTypeList)
        recyclerView.adapter = adapter
    }

    private fun setupCancelButton() {
        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private inner class MirrorAdapter(
        private val data: List<MirrorType>
    ) : RecyclerView.Adapter<MirrorAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.livekit_recycler_item_mirror_type, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val videoQuality = data[position]
            holder.textView.text = mirrorTypeToString(videoQuality)

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

        private fun mirrorTypeToString(mirrorType: MirrorType): String {
            return when (mirrorType) {
                MirrorType.AUTO -> context.getString(R.string.mirror_type_auto)
                MirrorType.ENABLE -> context.getString(R.string.mirror_type_enable)
                MirrorType.DISABLE -> context.getString(R.string.mirror_type_disable)
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.tv_resolution_text)
            val divider: View = itemView.findViewById(R.id.divider)
        }
    }
}