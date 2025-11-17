package com.trtc.uikit.livekit.features.audiencecontainer.manager

import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.uikit.livekit.features.audiencecontainer.manager.observer.AudienceContainerViewListenerList
import com.trtc.uikit.livekit.features.audiencecontainer.manager.observer.AudienceViewListenerList
import com.trtc.uikit.livekit.features.audiencecontainer.manager.observer.RoomEngineObserver
import com.trtc.uikit.livekit.features.audiencecontainer.store.IMState
import com.trtc.uikit.livekit.features.audiencecontainer.store.IMStore
import com.trtc.uikit.livekit.features.audiencecontainer.store.MediaState
import com.trtc.uikit.livekit.features.audiencecontainer.store.MediaStore
import com.trtc.uikit.livekit.features.audiencecontainer.store.ViewState
import com.trtc.uikit.livekit.features.audiencecontainer.store.ViewStore
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.live.BattleState
import io.trtc.tuikit.atomicxcore.api.live.BattleStore
import io.trtc.tuikit.atomicxcore.api.live.CoGuestState
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.CoHostState
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.LiveAudienceStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListState
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatState
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore

class AudienceManager(liveID: String) {
    private val liveListStore: LiveListStore = LiveListStore.shared()
    private val deviceStore: DeviceStore = DeviceStore.shared()
    private val roomEngine: TUIRoomEngine = TUIRoomEngine.sharedInstance()
    private val liveSeatStore: LiveSeatStore
    private val audienceStore: LiveAudienceStore
    private val coGuestStore: CoGuestStore
    private val coHostStore: CoHostStore
    private val battleStore: BattleStore
    private val imStore: IMStore
    private val mediaStore: MediaStore
    private val viewStore: ViewStore
    private val roomEngineObserver: RoomEngineObserver
    private val audienceViewListenerList: AudienceViewListenerList
    private var audienceContainerViewListenerList: AudienceContainerViewListenerList? = null

    init {
        liveSeatStore = LiveSeatStore.create(liveID)
        coGuestStore = CoGuestStore.create(liveID)
        coHostStore = CoHostStore.create(liveID)
        battleStore = BattleStore.create(liveID)
        audienceStore = LiveAudienceStore.create(liveID)
        viewStore = ViewStore(liveID)
        imStore = IMStore()
        mediaStore = MediaStore(liveID)
        roomEngineObserver = RoomEngineObserver(this)
        audienceViewListenerList = AudienceViewListenerList()
    }

    fun setAudienceContainerViewListenerList(viewListenerList: AudienceContainerViewListenerList) {
        audienceContainerViewListenerList = viewListenerList
    }

    fun addObserver() {
        roomEngine.addObserver(roomEngineObserver)
    }

    fun removeObserver() {
        roomEngine.removeObserver(roomEngineObserver)
        audienceViewListenerList.clearListeners()
    }

    fun addAudienceViewListener(listener: AudienceViewListener) {
        audienceViewListenerList.addListener(listener)
    }

    fun removeAudienceViewListener(listener: AudienceViewListener) {
        audienceViewListenerList.removeListener(listener)
    }

    fun destroy() {
        removeObserver()
        mediaStore.destroy()
    }


    fun getLiveListStore(): LiveListStore {
        return liveListStore
    }

    fun getDeviceStore(): DeviceStore {
        return deviceStore
    }

    fun getLiveSeatStore(): LiveSeatStore {
        return liveSeatStore
    }

    fun getCoGuestStore(): CoGuestStore {
        return coGuestStore
    }

    fun getCoHostStore(): CoHostStore {
        return coHostStore
    }

    fun getBattleStore(): BattleStore {
        return battleStore
    }

    fun getLiveAudienceStore(): LiveAudienceStore {
        return audienceStore
    }

    fun getIMStore(): IMStore {
        return imStore
    }

    fun getViewStore(): ViewStore {
        return viewStore
    }

    fun getMediaStore(): MediaStore {
        return mediaStore
    }

    fun getLiveListState(): LiveListState {
        return liveListStore.liveState
    }

    fun getLiveSeatState(): LiveSeatState {
        return liveSeatStore.liveSeatState
    }

    fun getCoGuestState(): CoGuestState {
        return coGuestStore.coGuestState
    }

    fun getCoHostState(): CoHostState {
        return coHostStore.coHostState
    }

    fun getBattleState(): BattleState {
        return battleStore.battleState
    }

    fun getIMState(): IMState {
        return imStore.imState
    }

    fun getViewState(): ViewState {
        return viewStore.viewState
    }

    fun getMediaState(): MediaState {
        return mediaStore.mediaState
    }

    fun notifyOnRoomDismissed(roomId: String) {
        audienceViewListenerList.notifyListeners { listener -> listener.onRoomDismissed(roomId) }
        audienceContainerViewListenerList?.let { listenerList ->
            val ownerInfo = liveListStore.liveState.currentLive.value
            listenerList.notifyListeners { listener ->
                listener.onLiveEnded(
                    roomId,
                    ownerInfo.liveOwner.userName,
                    ownerInfo.liveOwner.avatarURL
                )
            }
        }
    }

    fun notifyPictureInPictureClick() {
        audienceContainerViewListenerList?.notifyListeners { listener ->
            listener.onPictureInPictureClick()
        }
    }

    interface AudienceViewListener {
        fun onRoomDismissed(roomId: String)
    }
}
