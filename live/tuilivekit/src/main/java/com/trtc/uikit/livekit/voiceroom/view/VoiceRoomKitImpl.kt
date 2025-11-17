package com.trtc.uikit.livekit.voiceroom.view

import android.content.Context
import android.content.Intent
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.qcloud.tuicore.TUILogin
import com.trtc.uikit.livekit.voiceroom.VoiceRoomDefine
import com.trtc.uikit.livekit.voiceroom.VoiceRoomKit
import com.trtc.uikit.livekit.voiceroom.view.TUIVoiceRoomFragment.RoomBehavior.AUTO_CREATE
import com.trtc.uikit.livekit.voiceroom.view.TUIVoiceRoomFragment.RoomBehavior.JOIN
import com.trtc.uikit.livekit.voiceroom.view.TUIVoiceRoomFragment.RoomBehavior.PREPARE_CREATE
import java.util.Objects

class VoiceRoomKitImpl private constructor(context: Context) : VoiceRoomKit {

    private val applicationContext: Context = context.applicationContext

    companion object {
        @Volatile
        private var sInstance: VoiceRoomKitImpl? = null

        @Synchronized
        fun createInstance(context: Context): VoiceRoomKitImpl {
            if (sInstance == null) {
                synchronized(VoiceRoomKitImpl::class.java) {
                    if (sInstance == null) {
                        sInstance = VoiceRoomKitImpl(context)
                    }
                }
            }
            return sInstance!!
        }
    }

    override fun createRoom(roomId: String, params: VoiceRoomDefine.CreateRoomParams) {
        val intent = Intent(applicationContext, VoiceRoomActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(VoiceRoomActivity.INTENT_KEY_ROOM_ID, roomId)
        intent.putExtra(VoiceRoomActivity.INTENT_KEY_CREATE_ROOM_PARAMS, params)
        intent.putExtra(VoiceRoomActivity.INTENT_KEY_ROOM_BEHAVIOR, PREPARE_CREATE.ordinal)
        applicationContext.startActivity(intent)
    }

    override fun enterRoom(roomId: String) {
        val intent = Intent(applicationContext, VoiceRoomActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(VoiceRoomActivity.INTENT_KEY_ROOM_ID, roomId)
        intent.putExtra(VoiceRoomActivity.INTENT_KEY_ROOM_BEHAVIOR, JOIN.ordinal)
        applicationContext.startActivity(intent)
    }

    override fun enterRoom(liveInfo: TUILiveListManager.LiveInfo) {
        val intent = Intent(applicationContext, VoiceRoomActivity::class.java)
        val isAnchor = Objects.equals(liveInfo.ownerId, TUILogin.getUserId())
        intent.putExtra(VoiceRoomActivity.INTENT_KEY_ROOM_ID, liveInfo.roomId)
        if (isAnchor) {
            intent.putExtra(VoiceRoomActivity.INTENT_KEY_ROOM_BEHAVIOR, AUTO_CREATE.ordinal)
        } else {
            intent.putExtra(VoiceRoomActivity.INTENT_KEY_ROOM_BEHAVIOR, JOIN.ordinal)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(intent)
    }
}