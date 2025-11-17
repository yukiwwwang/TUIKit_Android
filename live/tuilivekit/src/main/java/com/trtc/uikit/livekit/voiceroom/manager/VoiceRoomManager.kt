package com.trtc.uikit.livekit.voiceroom.manager

import com.trtc.uikit.livekit.voiceroom.store.IMStore
import com.trtc.uikit.livekit.voiceroom.store.PrepareStore
import com.trtc.uikit.livekit.voiceroom.store.ViewStore

class VoiceRoomManager {
    val imStore = IMStore()
    val prepareStore = PrepareStore()
    val viewStore = ViewStore()

    fun destroy() {
        imStore.destroy()
        prepareStore.destroy()
        viewStore.destroy()
    }
}