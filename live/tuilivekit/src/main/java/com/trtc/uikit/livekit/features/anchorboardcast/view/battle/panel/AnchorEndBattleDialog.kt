package com.trtc.uikit.livekit.features.anchorboardcast.view.battle.panel

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import com.trtc.uikit.livekit.features.anchorboardcast.view.cohost.panel.StandardDialog
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.BattleStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore

class AnchorEndBattleDialog(
    context: Context,
    private val liveManager: AnchorManager
) : PopupDialog(context, com.trtc.tuikit.common.R.style.TUICommonBottomDialogTheme) {

    companion object {
        private val LOGGER = LiveKitLogger.getFeaturesLogger("AnchorEndBattleDialog")
    }

    init {
        val view = layoutInflater.inflate(R.layout.livekit_anchor_end_battle_panel, null)
        setView(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCancelButton()
        initEndLiveButton()
    }

    private fun initEndLiveButton() {
        val textEndLive = findViewById<TextView>(R.id.tv_end_live)
        textEndLive?.text = context.getString(R.string.common_battle_end_pk)
        textEndLive?.setOnClickListener {
            dismiss()
            showEndBattleDialog()
        }
    }

    private fun initCancelButton() {
        val textCancel = findViewById<TextView>(R.id.tv_cancel)
        textCancel?.text = context.getString(R.string.common_cancel)
        textCancel?.setOnClickListener { dismiss() }
    }

    private fun showEndBattleDialog() {
        val dialog = StandardDialog(context).apply {
            setContent(context.getString(R.string.common_battle_end_pk_tips))
            setAvatar(null)
            setPositiveTextColor(context.resources.getColor(R.color.common_not_standard_red))
            setNegativeText(context.getString(R.string.common_disconnect_cancel)) {
                dismiss()
            }
            setPositiveText(context.getString(R.string.common_battle_end_pk)) {
                dismiss()
                val battleId = liveManager.getBattleState().battleId
                BattleStore.create(LiveListStore.shared().liveState.currentLive.value.liveID)
                    .exitBattle(battleId, object : CompletionHandler {
                        override fun onSuccess() {}

                        override fun onFailure(code: Int, desc: String) {
                            LOGGER.error("AnchorEndBattleDialog terminateBattle failed:code:$code,desc:$desc")
                            ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                        }

                    })
            }
        }
        dialog.show()
    }
}