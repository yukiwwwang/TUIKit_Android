package com.trtc.uikit.livekit.features.anchorboardcast.manager

import android.text.TextUtils
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.DEFAULT_BACKGROUND_URL
import com.trtc.uikit.livekit.common.DEFAULT_COVER_URL
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.anchorboardcast.AnchorViewListener
import com.trtc.uikit.livekit.features.anchorboardcast.manager.api.impl.AnchorAPIImpl
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.AnchorViewListenerManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.BattleManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.CoHostManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.MediaManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.UserManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.observer.IMFriendshipListener
import com.trtc.uikit.livekit.features.anchorboardcast.manager.observer.RoomEngineObserver
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorConfig
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorState
import com.trtc.uikit.livekit.features.anchorboardcast.state.BattleState
import com.trtc.uikit.livekit.features.anchorboardcast.state.CoHostState
import com.trtc.uikit.livekit.features.anchorboardcast.state.MediaState
import com.trtc.uikit.livekit.features.anchorboardcast.state.UserState
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo

class AnchorManager(liveInfo: LiveInfo) {
    companion object {
        fun disableHeaderLiveData(disable: Boolean) {
            if (AnchorConfig.disableHeaderLiveData.value == disable) {
                return
            }
            AnchorConfig.disableHeaderLiveData.value = disable
        }

        fun disableHeaderVisitorCnt(disable: Boolean) {
            if (AnchorConfig.disableHeaderVisitorCnt.value == disable) {
                return
            }
            AnchorConfig.disableHeaderVisitorCnt.value = disable
        }

        fun disableFooterCoGuest(disable: Boolean) {
            if (AnchorConfig.disableFooterCoGuest.value == disable) {
                return
            }
            AnchorConfig.disableFooterCoGuest.value = disable
        }

        fun disableFooterCoHost(disable: Boolean) {
            if (AnchorConfig.disableFooterCoHost.value == disable) {
                return
            }
            AnchorConfig.disableFooterCoHost.value = disable
        }

        fun disableFooterBattle(disable: Boolean) {
            if (AnchorConfig.disableFooterBattle.value == disable) {
                return
            }
            AnchorConfig.disableFooterBattle.value = disable
        }

        fun disableFooterSoundEffect(disable: Boolean) {
            if (AnchorConfig.disableFooterSoundEffect.value == disable) {
                return
            }
            AnchorConfig.disableFooterSoundEffect.value = disable
        }
    }

    private val logger = LiveKitLogger.getFeaturesLogger("AnchorManager")
    private val userManager: UserManager
    private val mediaManager: MediaManager
    private val coHostManager: CoHostManager
    private val battleManager: BattleManager
    private val listenerManager: AnchorViewListenerManager
    private val state: AnchorState = AnchorState()
    private val liveService = AnchorAPIImpl()
    private val roomEngineObserver: RoomEngineObserver
    private val imFriendshipListener: IMFriendshipListener
    private val externalState: com.trtc.uikit.livekit.features.anchorboardcast.AnchorBoardcastState
    private var liveStateListener: LiveStateListener? = null

    init {
        userManager = UserManager(state, liveService)
        mediaManager = MediaManager(state, liveService)
        coHostManager = CoHostManager(state, liveService)
        battleManager = BattleManager(state, liveService)
        roomEngineObserver = RoomEngineObserver(this)
        imFriendshipListener = IMFriendshipListener(this)
        listenerManager = AnchorViewListenerManager()

        addObserver()
        setRoomId(liveInfo.liveID)
        mediaManager.setCustomVideoProcess()
        mediaManager.enableMultiPlaybackQuality(true)
        initCreateRoomState(liveInfo)

        externalState = com.trtc.uikit.livekit.features.anchorboardcast.AnchorBoardcastState()
        initExternalState()
    }

    fun addObserver() {
        liveService.addRoomEngineObserver(roomEngineObserver)
        liveService.addFriendListener(imFriendshipListener)
    }

    fun removeObserver() {
        liveService.removeRoomEngineObserver(roomEngineObserver)
        liveService.removeFriendListener(imFriendshipListener)
    }

    fun destroy() {
        removeObserver()
        userManager.destroy()
        mediaManager.destroy()
        coHostManager.destroy()
        battleManager.destroy()
        listenerManager.clearAnchorViewListeners()
    }

    fun getUserManager(): UserManager = userManager

    fun getMediaManager(): MediaManager = mediaManager

    fun getCoHostManager(): CoHostManager = coHostManager

    fun getBattleManager(): BattleManager = battleManager

    fun getState(): AnchorState = state

    fun getCoHostState(): CoHostState = state.coHostState

    fun getBattleState(): BattleState = state.battleState

    fun getUserState(): UserState = state.userState

    fun getMediaState(): MediaState = state.mediaState

    fun setRoomId(roomId: String) {
        state.roomId = roomId
        logger.info(
            "${hashCode()} setRoomId:[mRoomId=$roomId,mLiveService:${liveService.hashCode()}" +
                    ",mLiveObserver:${roomEngineObserver.hashCode()}]"
        )
    }

    fun initCreateRoomState(liveInfo: LiveInfo) {
        logger.info("initCreateRoomState roomId [roomId: ${liveInfo.liveID}, roomName:${liveInfo.liveName}")
        state.roomId = liveInfo.liveID
        if (TextUtils.isEmpty(liveInfo.coverURL)) {
            liveInfo.coverURL = DEFAULT_COVER_URL
        }
        if (TextUtils.isEmpty(liveInfo.backgroundURL)) {
            liveInfo.backgroundURL = DEFAULT_BACKGROUND_URL
        }
    }

    fun updateRoomState(liveInfo: LiveInfo) {
        state.liveInfo = liveInfo
    }

    fun enablePipMode(enable: Boolean) {
        mediaManager.enablePipMode(enable)
    }

    fun getExternalState(): com.trtc.uikit.livekit.features.anchorboardcast.AnchorBoardcastState = externalState

    fun setExternalState(messageCount: Int) {
        externalState.duration = System.currentTimeMillis() - state.liveInfo.createTime
        externalState.messageCount = messageCount.toLong()
    }

    fun setLiveStatisticsData(data: TUILiveListManager.LiveStatisticsData?) {
        if (data == null) {
            return
        }
        externalState.viewCount = data.totalViewers.toLong()
        externalState.giftSenderCount = data.totalUniqueGiftSenders.toLong()
        externalState.giftIncome = data.totalGiftCoins.toLong()
        externalState.likeCount = data.totalLikesReceived.toLong()
    }

    private fun initExternalState() {
        externalState.duration = 0
        externalState.viewCount = 0
        externalState.messageCount = 0
    }

    fun notifyPictureInPictureClick() {
        listenerManager.notifyAnchorViewListener { it.onClickFloatWindow() }
    }

    fun notifyRoomExit() {
        listenerManager.notifyAnchorViewListener { it.onEndLiving(externalState) }
    }

    fun addAnchorViewListener(listener: AnchorViewListener) {
        listenerManager.addAnchorViewListener(listener)
    }

    fun removeAnchorViewListener(listener: AnchorViewListener) {
        listenerManager.removeAnchorViewListener(listener)
    }

    fun setLiveStateListener(listener: LiveStateListener) {
        liveStateListener = listener
    }

    fun onKickedOffLine(message: String) {
        liveStateListener?.onKickedOffLine(message)
    }

    fun onKickedOutOfRoom(roomId: String, reason: TUIRoomDefine.KickedOutOfRoomReason, message: String) {
        liveStateListener?.onKickedOutOfRoom(roomId, reason, message)
    }

    fun onSeatLockStateChanged(seatList: List<TUIRoomDefine.SeatInfo>) {
        val seatInfoMap = hashMapOf<String, TUIRoomDefine.SeatInfo>()
        for (seatInfo in seatList) {
            if (TextUtils.isEmpty(seatInfo.userId)) {
                continue
            }

            seatInfoMap[seatInfo.userId] = seatInfo
            val lockAudioUsers = state.lockAudioUserList.value ?: LinkedHashSet()
            if (seatInfo.isAudioLocked) {
                lockAudioUsers.add(seatInfo.userId)
            } else {
                lockAudioUsers.remove(seatInfo.userId)
            }
            state.lockAudioUserList.value = lockAudioUsers

            val lockVideoUsers = state.lockVideoUserList.value ?: LinkedHashSet()
            if (seatInfo.isVideoLocked) {
                lockVideoUsers.add(seatInfo.userId)
            } else {
                lockVideoUsers.remove(seatInfo.userId)
            }
            state.lockVideoUserList.value = lockVideoUsers
            updateSelfMediaDeviceState(seatInfo)
        }
    }

    private fun updateSelfMediaDeviceState(seatInfo: TUIRoomDefine.SeatInfo?) {
        if (seatInfo == null || TextUtils.isEmpty(seatInfo.userId)) {
            return
        }
        if (seatInfo.userId != TUILogin.getUserId()) {
            return
        }
        val isAudioLocked = seatInfo.isAudioLocked
        val isVideoLocked = seatInfo.isVideoLocked
        val context = ContextProvider.getApplicationContext()

        if (isAudioLocked != getMediaState().isAudioLocked.value) {
            getMediaState().isAudioLocked.value = isAudioLocked
            ToastUtil.toastShortMessage(
                context.resources.getString(
                    if (isAudioLocked) R.string.common_mute_audio_by_master
                    else R.string.common_un_mute_audio_by_master
                )
            )
        }
        if (isVideoLocked != getMediaState().isVideoLocked.value) {
            getMediaState().isVideoLocked.value = isVideoLocked
            ToastUtil.toastShortMessage(
                context.resources.getString(
                    if (isVideoLocked) R.string.common_mute_video_by_owner
                    else R.string.common_un_mute_video_by_master
                )
            )
        }
    }

    interface LiveStateListener {
        fun onRoomDismissed()
        fun onKickedOffLine(message: String)
        fun onKickedOutOfRoom(roomId: String, reason: TUIRoomDefine.KickedOutOfRoomReason, message: String)
    }
}