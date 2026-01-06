package com.trtc.uikit.livekit.features.anchorboardcast.store.observer

import com.google.gson.Gson
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.anchorboardcast.store.AnchorStore
import java.lang.ref.WeakReference

class RoomEngineObserver(anchorStore: AnchorStore) : TUIRoomObserver() {
    private val logger = LiveKitLogger.getFeaturesLogger("RoomEngineObserver")

    private val anchorStoreRef: WeakReference<AnchorStore> = WeakReference(anchorStore)

    override fun onSeatListChanged(
        roomId: String?,
        seatList: List<TUIRoomDefine.SeatFullInfo>,
        newlySeatedUsers: List<TUIRoomDefine.UserInfo?>?,
        newlyLeftUsers: List<TUIRoomDefine.UserInfo?>?,
    ) {
        logger.info("${hashCode()} onSeatListChanged:roomId:$roomId,[seatList:${Gson().toJson(seatList)}]")
        val anchorStore = anchorStoreRef.get()
        if (anchorStore != null && anchorStore.getState().roomId == roomId) {
            anchorStore.onSeatLockStateChanged(seatList)
        }
    }
}