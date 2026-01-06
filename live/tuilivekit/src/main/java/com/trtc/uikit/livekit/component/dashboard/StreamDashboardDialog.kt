package com.trtc.uikit.livekit.component.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.trtc.tuikit.common.util.ScreenUtil
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.component.dashboard.view.CircleIndicator
import com.trtc.uikit.livekit.component.dashboard.view.StreamInfoAdapter
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.live.AVStatistics
import io.trtc.tuikit.atomicxcore.api.live.LiveEndedReason
import io.trtc.tuikit.atomicxcore.api.live.LiveListListener
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class StreamDashboardDialog(context: Context, val roomId: String) : AtomicPopover(context) {
    private val mPagerSnapHelper = PagerSnapHelper()
    private lateinit var mRecyclerMediaInfo: RecyclerView
    private lateinit var mCircleIndicator: CircleIndicator
    private lateinit var mTextUpLoss: TextView
    private lateinit var mTextDownLoss: TextView
    private lateinit var mTextRtt: TextView
    private lateinit var mAdapter: StreamInfoAdapter
    private var mColorGreen: Int = 0
    private var mColorPink: Int = 0
    private val mVideoStatusList = ArrayList<AVStatistics>()
    private var subscribeStateJob: Job? = null

    private val liveListListener = object : LiveListListener() {
        override fun onLiveEnded(liveID: String, reason: LiveEndedReason, message: String) {
            dismiss()
        }
    }

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.livekit_stream_dashboard, null)

        bindViewId(view)
        initMediaInfoRecyclerView()
        updateNetworkStatistics(0, 0, 0)
        setContent(view)
    }

    private fun bindViewId(view: View) {
        mTextRtt = view.findViewById(R.id.tv_rtt)
        mTextDownLoss = view.findViewById(R.id.tv_downLoss)
        mTextUpLoss = view.findViewById(R.id.tv_upLoss)
        mRecyclerMediaInfo = view.findViewById(R.id.rv_media_info)
        mCircleIndicator = view.findViewById(R.id.ci_pager)
        mColorGreen = context.resources.getColor(R.color.common_text_color_normal)
        mColorPink = context.resources.getColor(R.color.common_not_standard_pink_f9)
    }

    override fun onStart() {
        super.onStart()
        addObserver()
        window?.let { setDialogMaxHeight(it) }
    }

    override fun onStop() {
        super.onStop()
        removeObserver()
    }

    protected fun setDialogMaxHeight(window: Window) {
        val configuration = context.resources.configuration
        window.setBackgroundDrawableResource(android.R.color.transparent)
        val params = window.attributes
        val screenHeight = context.resources.displayMetrics.heightPixels
        val height = (screenHeight * 0.75).toInt()

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.gravity = Gravity.END
            params.width = context.resources.displayMetrics.widthPixels / 2
        } else {
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
        }
        params.height = height
        window.attributes = params
    }

    private fun initMediaInfoRecyclerView() {
        mCircleIndicator.setCircleRadius(ScreenUtil.dip2px(3f))
        mPagerSnapHelper.attachToRecyclerView(mRecyclerMediaInfo)
        mAdapter = StreamInfoAdapter(context, mVideoStatusList)
        mRecyclerMediaInfo.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerMediaInfo.adapter = mAdapter
        mRecyclerMediaInfo.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateCircleIndicator()
                }
            }
        })
    }

    private fun updateCircleIndicator() {
        val count = mAdapter.itemCount
        mCircleIndicator.visibility = if (count > 1) View.VISIBLE else View.GONE
        mCircleIndicator.setCircleCount(count)
        val snapView = mPagerSnapHelper.findSnapView(mRecyclerMediaInfo.layoutManager)
        if (snapView != null) {
            val position = mRecyclerMediaInfo.layoutManager?.getPosition(snapView) ?: 0
            mCircleIndicator.setSelected(position)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateNetworkStatistics(rtt: Int, upLoss: Int, downLoss: Int) {
        mTextRtt.text = String.format("%dms", rtt)
        mTextRtt.setTextColor(if (rtt > 100) mColorPink else mColorGreen)
        mTextDownLoss.text = String.format("%d%%", downLoss)
        mTextDownLoss.setTextColor(if (downLoss > 10) mColorPink else mColorGreen)
        mTextUpLoss.text = String.format("%d%%", upLoss)
        mTextUpLoss.setTextColor(if (upLoss > 10) mColorPink else mColorGreen)
    }

    private fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                DeviceStore.shared().deviceState.networkInfo.collect {
                    updateNetworkStatistics(it.delay, it.upLoss, it.downLoss)
                }
            }

            launch {
                val liveSeatStore = LiveSeatStore.create(roomId)
                liveSeatStore.liveSeatState.avStatistics.collect {
                    mAdapter.updateRemoteVideoStatus(it)
                }
            }
        }
        LiveListStore.shared().addLiveListListener(liveListListener)
    }

    private fun removeObserver() {
        LiveListStore.shared().removeLiveListListener(liveListListener)
    }
}