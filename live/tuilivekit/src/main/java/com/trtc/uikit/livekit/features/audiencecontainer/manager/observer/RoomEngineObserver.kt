package com.trtc.uikit.livekit.features.audiencecontainer.manager.observer

import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.EVENT_KEY_LIVE_KIT
import com.trtc.uikit.livekit.common.EVENT_SUB_KEY_DESTROY_LIVE_VIEW
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager
import java.lang.ref.WeakReference

class RoomEngineObserver(manager: AudienceManager) : TUIRoomObserver() {

    companion object {
        private val LOGGER = LiveKitLogger.getLiveStreamLogger("RoomEngineObserver")
    }

    private val liveManager: WeakReference<AudienceManager> = WeakReference(manager)

    override fun onRoomDismissed(roomId: String, reason: TUIRoomDefine.RoomDismissedReason) {
        LOGGER.info("${hashCode()} onRoomDismissed:[roomId$roomId]")
        ToastUtil.toastShortMessage(
            ContextProvider.getApplicationContext().resources
                .getString(R.string.common_room_destroy)
        )
        TUICore.notifyEvent(
            TUIConstants.Privacy.EVENT_ROOM_STATE_CHANGED,
            TUIConstants.Privacy.EVENT_SUB_KEY_ROOM_STATE_STOP,
            null
        )
        val manager = liveManager.get()
        manager?.notifyOnRoomDismissed(roomId)
    }

    override fun onKickedOffLine(message: String) {
        LOGGER.info("${hashCode()} onKickedOffLine:[message:$message]")
        ToastUtil.toastShortMessage(message)
        val manager = liveManager.get()
        TUICore.notifyEvent(
            TUIConstants.Privacy.EVENT_ROOM_STATE_CHANGED,
            TUIConstants.Privacy.EVENT_SUB_KEY_ROOM_STATE_STOP,
            null
        )
        if (manager != null) {
            TUICore.notifyEvent(EVENT_KEY_LIVE_KIT, EVENT_SUB_KEY_DESTROY_LIVE_VIEW, null)
        } else {
            LOGGER.error("${hashCode()} onKickedOffLine: AudienceManager is null")
        }
    }
}
