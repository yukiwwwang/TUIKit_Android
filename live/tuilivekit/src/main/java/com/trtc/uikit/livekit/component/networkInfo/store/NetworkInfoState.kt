package com.trtc.uikit.livekit.component.networkInfo.store

import androidx.lifecycle.MutableLiveData
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine

class NetworkInfoState {
    val videoStatus = MutableLiveData(Status.Normal)
    val audioStatus = MutableLiveData(Status.Normal)
    val isTakeInSeat = MutableLiveData(false)
    val isNetworkConnected = MutableLiveData(true)
    val networkStatus = MutableLiveData(TUICommonDefine.NetworkQuality.UNKNOWN)
    val resolution = MutableLiveData("")
    val audioMode = MutableLiveData(TUIRoomDefine.AudioQuality.DEFAULT)
    val audioCaptureVolume = MutableLiveData(0)
    val createTime = MutableLiveData(0L)
    var isDeviceThermal: Boolean = false
    val isDisplayNetworkWeakTips = MutableLiveData(false)
    val rtt = MutableLiveData(0)
    val upLoss = MutableLiveData(0)
    val downLoss = MutableLiveData(0)
    val roomDismissed = MutableLiveData(false)

    enum class Status {
        Mute,
        Closed,
        Normal,
        Abnormal
    }
}