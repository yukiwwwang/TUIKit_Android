package com.trtc.uikit.livekit.component.pictureinpicture

import androidx.lifecycle.MutableLiveData

class PictureInPictureState {
    val roomId = MutableLiveData("")
    var anchorIsPictureInPictureMode = false
    var audienceIsPictureInPictureMode = false
    var isAnchorStreaming = false
}