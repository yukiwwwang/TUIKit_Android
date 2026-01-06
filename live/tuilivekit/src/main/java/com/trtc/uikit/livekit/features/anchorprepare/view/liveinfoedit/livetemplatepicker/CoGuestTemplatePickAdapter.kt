package com.trtc.uikit.livekit.features.anchorprepare.view.liveinfoedit.livetemplatepicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.features.anchorprepare.store.AnchorPrepareStore
import com.trtc.uikit.livekit.features.anchorprepare.view.liveinfoedit.livetemplatepicker.LiveTemplatePicker.TemplateType

class CoGuestTemplatePickAdapter(
    private val context: Context,
    private val store: AnchorPrepareStore,
    private val dataList: List<TemplateType>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<CoGuestTemplatePickAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.anchor_prepare_layout_template_pick_item, parent, false
        )
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val data = dataList[position]
        holder.imageIcon.setImageResource(data.icon)
        holder.textName.text = TemplateType.getNameById(context, data.id)
        
        if (store.getState().coGuestTemplateId.value == data.id) {
            holder.layoutContainer.setBackgroundResource(R.drawable.anchor_prepare_template_icon_background_selected)
        } else {
            holder.layoutContainer.setBackgroundResource(R.drawable.anchor_prepare_template_icon_background)
        }
        
        holder.layoutContainer.setOnClickListener {
            itemClickListener.onClick(dataList[position])
        }
    }

    override fun getItemCount(): Int = dataList.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        val textName: TextView = itemView.findViewById(R.id.tv_name)
        val layoutContainer: LinearLayout = itemView.findViewById(R.id.layout_container)
    }

    fun interface OnItemClickListener {
        fun onClick(type: TemplateType)
    }
}