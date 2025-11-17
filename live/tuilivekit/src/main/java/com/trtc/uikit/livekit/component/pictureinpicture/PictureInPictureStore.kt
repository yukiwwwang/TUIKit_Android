package com.trtc.uikit.livekit.component.pictureinpicture

import com.trtc.uikit.livekit.common.MutableLiveDataUtils.setValue

class PictureInPictureStore private constructor() {

    val state = PictureInPictureState()

    companion object {
        @Volatile
        private var instance: PictureInPictureStore? = null

        @JvmStatic
        fun sharedInstance(): PictureInPictureStore {
            return instance ?: synchronized(this) {
                instance ?: PictureInPictureStore().also { instance = it }
            }
        }
    }

    fun setPictureInPictureModeRoomId(roomId: String) {
        setValue(state.roomId, roomId)
    }

    fun reset() {
        state.roomId.value = ""
        state.anchorIsPictureInPictureMode = false
        state.audienceIsPictureInPictureMode = false
        state.isAnchorStreaming = false
    }
}