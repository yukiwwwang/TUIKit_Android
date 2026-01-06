package com.trtc.uikit.livekit.livestream

import android.content.Context
import com.trtc.uikit.livekit.livestream.impl.VideoLiveKitImpl
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo
import io.trtc.tuikit.atomicxcore.api.live.StopLiveCompletionHandler

interface VideoLiveKit {

    companion object {
        fun createInstance(context: Context): VideoLiveKit {
            return VideoLiveKitImpl.createInstance(context)
        }
    }

    fun startLive(roomId: String)

    fun stopLive(callback: StopLiveCompletionHandler?)

    fun joinLive(roomId: String)

    fun joinLive(liveInfo: LiveInfo)

    fun leaveLive(callback: CompletionHandler?)
}