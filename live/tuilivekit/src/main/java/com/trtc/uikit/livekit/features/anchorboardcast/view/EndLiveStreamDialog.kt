package com.trtc.uikit.livekit.features.anchorboardcast.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.util.ScreenUtil
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.BattleStore
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.StopLiveCompletionHandler
import io.trtc.tuikit.atomicxcore.api.view.LiveCoreView

@SuppressLint("ViewConstructor")
class EndLiveStreamDialog(
    context: Context,
    private val coreView: LiveCoreView,
    private val anchorManager: AnchorManager,
    private val listener: EndLiveStreamDialogListener?
) : PopupDialog(context) {

    companion object {
        private const val EVENT_KEY_TIME_LIMIT = "RTCRoomTimeLimitService"
        private const val EVENT_SUB_KEY_COUNTDOWN_END = "CountdownEnd"
    }

    private val logger = LiveKitLogger.getFeaturesLogger("EndLiveStreamDialog")
    private lateinit var mRootLayout: LinearLayout

    init {
        initView()
    }

    @SuppressLint("InflateParams")
    private fun initView() {
        mRootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.livekit_popup_dialog_bg)
        }

        initTitleView()
        if (isInCoHost()) {
            if (isInBattle()) {
                initExitBattleItem()
            } else {
                initExitCoHostItem()
            }
        }
        initExitRoomItem()
        initCancelItem()
        setView(mRootLayout)
    }

    private fun isInCoGuest(): Boolean {
        val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
        return CoGuestStore.create(currentLiveId).coGuestState.connected
            .value.filterNot { it.liveID != currentLiveId }.size > 1
    }

    private fun isInCoHost(): Boolean {
        val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
        return CoHostStore.create(currentLiveId).coHostState.connected.value.isNotEmpty()
    }

    private fun isInBattle(): Boolean {
        return anchorManager.getBattleState().isBattleRunning.value == true
    }

    private fun initTitleView() {
        val tips = when {
            isInCoGuest() -> context.getString(R.string.common_anchor_end_link_tips)
            isInCoHost() -> {
                if (isInBattle()) {
                    context.getString(R.string.common_end_pk_tips)
                } else {
                    context.getString(R.string.common_end_connection_tips)
                }
            }

            else -> ""
        }

        if (TextUtils.isEmpty(tips)) {
            return
        }

        addItemView().apply {
            textSize = 12f
            text = tips
        }
        addSplitLine(ScreenUtil.dip2px(1.0f))
    }

    private fun initExitBattleItem() {
        addItemView().apply {
            val color = context.resources.getColor(R.color.common_not_standard_red)
            setTextColor(color)
            text = context.getString(R.string.common_end_pk)
            setOnClickListener {
                val battleId = anchorManager.getBattleState().battleId
                val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
                BattleStore.create(currentLiveId).exitBattle(battleId, object : CompletionHandler {
                    override fun onSuccess() {
                        anchorManager.getBattleManager().onExitBattle()
                    }

                    override fun onFailure(code: Int, desc: String) {
                        logger.error("terminateBattle failed:code:$code,desc:$desc")
                    }

                })
                dismiss()
            }
        }
        addSplitLine(ScreenUtil.dip2px(1.0f))
    }

    private fun initExitCoHostItem() {
        addItemView().apply {
            val color = context.resources.getColor(R.color.common_not_standard_red)
            setTextColor(color)
            text = context.getString(R.string.common_end_connection)
            setOnClickListener {
                val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
                CoHostStore.create(currentLiveId).exitHostConnection(null)
                dismiss()
            }
        }
        addSplitLine(ScreenUtil.dip2px(1.0f))
    }

    private fun initExitRoomItem() {
        addItemView().apply {
            text = context.getString(R.string.common_end_live)
            setOnClickListener { view -> onExitLiveClick(view) }
        }
        addSplitLine(ScreenUtil.dip2px(7.0f))
    }

    private fun onExitLiveClick(view: View) {
        if (!view.isEnabled) {
            return
        }
        view.isEnabled = false
        val keepOwnerOnSeat = anchorManager.getState().liveInfo.keepOwnerOnSeat
        logger.info("onExitLiveClick, keepOwnerOnSeat:$keepOwnerOnSeat")

        if (keepOwnerOnSeat) {
            LiveListStore.shared().endLive(object : StopLiveCompletionHandler {
                override fun onSuccess(statisticsData: TUILiveListManager.LiveStatisticsData) {
                    anchorManager.setLiveStatisticsData(statisticsData)
                    onEnd()
                }

                override fun onFailure(code: Int, desc: String) {
                    logger.error("stopLiveStream onError:code:$code,desc:$desc")
                    onEnd()
                }

                private fun onEnd() {
                    listener?.onRoomExitEndStatistics()
                    dismiss()
                    anchorManager.notifyRoomExit()
                }
            })
        } else {
            coreView.leaveLiveStream(null)
            dismiss()
            listener?.onRoomExit()
        }

        TUICore.notifyEvent(
            TUIConstants.Privacy.EVENT_ROOM_STATE_CHANGED,
            TUIConstants.Privacy.EVENT_SUB_KEY_ROOM_STATE_STOP, null
        )
        TUICore.notifyEvent(EVENT_KEY_TIME_LIMIT, EVENT_SUB_KEY_COUNTDOWN_END, null)
        coreView.setLocalVideoMuteImage(null, null)
    }

    private fun initCancelItem() {
        addItemView().apply {
            text = context.getString(R.string.common_cancel)
            setOnClickListener { dismiss() }
        }
    }

    private fun addSplitLine(height: Int) {
        val view = View(context).apply {
            val color = context.resources.getColor(R.color.common_design_standard_g8)
            setBackgroundColor(color)
        }
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        mRootLayout.addView(view, params)
    }

    private fun addItemView(): TextView {
        val textView = TextView(context).apply {
            val color = context.resources.getColor(R.color.common_design_standard_g2)
            setTextColor(color)
            textSize = 16f
            gravity = Gravity.CENTER
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            ScreenUtil.dip2px(56.0f)
        )
        mRootLayout.addView(textView, params)
        return textView
    }

    interface EndLiveStreamDialogListener {
        fun onRoomExit()
        fun onRoomExitEndStatistics()
    }
}