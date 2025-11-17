package com.trtc.uikit.livekit.voiceroom.interaction.connection

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.StandardDialog
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.voiceroom.interaction.common.BattleParticipantAdapter
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.BattleConfig
import io.trtc.tuikit.atomicxcore.api.live.BattleEndedReason
import io.trtc.tuikit.atomicxcore.api.live.BattleInfo
import io.trtc.tuikit.atomicxcore.api.live.BattleListener
import io.trtc.tuikit.atomicxcore.api.live.BattleRequestCallback
import io.trtc.tuikit.atomicxcore.api.live.BattleStore
import io.trtc.tuikit.atomicxcore.api.live.CoHostStatus
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CrossRoomSessionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), LifecycleOwner {
    private val DEBOUNCE_INTERVAL_MS = 500L
    private lateinit var buttonStartPK: Button
    private lateinit var participantAdapter: BattleParticipantAdapter
    private lateinit var exitView: LinearLayout
    private val coHostStore: CoHostStore
    private val battleStore: BattleStore
    private lateinit var recyclerConnectedList: RecyclerView
    private val jobs = mutableListOf<Job>()
    private lateinit var textTitle: TextView
    private lateinit var textExit: TextView
    private var isRequestingBattle = false
    private var lastClickTime: Long = 0L
    private var currentBattleID: String? = null
    private var exitBattleDialog: StandardDialog? = null
    private var exitConnectionDialog: StandardDialog? = null
    private var invitedUserList: List<String> = emptyList()
    private lateinit var exitBattleListener: OnClickListener
    private lateinit var exitConnectionListener: OnClickListener
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    private val parentLifecycleObserver = LifecycleEventObserver { _, event ->
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    private val battleListener: BattleListener = object : BattleListener() {
        override fun onBattleEnded(battleInfo: BattleInfo, reason: BattleEndedReason?) {
            isRequestingBattle = false
            currentBattleID = null
            invitedUserList = emptyList()
            post { updatePKButtonUI() }
        }

        override fun onBattleRequestTimeout(
            battleId: String,
            inviter: SeatUserInfo,
            invitee: SeatUserInfo,
        ) {
            resetBattleRequestState(battleId)
        }

        override fun onBattleRequestReject(
            battleId: String,
            inviter: SeatUserInfo,
            invitee: SeatUserInfo,
        ) {
            resetBattleRequestState(battleId)
        }
    }

    init {
        val liveID = LiveListStore.shared().liveState.currentLive.value.liveID
        coHostStore = CoHostStore.create(liveID)
        battleStore = BattleStore.create(liveID)
        battleStore.addBattleListener(battleListener)
        initView(context)
        addObserver()
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context)
            .inflate(R.layout.livekit_voiceroom_cross_room_session_view, this, true)
        bindViewId()
        initStartPKButton()
        initLeaveView()
        initCoHostConnectedList()
    }

    private fun bindViewId() {
        textTitle = findViewById(R.id.tv_title)
        textExit = findViewById(R.id.tv_exit)
        exitView = findViewById(R.id.ll_exit_pk)
        buttonStartPK = findViewById(R.id.btn_start_pk)
        recyclerConnectedList = findViewById(R.id.rv_in_co_list)
    }

    private fun initStartPKButton() {
        updatePKButtonUI()
        buttonStartPK.setOnClickListener {
            val currentTime = SystemClock.elapsedRealtime()
            if (currentTime - lastClickTime < DEBOUNCE_INTERVAL_MS) {
                return@setOnClickListener
            }
            lastClickTime = currentTime
            if (isRequestingBattle) {
                currentBattleID?.let { battleId ->
                    battleStore.cancelBattleRequest(
                        battleId,
                        invitedUserList,
                        object : CompletionHandler {
                            override fun onSuccess() {
                                isRequestingBattle = false
                                currentBattleID = null
                                invitedUserList = emptyList()
                                post { updatePKButtonUI() }
                            }

                            override fun onFailure(code: Int, desc: String) {
                                ErrorLocalized.onError(code)
                            }
                        })
                }
            } else {
                val selfId = TUIRoomEngine.getSelfInfo().userId
                val connectedList = coHostStore.coHostState.connected.value
                    .filter { it.userID != selfId }
                    .map { it.userID }

                if (connectedList.isEmpty()) return@setOnClickListener

                val config = BattleConfig(BATTLE_DURATION, true, "")
                battleStore.requestBattle(
                    config,
                    connectedList,
                    BATTLE_REQUEST_TIMEOUT,
                    object : BattleRequestCallback {
                        override fun onSuccess(
                            battleInfo: BattleInfo,
                            resultMap: Map<String, Int>,
                        ) {
                            isRequestingBattle = true
                            currentBattleID = battleInfo.battleID
                            invitedUserList = connectedList
                            post { updatePKButtonUI() }
                        }

                        override fun onError(code: Int, desc: String) {
                            ErrorLocalized.onError(code)
                        }
                    })
            }
        }
    }

    private fun resetBattleRequestState(failedBattleId: String) {
        if (isRequestingBattle && failedBattleId == currentBattleID) {
            isRequestingBattle = false
            currentBattleID = null
            invitedUserList = emptyList()
            post {
                updatePKButtonUI()
            }
        }
    }

    private fun updatePKButtonUI() {
        if (isRequestingBattle) {
            buttonStartPK.setText(R.string.seat_cancel_invite)
            buttonStartPK.setBackgroundResource(R.drawable.livekit_btn_grey_edge_bg)
        } else {
            buttonStartPK.setText(R.string.seat_invite_battle)
            buttonStartPK.setBackgroundResource(R.drawable.livekit_voiceroom_start_pk_button_bg)
        }
    }

    private fun initLeaveView() {
        exitConnectionListener = OnClickListener {
            StandardDialog(context).also { dialog ->
                exitConnectionDialog = dialog
                dialog.setContent(resources.getString(R.string.common_disconnect_tips))
                dialog.setNegativeText(
                    context.getString(R.string.common_disconnect_cancel),
                    ContextCompat.getColor(context, R.color.common_text_color_secondary)
                ) { dialog.dismiss() }

                dialog.setPositiveText(
                    context.getString(R.string.common_end_connect),
                    ContextCompat.getColor(context, R.color.common_text_color_abnormal)
                ) {
                    coHostStore.exitHostConnection(object : CompletionHandler {
                        override fun onSuccess() {
                            dialog.dismiss()
                        }

                        override fun onFailure(code: Int, desc: String) {
                            ErrorLocalized.onError(code)
                        }
                    })
                }
                dialog.show()
            }
        }

        exitBattleListener = OnClickListener {
            StandardDialog(context).also { dialog ->
                exitBattleDialog = dialog
                dialog.setContent(context.getString(R.string.common_battle_end_pk_tips))
                dialog.setNegativeText(
                    context.getString(R.string.common_disconnect_cancel),
                    context.resources.getColor(R.color.common_text_color_secondary)
                ) { dialog.dismiss() }
                dialog.setPositiveText(
                    context.getString(R.string.seat_end_Battle),
                    context.resources.getColor(R.color.common_text_color_abnormal)
                ) {
                    battleStore.exitBattle(
                        battleStore.battleState.currentBattleInfo.value?.battleID,
                        object : CompletionHandler {
                            override fun onSuccess() {
                                dialog.dismiss()
                            }

                            override fun onFailure(code: Int, desc: String) {
                                ErrorLocalized.onError(code)
                            }
                        })
                }
                dialog.show()
            }
        }
    }

    private fun initCoHostConnectedList() {
        recyclerConnectedList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        participantAdapter = BattleParticipantAdapter(context)
        recyclerConnectedList.adapter = participantAdapter
    }

    private fun addObserver() {
        coHostStore.coHostState.connected
            .onEach(::onConnectedListUserChange)
            .launchIn(lifecycleScope)
            .let(jobs::add)

        coHostStore.coHostState.coHostStatus
            .onEach(::onConnectedStatusChange)
            .launchIn(lifecycleScope)
            .let(jobs::add)

        battleStore.battleState.battleUsers
            .onEach(::onBattleListUserChange)
            .launchIn(lifecycleScope)
            .let(jobs::add)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onConnectedListUserChange(connectedList: List<SeatUserInfo>) {
        participantAdapter.submitList(ArrayList(connectedList))
    }

    private fun onBattleListUserChange(battleList: List<SeatUserInfo>) {
        if (battleList.isNotEmpty()) {
            textTitle.setText(R.string.seat_in_pk)
            textExit.text = context.getString(R.string.common_battle_end_pk)
            buttonStartPK.visibility = GONE
            participantAdapter.isInPK = true
            exitView.setOnClickListener(exitBattleListener)
            exitConnectionDialog?.dismiss()
        } else {
            textTitle.setText(R.string.common_battle_connecting)
            textExit.text = context.getString(R.string.common_end_connect)
            buttonStartPK.visibility = VISIBLE
            participantAdapter.isInPK = false
            exitView.setOnClickListener(exitConnectionListener)
            exitBattleDialog?.dismiss()
        }
    }

    private fun onConnectedStatusChange(status: CoHostStatus) {
        val isConnected = (status == CoHostStatus.CONNECTED)
        buttonStartPK.visibility = if (isConnected) VISIBLE else GONE
        exitView.visibility = if (isConnected) VISIBLE else GONE
        if (!isConnected) exitConnectionDialog?.dismiss()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.lifecycle?.addObserver(parentLifecycleObserver)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(parentLifecycleObserver)
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    companion object {
        const val BATTLE_REQUEST_TIMEOUT = 10
        const val BATTLE_DURATION = 30
    }
}