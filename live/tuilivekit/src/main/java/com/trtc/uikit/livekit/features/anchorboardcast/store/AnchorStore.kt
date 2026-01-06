package com.trtc.uikit.livekit.features.anchorboardcast.store

import android.text.TextUtils
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.imsdk.v2.V2TIMManager
import com.trtc.uikit.livekit.common.DEFAULT_BACKGROUND_URL
import com.trtc.uikit.livekit.common.DEFAULT_COVER_URL
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.anchorboardcast.AnchorBoardcastState
import com.trtc.uikit.livekit.features.anchorboardcast.AnchorViewListener
import com.trtc.uikit.livekit.features.anchorboardcast.store.dispatcher.AnchorViewListenerDispatcher
import com.trtc.uikit.livekit.features.anchorboardcast.store.observer.IMFriendshipListener
import com.trtc.uikit.livekit.features.anchorboardcast.store.observer.RoomEngineObserver
import io.trtc.tuikit.atomicxcore.api.live.LiveAudienceStore
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class AnchorState(
    var roomId: String,
    var liveInfo: LiveInfo,
    val lockAudioUserList: StateFlow<LinkedHashSet<String>>,
    val lockVideoUserList: StateFlow<LinkedHashSet<String>>,
)

class AnchorStore(liveInfo: LiveInfo) {
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

    private val _lockAudioUserList = MutableStateFlow<LinkedHashSet<String>>(LinkedHashSet())
    private val _lockVideoUserList = MutableStateFlow<LinkedHashSet<String>>(LinkedHashSet())
    private val state: AnchorState = AnchorState(
        roomId = liveInfo.liveID,
        liveInfo = liveInfo,
        lockAudioUserList = _lockAudioUserList,
        lockVideoUserList = _lockVideoUserList
    )

    private lateinit var liveAudienceStore: LiveAudienceStore
    private val logger = LiveKitLogger.getFeaturesLogger("AnchorStore")
    private val userStore: UserStore = UserStore()
    private val mediaStore: MediaStore = MediaStore()
    private val anchorCoHostStore: AnchorCoHostStore = AnchorCoHostStore(liveInfo)
    private val anchorBattleStore: AnchorBattleStore = AnchorBattleStore(liveInfo)
    private val listenerDispatcher: AnchorViewListenerDispatcher = AnchorViewListenerDispatcher()
    private val roomEngineObserver: RoomEngineObserver = RoomEngineObserver(this)
    private val imFriendshipListener: IMFriendshipListener = IMFriendshipListener(this)
    private val externalState: AnchorBoardcastState

    init {
        addObserver()
        setRoomId(liveInfo.liveID)
        liveAudienceStore = LiveAudienceStore.create(liveInfo.liveID)
        mediaStore.setCustomVideoProcess()
        mediaStore.enableMultiPlaybackQuality(true)
        initCreateRoomState(liveInfo)

        externalState = AnchorBoardcastState()
        initExternalState()
    }

    fun addObserver() {
        TUIRoomEngine.sharedInstance().addObserver(roomEngineObserver)
        V2TIMManager.getFriendshipManager().addFriendListener(imFriendshipListener)
    }

    fun removeObserver() {
        TUIRoomEngine.sharedInstance().removeObserver(roomEngineObserver)
        V2TIMManager.getFriendshipManager().removeFriendListener(imFriendshipListener)
    }

    fun destroy() {
        removeObserver()
        mediaStore.destroy()
        anchorBattleStore.destroy()
        listenerDispatcher.clearAnchorViewListeners()
    }

    fun getLiveAudienceStore(): LiveAudienceStore = liveAudienceStore
    fun getUserStore(): UserStore = userStore

    fun getMediaStore(): MediaStore = mediaStore

    fun getAnchorCoHostStore(): AnchorCoHostStore = anchorCoHostStore

    fun getAnchorBattleStore(): AnchorBattleStore = anchorBattleStore

    fun getState(): AnchorState = state

    fun getCoHostState(): AnchorCoHostState = getAnchorCoHostStore().coHostState

    fun getBattleState(): AnchorBattleState = getAnchorBattleStore().battleState

    fun getUserState(): UserState = getUserStore().userState

    fun getMediaState(): MediaState = getMediaStore().mediaState

    fun setRoomId(roomId: String) {
        state.roomId = roomId
        logger.info("${hashCode()} setRoomId:[mRoomId=$roomId],mLiveObserver:${roomEngineObserver.hashCode()}]")
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
        mediaStore.enablePipMode(enable)
    }

    fun getExternalState(): AnchorBoardcastState = externalState

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
        listenerDispatcher.notifyAnchorViewListener { it.onClickFloatWindow() }
    }

    fun notifyRoomExit() {
        listenerDispatcher.notifyAnchorViewListener { it.onEndLiving(externalState) }
    }

    fun addAnchorViewListener(listener: AnchorViewListener) {
        listenerDispatcher.addAnchorViewListener(listener)
    }

    fun removeAnchorViewListener(listener: AnchorViewListener) {
        listenerDispatcher.removeAnchorViewListener(listener)
    }


    fun onSeatLockStateChanged(seatList: List<TUIRoomDefine.SeatFullInfo>) {
        val seatInfoMap = hashMapOf<String, TUIRoomDefine.SeatFullInfo>()
        for (seatInfo in seatList) {
            if (TextUtils.isEmpty(seatInfo.userId)) {
                continue
            }

            seatInfoMap[seatInfo.userId] = seatInfo
            val lockAudioUsers = state.lockAudioUserList.value
            if (seatInfo.userMicrophoneStatus == TUIRoomDefine.DeviceStatus.CLOSED_BY_ADMIN) {
                lockAudioUsers.add(seatInfo.userId)
            } else {
                lockAudioUsers.remove(seatInfo.userId)
            }
            _lockAudioUserList.update { lockAudioUsers }

            val lockVideoUsers = state.lockVideoUserList.value
            if (seatInfo.userCameraStatus == TUIRoomDefine.DeviceStatus.CLOSED_BY_ADMIN) {
                lockVideoUsers.add(seatInfo.userId)
            } else {
                lockVideoUsers.remove(seatInfo.userId)
            }
            _lockVideoUserList.update { lockVideoUsers }
        }
    }
}