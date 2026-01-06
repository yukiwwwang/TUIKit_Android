package com.trtc.uikit.livekit.features.anchorprepare.view.liveinfoedit.livecoverpicker

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover

class LiveCoverPicker(context: Context, private val config: Config) : AtomicPopover(context) {
    
    companion object {
        private const val COVER_IMAGE_DEFAULT_WIDTH = 114
    }

    private var selectedImageURL: String = config.currentImageUrl
    private var onItemClickListener: OnItemClickListener? = null

    init {
        initView(context)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    private fun initView(context: Context) {
        val view = View.inflate(context, R.layout.anchor_prepare_layout_select_cover, null)

        initTitleView(view)
        initCoverImagePickRecyclerView(view)
        initSetCoverButton(view)

        setContent(view)
    }

    private fun initTitleView(view: View) {
        val title = view.findViewById<TextView>(R.id.title)
        title.text = config.title

        view.findViewById<View>(R.id.iv_back).setOnClickListener { dismiss() }
    }

    private fun initCoverImagePickRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_image)

        val spanCount = calculateViewColumnCount(context)
        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        recyclerView.addItemDecoration(LiveCoverImagePickAdapter.GridDividerItemDecoration(context))
        
        val selectedPosition = config.data.indexOf(config.currentImageUrl)
        selectedImageURL = config.currentImageUrl
        
        recyclerView.adapter = LiveCoverImagePickAdapter(context, config.data, selectedPosition) { imageUrl ->
            selectedImageURL = imageUrl
        }
    }

    private fun initSetCoverButton(view: View) {
        val confirmButton = view.findViewById<TextView>(R.id.btn_confirm)
        confirmButton.text = config.confirmButtonText

        confirmButton.setOnClickListener {
            onItemClickListener?.onClick(selectedImageURL)
            dismiss()
        }
    }

    private fun calculateViewColumnCount(context: Context): Int {
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val itemWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            COVER_IMAGE_DEFAULT_WIDTH.toFloat(),
            metrics
        ).toInt()
        return screenWidth / itemWidth
    }

    data class Config(
        var title: String = "",
        var confirmButtonText: String = "",
        var data: List<String> = emptyList(),
        var currentImageUrl: String = ""
    )

    fun interface OnItemClickListener {
        fun onClick(imageUrl: String)
    }
}