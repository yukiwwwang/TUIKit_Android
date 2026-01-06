package com.trtc.uikit.livekit.voiceroom.view.seatmanager

import android.content.Context
import android.text.TextUtils
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.completionHandler
import com.trtc.uikit.livekit.voiceroom.manager.VoiceRoomManager
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import io.trtc.tuikit.atomicxcore.api.live.MoveSeatPolicy
import io.trtc.tuikit.atomicxcore.api.live.TakeSeatMode

class SeatActionSheetGenerator(
    private val context: Context,
    private val voiceRoomManager: VoiceRoomManager?
) {
    private val liveListStore = LiveListStore.shared()
    private val liveSeatStore: LiveSeatStore =
        LiveSeatStore.create(liveListStore.liveState.currentLive.value.liveID)
    private val coGuestStore = CoGuestStore.create(liveListStore.liveState.currentLive.value.liveID)
    private var seatInvitationDialog: SeatInvitationDialog? = null
    private var userManagerDialog: UserManagerDialog? = null

    fun destroy() {
        userManagerDialog?.dismiss()
    }

    fun generate(seatInfo: TUIRoomDefine.SeatInfo): List<ListMenuInfo> {
        val selfUserId = TUIRoomEngine.getSelfInfo().userId
        return if (TextUtils.equals(
                liveListStore.liveState.currentLive.value.liveOwner.userID,
                selfUserId
            )
        ) {
            generaSeatManagerMenuInfo(seatInfo, selfUserId)
        } else {
            generaSeatGeneraUserMenuInfo(seatInfo, selfUserId)
        }
    }

    private fun generaSeatManagerMenuInfo(
        seatInfo: TUIRoomDefine.SeatInfo,
        selfUserId: String
    ): List<ListMenuInfo> {
        val menuInfoList = ArrayList<ListMenuInfo>()
        if (TextUtils.isEmpty(seatInfo.userId)) {
            if (!seatInfo.isLocked) {
                val inviteUser =
                    ListMenuInfo(
                        context.getString(R.string.common_voiceroom_invite),
                        object : ListMenuInfo.OnClickListener {
                            override fun onClick() {
                                showSeatInvitationPanel(seatInfo.index)
                            }
                        })
                menuInfoList.add(inviteUser)
            }
            val lockSeat = ListMenuInfo(
                if (seatInfo.isLocked) {
                    context.getString(R.string.common_voiceroom_unlock)
                } else {
                    context.getString(R.string.common_voiceroom_lock)
                }, object : ListMenuInfo.OnClickListener {
                    override fun onClick() {
                        lockSeat(seatInfo)
                    }
                })
            menuInfoList.add(lockSeat)
            return menuInfoList
        }
        if (isSelfSeatInfo(seatInfo, selfUserId)) {
            return menuInfoList
        }
        showUserManagerPanel(seatInfo)
        return menuInfoList
    }

    private fun generaSeatGeneraUserMenuInfo(
        seatInfo: TUIRoomDefine.SeatInfo,
        selfUserId: String
    ): List<ListMenuInfo> {
        val menuInfoList = ArrayList<ListMenuInfo>()
        if (seatInfo.isLocked) {
            return menuInfoList
        }

        if (TextUtils.isEmpty(seatInfo.userId)) {
            if (coGuestStore.coGuestState.connected.value.none { it.userID == TUIRoomEngine.getSelfInfo().userId }) {
                val takeSeat =
                    ListMenuInfo(
                        context.getString(R.string.common_voiceroom_take_seat),
                        object : ListMenuInfo.OnClickListener {
                            override fun onClick() {
                                takeSeat(seatInfo.index)
                            }
                        })
                menuInfoList.add(takeSeat)
            } else {
                val moveSeat =
                    ListMenuInfo(
                        context.getString(R.string.common_voiceroom_take_seat),
                        object : ListMenuInfo.OnClickListener {
                            override fun onClick() {
                                moveToSeat(seatInfo.index)
                            }
                        })
                menuInfoList.add(moveSeat)
            }
            return menuInfoList
        }
        if (isSelfSeatInfo(seatInfo, selfUserId)) {
            return menuInfoList
        }
        showUserManagerPanel(seatInfo)
        return menuInfoList
    }

    private fun showSeatInvitationPanel(index: Int) {
        if (seatInvitationDialog == null) {
            seatInvitationDialog = SeatInvitationDialog(context)
        }
        seatInvitationDialog?.setInviteSeatIndex(index)
        seatInvitationDialog?.show()
    }

    private fun showUserManagerPanel(seatInfo: TUIRoomDefine.SeatInfo) {
        if (userManagerDialog == null) {
            userManagerDialog = UserManagerDialog(context, voiceRoomManager)
        }
        userManagerDialog?.setSeatIndex(seatInfo.index)
        userManagerDialog?.show()
    }

    private fun isSelfSeatInfo(seatInfo: TUIRoomDefine.SeatInfo, selfUserId: String): Boolean {
        if (TextUtils.isEmpty(selfUserId)) {
            return false
        }
        return selfUserId == seatInfo.userId
    }

    private fun takeSeat(seatIndex: Int) {
        val liveInfo = liveListStore.liveState.currentLive.value
        val isOwner = TUIRoomEngine.getSelfInfo().userId == liveInfo.liveOwner.userID
        if (liveInfo.seatMode == TakeSeatMode.FREE || isOwner) {
            liveSeatStore.takeSeat(
                seatIndex,
                completionHandler {
                    onError { code, _ ->
                        ErrorLocalized.onError(
                            code
                        )
                    }
                })
            return
        }

        voiceRoomManager?.viewStore?.updateTakeSeatState(true)
        coGuestStore.applyForSeat(seatIndex, 60, null, completionHandler {
            onError { code, _ ->
                if (code != ErrorLocalized.LIVE_SERVER_ERROR_ALREADY_ON_THE_MIC_QUEUE) {
                    voiceRoomManager?.viewStore?.updateTakeSeatState(false)
                }
                ErrorLocalized.onError(
                    code
                )
            }
        })
    }

    private fun moveToSeat(seatIndex: Int) {
        liveSeatStore.moveUserToSeat(
            TUIRoomEngine.getSelfInfo().userId,
            seatIndex,
            MoveSeatPolicy.ABORT_WHEN_OCCUPIED,
            completionHandler {
                onError { code, _ ->
                    ErrorLocalized.onError(
                        code
                    )
                }
            }
        )
    }

    private fun lockSeat(seatInfo: TUIRoomDefine.SeatInfo) {
        if (seatInfo.isLocked) {
            liveSeatStore.unlockSeat(seatInfo.index, null)
        } else {
            liveSeatStore.lockSeat(seatInfo.index, null)
        }
    }
}