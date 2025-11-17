package com.trtc.uikit.livekit.livestream.impl

import android.os.Bundle
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo
import io.trtc.tuikit.atomicxcore.api.live.LiveUserInfo
import io.trtc.tuikit.atomicxcore.api.live.TakeSeatMode

object LiveInfoUtils {

    fun convertLiveInfoToBundle(liveInfo: LiveInfo): Bundle {
        return Bundle().apply {
            putString("coverUrl", liveInfo.coverURL)
            putString("backgroundUrl", liveInfo.backgroundURL)

            val categoryList = ArrayList(liveInfo.categoryList)
            putIntegerArrayList("categoryList", categoryList)

            putBoolean("isPublicVisible", liveInfo.isPublicVisible)
            putInt("activityStatus", liveInfo.activityStatus)
            putInt("viewCount", liveInfo.totalViewerCount)
            putString("roomId", liveInfo.liveID)
            putString("ownerId", liveInfo.liveOwner.userID)
            putString("ownerName", liveInfo.liveOwner.userName)
            putString("ownerAvatarUrl", liveInfo.liveOwner.avatarURL)
            putString("name", liveInfo.liveName)
            putBoolean("isMessageDisableForAllUser", liveInfo.isMessageDisable)
            putBoolean("isSeatEnabled", liveInfo.isSeatEnabled)
            putInt("seatMode", liveInfo.seatMode.ordinal)
            putInt("maxSeatCount", liveInfo.maxSeatCount)
            putLong("createTime", liveInfo.createTime)
        }
    }

    fun convertBundleToLiveInfo(liveBundle: Bundle): LiveInfo {
        return LiveInfo(
            liveOwner = LiveUserInfo().apply {
                userID = liveBundle.getString("ownerId", "")
                userName = liveBundle.getString("ownerName", "")
                avatarURL = liveBundle.getString("ownerAvatarUrl", "")
            },
            createTime = liveBundle.getLong("createTime", 0)
        ).apply {
            liveID = liveBundle.getString("roomId", "")

            liveName = liveBundle.getString("name", "")
            isMessageDisable = liveBundle.getBoolean("isMessageDisableForAllUser", false)
            isSeatEnabled = liveBundle.getBoolean("isSeatEnabled", false)
            seatMode = if (liveBundle.getInt("seatMode", 0) == 0) {
                TakeSeatMode.FREE
            } else {
                TakeSeatMode.APPLY
            }
            maxSeatCount = liveBundle.getInt("maxSeatCount", 0)
            coverURL = liveBundle.getString("coverUrl", "")
            backgroundURL = liveBundle.getString("backgroundUrl", "")
            categoryList = liveBundle.getIntegerArrayList("categoryList") ?: emptyList()
            isPublicVisible = liveBundle.getBoolean("isPublicVisible", false)
            activityStatus = liveBundle.getInt("activityStatus", 0)
            totalViewerCount = liveBundle.getInt("viewCount", 0)
        }
    }

    fun TUILiveListManager.LiveInfo.asStoreLiveInfo(): LiveInfo {
        return LiveInfo(
            liveID = this.roomId ?: "",
            liveName = this.name ?: "",
            notice = this.notice ?: "",
            isMessageDisable = this.isMessageDisableForAllUser,
            isPublicVisible = this.isPublicVisible,
            isSeatEnabled = this.isSeatEnabled,
            keepOwnerOnSeat = this.keepOwnerOnSeat,
            maxSeatCount = this.maxSeatCount,
            seatMode = this.seatMode.toTakeSeatMode(),
            seatLayoutTemplateID = this.seatLayoutTemplateId,
            coverURL = this.coverUrl ?: "",
            backgroundURL = this.backgroundUrl ?: "",
            categoryList = this.categoryList ?: emptyList(),
            activityStatus = this.activityStatus,
            liveOwner = LiveUserInfo(
                userID = this.ownerId ?: "",
                userName = this.ownerName ?: "",
                avatarURL = this.ownerAvatarUrl ?: ""
            ),
            createTime = this.createTime,
            totalViewerCount = this.viewCount,
            isGiftEnabled = true,
            metaData = emptyMap()
        )
    }

    fun TUIRoomDefine.SeatMode?.toTakeSeatMode(): TakeSeatMode {
        return when (this) {
            TUIRoomDefine.SeatMode.FREE_TO_TAKE -> TakeSeatMode.FREE
            TUIRoomDefine.SeatMode.APPLY_TO_TAKE -> TakeSeatMode.APPLY
            else -> TakeSeatMode.APPLY
        }
    }

    fun LiveInfo.asEngineLiveInfo(): TUILiveListManager.LiveInfo {
        val javaLiveInfo = TUILiveListManager.LiveInfo()
        javaLiveInfo.roomId = this.liveID
        javaLiveInfo.name = this.liveName
        javaLiveInfo.notice = this.notice
        javaLiveInfo.isMessageDisableForAllUser = this.isMessageDisable
        javaLiveInfo.isPublicVisible = this.isPublicVisible
        javaLiveInfo.isSeatEnabled = this.isSeatEnabled
        javaLiveInfo.keepOwnerOnSeat = this.keepOwnerOnSeat
        javaLiveInfo.maxSeatCount = this.maxSeatCount
        javaLiveInfo.seatMode = this.seatMode.toTUISeatMode()
        javaLiveInfo.seatLayoutTemplateId = this.seatLayoutTemplateID
        javaLiveInfo.coverUrl = this.coverURL
        javaLiveInfo.backgroundUrl = this.backgroundURL
        javaLiveInfo.categoryList = this.categoryList.toMutableList()
        javaLiveInfo.activityStatus = this.activityStatus

        javaLiveInfo.ownerId = this.liveOwner.userID
        javaLiveInfo.ownerName = this.liveOwner.userName
        javaLiveInfo.ownerAvatarUrl = this.liveOwner.avatarURL
        javaLiveInfo.createTime = this.createTime
        javaLiveInfo.viewCount = this.totalViewerCount

        return javaLiveInfo
    }

    fun TakeSeatMode.toTUISeatMode(): TUIRoomDefine.SeatMode {
        return when (this) {
            TakeSeatMode.FREE -> TUIRoomDefine.SeatMode.FREE_TO_TAKE
            TakeSeatMode.APPLY -> TUIRoomDefine.SeatMode.APPLY_TO_TAKE
        }
    }
}