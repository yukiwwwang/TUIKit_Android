package com.trtc.uikit.livekit.component.pippanel

class PIPPanelStore private constructor() {

    val state = PIPPanelState()

    companion object {
        @Volatile
        private var instance: PIPPanelStore? = null

        @JvmStatic
        fun sharedInstance(): PIPPanelStore {
            return instance ?: synchronized(this) {
                instance ?: PIPPanelStore().also { instance = it }
            }
        }
    }

    fun setPictureInPictureModeRoomId(roomId: String) {
        state.roomId.value = roomId
    }

    fun reset() {
        state.roomId.value = ""
        state.anchorIsPictureInPictureMode = false
        state.audienceIsPictureInPictureMode = false
        state.isAnchorStreaming = false
    }
}