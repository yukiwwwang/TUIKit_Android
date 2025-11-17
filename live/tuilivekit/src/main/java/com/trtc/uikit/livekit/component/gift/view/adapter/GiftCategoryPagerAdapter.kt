package com.trtc.uikit.livekit.component.gift.view.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.trtc.uikit.livekit.component.gift.view.GiftCategoryViewPagerManager
import io.trtc.tuikit.atomicxcore.api.gift.GiftCategory

class GiftCategoryPagerAdapter(
    private val context: Context,
    private val categories: List<GiftCategory>,
    private val columns: Int,
    private val categoryViewManager: GiftCategoryViewPagerManager
) : RecyclerView.Adapter<GiftCategoryPagerAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val container = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return CategoryViewHolder(container)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        val categoryView = categoryViewManager.createSingleCategoryView(context, category, columns)
        holder.bind(categoryView)
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView as FrameLayout

        fun bind(view: View) {
            container.removeAllViews()
            container.addView(view)
        }
    }
}