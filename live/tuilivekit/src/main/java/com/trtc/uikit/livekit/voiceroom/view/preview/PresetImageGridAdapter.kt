package com.trtc.uikit.livekit.voiceroom.view.preview

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R

class PresetImageGridAdapter(
    private val context: Context,
    private val dataList: List<String>,
    private var selectedPosition: Int,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<PresetImageGridAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.livekit_recycle_item_preset_cover,
            parent, false
        )
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageURL = dataList[position]
        ImageLoader.load(
            context,
            holder.mImage,
            imageURL,
            R.drawable.anchor_prepare_live_stream_default_cover
        )
        holder.mImageSelectedContainer.visibility =
            if (position == selectedPosition) View.VISIBLE else View.INVISIBLE
        holder.mImage.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.bindingAdapterPosition
            notifyItemChanged(position)
            itemClickListener.onClick(imageURL)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImage: ImageFilterView = itemView.findViewById(R.id.image)
        val mImageSelectedContainer: View = itemView.findViewById(R.id.image_selected_container)
    }

    class GridDividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private val mDividerValue: Int

        init {
            val metrics: DisplayMetrics = context.resources.displayMetrics
            mDividerValue =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14f, metrics).toInt()
        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = mDividerValue
            outRect.left = mDividerValue
        }
    }

    interface OnItemClickListener {
        fun onClick(coverURL: String)
    }
}