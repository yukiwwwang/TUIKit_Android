package com.trtc.uikit.livekit.features.anchorboardcast.manager.observer

import com.google.gson.Gson
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import java.lang.ref.WeakReference

class RoomEngineObserver(manager: AnchorManager) : TUIRoomObserver() {
    companion object {
        private val LOGGER = LiveKitLogger.getFeaturesLogger("RoomEngineObserver")
    }

    private val mLiveManager = WeakReference(manager)

    override fun onSeatListChanged(
        seatList: List<TUIRoomDefine.SeatInfo>,
        seatedList: List<TUIRoomDefine.SeatInfo>,
        leftList: List<TUIRoomDefine.SeatInfo>
    ) {
        LOGGER.info(
            "${hashCode()} onSeatListChanged:[seatList:${Gson().toJson(seatList)},seatedList:${
                Gson().toJson(
                    seatedList
                )
            },leftList:${Gson().toJson(leftList)}]"
        )
        mLiveManager.get()?.onSeatLockStateChanged(seatList)
    }

    override fun onRemoteUserEnterRoom(roomId: String, userInfo: TUIRoomDefine.UserInfo) {
        LOGGER.info("${hashCode()} onRemoteUserEnterRoom:[roomId:$roomId,userId:${userInfo.userId}]")
        mLiveManager.get()?.getUserManager()?.onRemoteUserEnterRoom(roomId, userInfo)
    }

    override fun onRemoteUserLeaveRoom(roomId: String, userInfo: TUIRoomDefine.UserInfo) {
        LOGGER.info("${hashCode()} onRemoteUserLeaveRoom:[roomId:$roomId,userId:${userInfo.userId}]")
        mLiveManager.get()?.getUserManager()?.onRemoteUserLeaveRoom(roomId, userInfo)
    }

    override fun onUserInfoChanged(
        userInfo: TUIRoomDefine.UserInfo,
        modifyFlag: List<TUIRoomDefine.UserInfoModifyFlag>
    ) {
        LOGGER.info(
            "${hashCode()}onUserInfoChanged:[userInfo:${Gson().toJson(userInfo)}, modifyFlag:${
                Gson().toJson(
                    modifyFlag
                )
            }"
        )
        mLiveManager.get()?.getUserManager()?.onUserInfoChanged(userInfo, modifyFlag)
    }

    override fun onSendMessageForUserDisableChanged(roomId: String, userId: String, isDisable: Boolean) {
        LOGGER.info("${hashCode()} onSendMessageForUserDisableChanged:[roomId:$roomId,userId:$userId,isDisable:$isDisable]")
        mLiveManager.get()?.getUserManager()?.onSendMessageForUserDisableChanged(roomId, userId, isDisable)
    }

    override fun onError(errorCode: TUICommonDefine.Error, message: String) {
        LOGGER.info("${hashCode()} onError:[errorCode:$errorCode,message:$message]")
        mLiveManager.get()?.getMediaManager()?.onError(errorCode, errorCode)
    }
}