package com.trtc.uikit.livekit.voiceroom

import android.content.Context
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.trtc.uikit.livekit.voiceroom.view.VoiceRoomKitImpl

interface VoiceRoomKit {

    companion object {
        fun createInstance(context: Context): VoiceRoomKit {
            return VoiceRoomKitImpl.createInstance(context)
        }
    }

    fun createRoom(roomId: String, info: VoiceRoomDefine.CreateRoomParams)

    fun enterRoom(roomId: String)

    fun enterRoom(liveInfo: TUILiveListManager.LiveInfo)
}