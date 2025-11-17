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
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.tencent.trtc.TRTCStatistics
import com.trtc.tuikit.common.util.ScreenUtil
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.component.dashboard.service.TRTCObserver
import com.trtc.uikit.livekit.component.dashboard.service.TRTCStatisticsListener
import com.trtc.uikit.livekit.component.dashboard.store.StreamDashboardUserState
import com.trtc.uikit.livekit.component.dashboard.view.CircleIndicator
import com.trtc.uikit.livekit.component.dashboard.view.StreamInfoAdapter

class StreamDashboardDialog(context: Context) : PopupDialog(context, com.trtc.tuikit.common.R.style.TUICommonBottomDialogTheme) {

    private val mTRTCObserver: TRTCObserver = TRTCObserver()
    private val mPagerSnapHelper = PagerSnapHelper()
    private lateinit var mRecyclerMediaInfo: RecyclerView
    private lateinit var mCircleIndicator: CircleIndicator
    private lateinit var mTextUpLoss: TextView
    private lateinit var mTextDownLoss: TextView
    private lateinit var mTextRtt: TextView
    private lateinit var mAdapter: StreamInfoAdapter
    private var mColorGreen: Int = 0
    private var mColorPink: Int = 0
    private val mVideoStatusList = ArrayList<StreamDashboardUserState>()

    private val mRoomObserver = object : TUIRoomObserver() {
        override fun onRoomDismissed(roomId: String, reason: TUIRoomDefine.RoomDismissedReason) {
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
        setTRTCListener()
        updateNetworkStatistics(0, 0, 0)
        setView(view)
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

    private fun setTRTCListener() {
        mTRTCObserver.setListener(object : TRTCStatisticsListener() {
            @SuppressLint("DefaultLocale")
            override fun onNetworkStatisticsChange(rtt: Int, upLoss: Int, downLoss: Int) {
                updateNetworkStatistics(rtt, upLoss, downLoss)
            }

            override fun onLocalStatisticsChange(localArray: ArrayList<TRTCStatistics.TRTCLocalStatistics>) {
                mAdapter.updateLocalVideoStatus(localArray)
                updateCircleIndicator()
            }

            override fun onRemoteStatisticsChange(remoteArray: ArrayList<TRTCStatistics.TRTCRemoteStatistics>) {
                mAdapter.updateRemoteVideoStatus(remoteArray)
                updateCircleIndicator()
            }
        })
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
        TUIRoomEngine.sharedInstance().addObserver(mRoomObserver)
        TUIRoomEngine.sharedInstance().trtcCloud.addListener(mTRTCObserver)
    }

    private fun removeObserver() {
        TUIRoomEngine.sharedInstance().removeObserver(mRoomObserver)
        TUIRoomEngine.sharedInstance().trtcCloud.removeListener(mTRTCObserver)
    }
}