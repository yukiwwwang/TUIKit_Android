package com.trtc.uikit.livekit.component.gift.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicxcore.api.gift.GiftCategory

class GiftCategoryTabManager(private val context: Context) {
    private var onCategorySelectedListener: OnCategorySelectedListener? = null
    private var currentSelectedIndex = 0
    private val tabViews = mutableListOf<TextView>()
    private var categories: List<GiftCategory> = emptyList()

    interface OnCategorySelectedListener {
        fun onCategorySelected(categoryIndex: Int, category: GiftCategory?)
    }

    fun setOnCategorySelectedListener(listener: OnCategorySelectedListener) {
        onCategorySelectedListener = listener
    }

    fun createTabLayout(categories: List<GiftCategory>): View {
        this.categories = categories

        val inflater = LayoutInflater.from(context)
        val tabLayoutView = inflater.inflate(R.layout.gift_layout_tab_layout, null)
        val tabContainer = tabLayoutView.findViewById<LinearLayout>(R.id.ll_tab_container)

        categories.forEachIndexed { index, category ->
            val tabView = createTabView(category.name, index)
            tabContainer.addView(tabView)
            tabViews.add(tabView)
        }

        selectTab(0)

        return tabLayoutView
    }

    private fun createTabView(title: String, index: Int): TextView {
        val inflater = LayoutInflater.from(context)
        val tabView = inflater.inflate(R.layout.gift_layout_tab_item, null) as TextView
        
        tabView.apply {
            text = title
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            
            setOnClickListener {
                selectTab(index)
                val selectedCategory = categories.getOrNull(index)
                onCategorySelectedListener?.onCategorySelected(index, selectedCategory)
            }
        }
        
        return tabView
    }

    fun selectTab(index: Int) {
        tabViews.forEachIndexed { i, tabView ->
            if (i == index) {
                tabView.setTextColor(ContextCompat.getColor(context, R.color.common_color_white_e5))
                tabView.isSelected = true
            } else {
                tabView.setTextColor(ContextCompat.getColor(context, R.color.common_text_color_secondary))
                tabView.isSelected = false
            }
        }
        currentSelectedIndex = index
    }
}