package com.trtc.uikit.livekit.component.gift.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trtc.tuikit.common.util.ScreenUtil.dip2px
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.component.gift.view.adapter.GiftPanelAdapter
import io.trtc.tuikit.atomicxcore.api.gift.Gift
import io.trtc.tuikit.atomicxcore.api.gift.GiftCategory

class GiftCategoryViewPagerManager {
    private var giftClickListener: GiftClickListener? = null

    fun setGiftClickListener(listener: GiftClickListener) {
        giftClickListener = listener
    }

    fun createSingleCategoryView(
        context: Context,
        category: GiftCategory,
        columns: Int
    ): View {
        val inflater = LayoutInflater.from(context)
        val contentView = inflater.inflate(R.layout.gift_layout_category_content, null)
        val recyclerView = contentView.findViewById<RecyclerView>(R.id.rv_gifts)

        recyclerView.layoutManager = GridLayoutManager(context, columns)
        val params = recyclerView.layoutParams
        params.height = dip2px(228f)
        recyclerView.layoutParams = params

        val adapter = GiftPanelAdapter(0, category.giftList.toMutableList(), context)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : GiftPanelAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, gift: Gift, position: Int, pageIndex: Int) {
                giftClickListener?.onClick(position, gift)
            }
        })

        return contentView
    }

    interface GiftClickListener {
        fun onClick(position: Int, gift: Gift)
    }
}