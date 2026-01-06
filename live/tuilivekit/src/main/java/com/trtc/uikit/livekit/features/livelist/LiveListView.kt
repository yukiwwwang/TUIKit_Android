package com.trtc.uikit.livekit.features.livelist

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.tencent.qcloud.tuicore.TUICore
import com.trtc.uikit.livekit.features.livelist.view.access.DoubleColumnListViewAdapter
import com.trtc.uikit.livekit.features.livelist.view.access.SingleColumnListViewAdapter
import com.trtc.uikit.livekit.features.livelist.view.access.TUILiveListDataSource
import com.trtc.uikit.livekit.features.livelist.store.LiveInfoListStore
import com.trtc.uikit.livekit.features.livelist.view.doublecolumn.DoubleColumnListView
import com.trtc.uikit.livekit.features.livelist.view.singlecolumn.SingleColumnListView

class LiveListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var style: Style
    private lateinit var singleColumnListView: SingleColumnListView
    private lateinit var doubleColumnListView: DoubleColumnListView

    private lateinit var liveInfoListStore: LiveInfoListStore
    private lateinit var fragmentActivity: FragmentActivity
    private var liveListViewAdapter: LiveListViewAdapter? = null
    private var onItemClickListener: OnItemClickListener? = null
    private var isInit = false

    fun init(
        fragmentActivity: FragmentActivity,
        style: Style,
        adapter: LiveListViewAdapter? = null,
        dataSource: LiveListDataSource? = null
    ) {
        this.fragmentActivity = fragmentActivity
        this.style = style
        liveListViewAdapter = adapter
        val liveDataSource = dataSource ?: TUILiveListDataSource()
        liveInfoListStore = LiveInfoListStore(liveDataSource)
        initLiveColumnListView(fragmentActivity, style, liveInfoListStore)
        isInit = true
    }

    fun updateColumnStyle(style: Style) {
        if (isInit && style != this.style) {
            initLiveColumnListView(fragmentActivity, style, liveInfoListStore)
        }
        this.style = style
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
        when (style) {
            Style.DOUBLE_COLUMN -> setDoubleColumnListViewClickLister(listener)
            else -> setSingleColumnListViewClickLister(listener)
        }
    }

    fun refreshData() {
        when (style) {
            Style.DOUBLE_COLUMN -> doubleColumnListView.refreshData()
            else -> singleColumnListView.refreshData()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        enableSwitchPlaybackQuality(true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        enableSwitchPlaybackQuality(false)
    }

    private fun initLiveColumnListView(
        fragmentActivity: FragmentActivity,
        style: Style,
        liveInfoListStore: LiveInfoListStore
    ) {
        val adapter = liveListViewAdapter ?: when (style) {
            Style.DOUBLE_COLUMN -> DoubleColumnListViewAdapter(fragmentActivity)
            else -> SingleColumnListViewAdapter(fragmentActivity)
        }

        removeAllViews()
        if (style == Style.DOUBLE_COLUMN) {
            doubleColumnListView = DoubleColumnListView(fragmentActivity).apply {
                init(fragmentActivity, adapter, liveInfoListStore)
            }
            addView(doubleColumnListView)
            setDoubleColumnListViewClickLister(onItemClickListener)
        } else {
            singleColumnListView = SingleColumnListView(fragmentActivity).apply {
                init(fragmentActivity, adapter, liveInfoListStore)
            }
            addView(singleColumnListView)
            setSingleColumnListViewClickLister(onItemClickListener)
        }
    }

    private fun setDoubleColumnListViewClickLister(listener: OnItemClickListener?) {
        listener?.let { doubleColumnListView.setOnItemClickListener(it) }
    }

    private fun setSingleColumnListViewClickLister(listener: OnItemClickListener?) {
        listener?.let { singleColumnListView.setOnItemClickListener(it) }
    }

    private fun enableSwitchPlaybackQuality(enable: Boolean) {
        val params: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        params.put("enable", enable)
        TUICore.callService("AdvanceSettingManager", "enableSwitchPlaybackQuality", params)
    }
}
