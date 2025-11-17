package com.trtc.uikit.livekit.features.anchorboardcast.view.cohost.panel

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tencent.cloud.tuikit.engine.extension.TUILiveConnectionManager
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.interfaces.ITUINotification
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.EVENT_KEY_LIVE_KIT
import com.trtc.uikit.livekit.common.EVENT_SUB_KEY_REQUEST_CONNECTION
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import io.trtc.tuikit.atomicxcore.api.live.BattleInfo
import io.trtc.tuikit.atomicxcore.api.live.BattleListener
import io.trtc.tuikit.atomicxcore.api.live.BattleStore
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import io.trtc.tuikit.atomicxcore.api.view.LiveCoreView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
class AnchorCoHostManageDialog(
    context: Context,
    private val anchorManager: AnchorManager,
    private val liveStream: LiveCoreView
) : PopupDialog(context), ITUINotification {

    private lateinit var textConnectedTitle: TextView
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var recyclerConnectedList: RecyclerView
    private lateinit var recyclerRecommendList: RecyclerView
    private lateinit var anchorConnectedAdapter: AnchorConnectingAdapter
    private lateinit var anchorRecommendedAdapter: AnchorRecommendedAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var textDisconnect: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var textRecommendTitle: TextView
    private var subscribeStateJob: Job? = null
    private val battleListener = object : BattleListener() {
        override fun onBattleStarted(
            battleInfo: BattleInfo,
            inviter: SeatUserInfo,
            invitees: List<SeatUserInfo>
        ) {
            if (isShowing) {
                dismiss()
            }
        }
    }

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(
            R.layout.livekit_layout_anchor_connection_manager_panel, null
        )
        bindViewId(view)
        initBackView(view)
        initRefresh()
        initConnectingList()
        initRecommendTitle()
        initRecommendList()
        initDisconnectView()
        initNestedScrollView()
        refreshData()
        setView(view)
        addObserver()
    }

    private fun bindViewId(view: View) {
        nestedScrollView = view.findViewById(R.id.nsv_scroll_view)
        textConnectedTitle = view.findViewById(R.id.tv_connected_title)
        textRecommendTitle = view.findViewById(R.id.tv_recommend_title)
        recyclerConnectedList = view.findViewById(R.id.rv_connecting_user_list)
        recyclerRecommendList = view.findViewById(R.id.rv_recommendation_user_list)
        swipeRefreshLayout = view.findViewById(R.id.srl_recommendation_user_list)
        textDisconnect = view.findViewById(R.id.tv_disconnect)
    }

    private fun addObserver() {
        val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
        BattleStore.create(currentLiveId).addBattleListener(battleListener)
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                onRecommendListChange()
            }
            launch {
                onConnectedUserChange()
            }
        }
        TUICore.registerEvent(EVENT_KEY_LIVE_KIT, EVENT_SUB_KEY_REQUEST_CONNECTION, this)
    }

    private fun removeObserver() {
        val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
        BattleStore.create(currentLiveId).removeBattleListener(battleListener)
        subscribeStateJob?.cancel()
        TUICore.unRegisterEvent(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun onRecommendListChange() {
        anchorManager.getCoHostState().recommendUsers.collect {
            textRecommendTitle.post {
                if (it.isEmpty()) {
                    textRecommendTitle.visibility = GONE
                } else {
                    textRecommendTitle.visibility = VISIBLE
                }
                anchorRecommendedAdapter.updateData(it)
                anchorRecommendedAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "StringFormatMatches")
    private suspend fun onConnectedUserChange() {
        val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
        val coHostStore = CoHostStore.create(currentLiveId)
        coHostStore.coHostState.connected.collect {
            textDisconnect.post {
                if (it.isEmpty()) {
                    textDisconnect.visibility = GONE
                    textConnectedTitle.visibility = GONE
                } else {
                    textDisconnect.visibility = VISIBLE
                    textConnectedTitle.visibility = VISIBLE
                    textConnectedTitle.text = context.getString(
                        R.string.common_connection_list_title,
                        it.size - 1
                    )
                }
                anchorConnectedAdapter.updateData(it)
                anchorConnectedAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initRecommendList() {
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerRecommendList.layoutManager = linearLayoutManager
        anchorRecommendedAdapter = AnchorRecommendedAdapter(context, anchorManager)
        recyclerRecommendList.adapter = anchorRecommendedAdapter
    }

    private fun refreshData() {
        anchorManager.getCoHostManager().fetchLiveList(true)
    }

    private fun loadMoreData() {
        anchorManager.getCoHostManager().fetchLiveList(false)
    }

    private fun initConnectingList() {
        recyclerConnectedList.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )
        anchorConnectedAdapter = AnchorConnectingAdapter(context, anchorManager)
        recyclerConnectedList.adapter = anchorConnectedAdapter
    }

    private fun initBackView(rootView: View) {
        rootView.findViewById<View>(R.id.iv_back)?.setOnClickListener { dismiss() }
    }

    private fun initRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            anchorManager.getCoHostManager().fetchLiveList(true)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun initRecommendTitle() {
        if (anchorManager.getCoHostState().recommendUsers.value.isEmpty()) {
            textRecommendTitle.visibility = GONE
        } else {
            textRecommendTitle.visibility = VISIBLE
        }
    }

    private fun initDisconnectView() {
        textDisconnect.setOnClickListener {
            showDisconnectDialog()
        }
    }

    private fun initNestedScrollView() {
        nestedScrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(
                v: NestedScrollView,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                if (scrollY >= (v.getChildAt(0).measuredHeight - v.measuredHeight)) {
                    Log.d("xander_test", "loadMoreData")
                    if (!anchorManager.getCoHostState().isLoadMore && !anchorManager.getCoHostState().isLastPage) {
                        loadMoreData()
                    }
                }
            }

        })
    }

    private fun showDisconnectDialog() {
        val dialog = StandardDialog(context)
        dialog.setContent(context.getString(R.string.common_disconnect_tips))
        dialog.setAvatar(null)

        dialog.setNegativeText(context.getString(R.string.common_disconnect_cancel)) {
            dialog.dismiss()
        }
        dialog.setPositiveText(context.getString(R.string.common_end_connect)) {
            dialog.dismiss()
            disconnect()
        }
        dialog.show()
    }

    private fun disconnect() {
        val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
        CoHostStore.create(currentLiveId).exitHostConnection(null)
    }

    override fun onNotifyEvent(key: String, subKey: String, param: Map<String, Any>?) {
        if (TextUtils.equals(key, EVENT_KEY_LIVE_KIT) && TextUtils.equals(subKey, EVENT_SUB_KEY_REQUEST_CONNECTION)) {
            if (param == null) {
                ErrorLocalized.onError(TUILiveConnectionManager.ConnectionCode.UNKNOWN.value)
            } else {
                val entry = param.entries.iterator().next()
                val code = entry.value as TUILiveConnectionManager.ConnectionCode
                ErrorLocalized.onError(code.value)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }
}