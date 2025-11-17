package com.trtc.uikit.livekit.component.networkInfo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Build
import android.text.TextUtils
import androidx.lifecycle.Observer
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudListener
import com.tencent.trtc.TRTCStatistics
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.component.networkInfo.store.NetworkInfoState

class NetworkInfoService(context: Context) {

    companion object {
        private const val NETWORK_WEAK_THRESHOLD_MS = 30_000L
        private const val LOW_FRAME_RATE_THRESHOLD = 15
        private const val LOW_BITRATE_THRESHOLD_240P = 100
        private const val LOW_BITRATE_THRESHOLD_360P = 200
        private const val LOW_BITRATE_THRESHOLD_480P = 350
        private const val LOW_BITRATE_THRESHOLD_540x540 = 500
        private const val LOW_BITRATE_THRESHOLD_540x960 = 800
        private const val LOW_BITRATE_THRESHOLD_1080P = 1500
    }

    private val logger = LiveKitLogger.getComponentLogger("NetworkInfoService")
    private val appContext: Context = context.applicationContext
    private val userId: String = TUIRoomEngine.getSelfInfo().userId
    val networkInfoState = NetworkInfoState()
    private val trtcCloud: TRTCCloud = TUIRoomEngine.sharedInstance().trtcCloud
    private val tuiRoomEngine: TUIRoomEngine = TUIRoomEngine.sharedInstance()
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var networkReceiver: BroadcastReceiver? = null
    private var networkBadStartTime: Long = 0

    private val networkConnectionObserver = Observer<Boolean> { isConnection ->
        onNetworkConnectionChange(isConnection)
    }

    private val engineObserver = object : TUIRoomObserver() {
        override fun onUserNetworkQualityChanged(networkMap: Map<String, TUICommonDefine.NetworkInfo>) {
            handleUserNetworkQualityChange(networkMap)
        }

        override fun onUserVideoStateChanged(
            userId: String,
            streamType: TUIRoomDefine.VideoStreamType,
            hasVideo: Boolean,
            reason: TUIRoomDefine.ChangeReason
        ) {
            handleVideoStateChanged(userId, hasVideo)
        }

        override fun onUserAudioStateChanged(
            userId: String,
            hasAudio: Boolean,
            reason: TUIRoomDefine.ChangeReason
        ) {
            handleAudioStateChanged(userId, hasAudio)
        }

        override fun onSeatListChanged(
            seatList: List<TUIRoomDefine.SeatInfo>,
            seatedList: List<TUIRoomDefine.SeatInfo>,
            leftList: List<TUIRoomDefine.SeatInfo>
        ) {
            handleSeatListChanged(seatList)
        }

        override fun onRoomDismissed(roomId: String, reason: TUIRoomDefine.RoomDismissedReason) {
            handleRoomDismissed()
        }
    }

    private val mTRTCObserver = object : TRTCCloudListener() {
        override fun onStatistics(statistics: TRTCStatistics) {
            handleLocalStreamStatistics(statistics)
        }
    }

    fun initAudioCaptureVolume() {
        logger.info("initAudioCaptureVolume:[]")
        networkInfoState.audioCaptureVolume.value = trtcCloud.audioCaptureVolume
    }

    fun setAudioCaptureVolume(volume: Int) {
        logger.info("setAudioCaptureVolume:[volume:$volume]")
        networkInfoState.audioCaptureVolume.value = volume
        trtcCloud.setAudioCaptureVolume(volume)
    }

    fun updateAudioStatusByVolume(volume: Int) {
        logger.info("updateAudioStatusByVolume:[volume:$volume]")
        if (networkInfoState.audioStatus.value != NetworkInfoState.Status.Closed) {
            networkInfoState.audioStatus.value = if (volume == 0) {
                NetworkInfoState.Status.Mute
            } else {
                NetworkInfoState.Status.Normal
            }
        }
    }

    fun updateAudioMode(audioQuality: TUIRoomDefine.AudioQuality) {
        logger.info("updateAudioMode:[audioQuality:$audioQuality]")
        networkInfoState.audioMode.value = audioQuality
        tuiRoomEngine.updateAudioQuality(audioQuality)
    }

    fun checkDeviceTemperature(context: Context) {
        logger.info("checkDeviceTemperature:[context:$context]")
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        intent?.let {
            val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val temperature = temp / 10.0f
            networkInfoState.isDeviceThermal = temperature > 45.0f
        }
    }

    fun addObserver() {
        startListenNetworkConnection()
        trtcCloud.addListener(mTRTCObserver)
        tuiRoomEngine.addObserver(engineObserver)
        networkInfoState.isNetworkConnected.observeForever(networkConnectionObserver)
    }

    fun removeObserver() {
        stopListenNetworkConnection()
        trtcCloud.removeListener(mTRTCObserver)
        tuiRoomEngine.removeObserver(engineObserver)
        networkInfoState.isNetworkConnected.removeObserver(networkConnectionObserver)
    }

    private fun handleUserNetworkQualityChange(networkMap: Map<String, TUICommonDefine.NetworkInfo>) {
        if (networkInfoState.isNetworkConnected.value == false) {
            return
        }
        val info = networkMap[userId]
        info?.let {
            updateNetworkInfo(it)
            checkAndShowNetworkWeakTips(it)
        }
    }

    private fun handleAudioStateChanged(userId: String, hasAudio: Boolean) {
        if (TextUtils.equals(userId, this@NetworkInfoService.userId)) {
            networkInfoState.audioStatus.value = if (hasAudio &&
                networkInfoState.audioStatus.value != NetworkInfoState.Status.Mute) {
                NetworkInfoState.Status.Normal
            } else {
                NetworkInfoState.Status.Closed
            }
        }
    }

    private fun handleSeatListChanged(seatList: List<TUIRoomDefine.SeatInfo>?) {
        if (seatList.isNullOrEmpty()) {
            return
        }
        for (seat in seatList) {
            if (TextUtils.equals(seat.userId, userId)) {
                networkInfoState.isTakeInSeat.value = true
                return
            }
        }
        networkInfoState.isTakeInSeat.value = false
    }

    private fun handleRoomDismissed() {
        networkInfoState.roomDismissed.value = true
    }

    private fun handleVideoStateChanged(userId: String, hasVideo: Boolean) {
        if (TextUtils.equals(userId, this@NetworkInfoService.userId)) {
            networkInfoState.videoStatus.value = if (hasVideo) {
                NetworkInfoState.Status.Normal
            } else {
                NetworkInfoState.Status.Closed
            }
        }
    }

    private fun handleLocalStreamStatistics(statistics: TRTCStatistics) {
        val localStreamStats = getLocalStreamStats(statistics) ?: return
        updateResolution(localStreamStats)
        updateVideoStatus(localStreamStats)
        updateAudioStatus(localStreamStats)
    }

    private fun updateNetworkInfo(info: TUICommonDefine.NetworkInfo) {
        networkInfoState.rtt.value = info.delay
        networkInfoState.downLoss.value = info.downLoss
        networkInfoState.upLoss.value = info.upLoss
        networkInfoState.networkStatus.value = info.quality
    }

    private fun checkAndShowNetworkWeakTips(info: TUICommonDefine.NetworkInfo) {
        val isNetworkWeak = (info.quality == TUICommonDefine.NetworkQuality.BAD || 
                           info.quality == TUICommonDefine.NetworkQuality.VERY_BAD || 
                           info.quality == TUICommonDefine.NetworkQuality.DOWN)
        val currentTime = System.currentTimeMillis()
        
        if (isNetworkWeak) {
            if (networkBadStartTime == 0L) {
                networkBadStartTime = currentTime
            } else if (currentTime - networkBadStartTime >= NETWORK_WEAK_THRESHOLD_MS) {
                networkInfoState.isDisplayNetworkWeakTips.value = true
                networkBadStartTime = currentTime
            }
        } else {
            networkBadStartTime = 0
        }
        
        val networkStatus = networkInfoState.networkStatus.value
        if (networkStatus == TUICommonDefine.NetworkQuality.VERY_BAD || 
            networkStatus == TUICommonDefine.NetworkQuality.DOWN) {
            networkInfoState.videoStatus.value = NetworkInfoState.Status.Abnormal
        }
    }

    private fun getLocalStreamStats(statistics: TRTCStatistics): TRTCStatistics.TRTCLocalStatistics? {
        for (stat in statistics.localArray) {
            if (stat.streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG) {
                return stat
            }
        }
        return null
    }

    private fun updateResolution(statistics: TRTCStatistics.TRTCLocalStatistics) {
        networkInfoState.resolution.value = "${statistics.width} P"
    }

    private fun updateVideoStatus(statistics: TRTCStatistics.TRTCLocalStatistics) {
        if (networkInfoState.videoStatus.value == NetworkInfoState.Status.Closed) {
            return
        }
        
        val networkStatus = networkInfoState.networkStatus.value
        if (networkStatus == TUICommonDefine.NetworkQuality.VERY_BAD || 
            networkStatus == TUICommonDefine.NetworkQuality.DOWN) {
            networkInfoState.videoStatus.value = NetworkInfoState.Status.Abnormal
            return
        }
        
        networkInfoState.videoStatus.value = if (statistics.frameRate < LOW_FRAME_RATE_THRESHOLD ||
                                                 isBitrateAbnormal(statistics)) {
            NetworkInfoState.Status.Abnormal
        } else {
            NetworkInfoState.Status.Normal
        }
    }

    private fun isBitrateAbnormal(statistics: TRTCStatistics.TRTCLocalStatistics): Boolean {
        val width = statistics.width
        val height = statistics.height
        val bitrate = statistics.videoBitrate
        
        return when (width) {
            240 -> bitrate < LOW_BITRATE_THRESHOLD_240P
            360 -> bitrate < LOW_BITRATE_THRESHOLD_360P
            480 -> bitrate < LOW_BITRATE_THRESHOLD_480P
            540 -> when (height) {
                540 -> bitrate < LOW_BITRATE_THRESHOLD_540x540
                960 -> bitrate < LOW_BITRATE_THRESHOLD_540x960
                else -> false
            }
            1080 -> bitrate < LOW_BITRATE_THRESHOLD_1080P
            else -> false
        }
    }

    private fun updateAudioStatus(statistics: TRTCStatistics.TRTCLocalStatistics) {
        val currentStatus = networkInfoState.audioStatus.value
        if (currentStatus == NetworkInfoState.Status.Closed || 
            currentStatus == NetworkInfoState.Status.Mute) {
            return
        }
        
        networkInfoState.audioStatus.value = if (statistics.audioCaptureState == 0) {
            NetworkInfoState.Status.Normal
        } else {
            NetworkInfoState.Status.Abnormal
        }
    }

    private fun startListenNetworkConnection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerNetworkCallback()
        } else {
            registerBroadcastReceiver()
        }
    }

    private fun stopListenNetworkConnection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            networkCallback?.let { cm?.unregisterNetworkCallback(it) }
        } else {
            networkReceiver?.let { appContext.unregisterReceiver(it) }
        }
    }

    private fun registerNetworkCallback() {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                logger.info("network connected")
                networkInfoState.isNetworkConnected.postValue(true)
            }

            override fun onLost(network: Network) {
                logger.info("network disconnected")
                networkInfoState.isNetworkConnected.postValue(false)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, networkCallback!!)
    }

    private fun registerBroadcastReceiver() {
        networkReceiver = object : BroadcastReceiver() {
            private var lastConnected = false

            override fun onReceive(context: Context, intent: Intent) {
                val connected = isNetworkAvailable(context)
                if (connected != lastConnected) {
                    lastConnected = connected
                    networkInfoState.isNetworkConnected.postValue(connected)
                    logger.info("network ${if (connected) "connected" else "disconnected"}")
                }
            }
        }
        
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        appContext.registerReceiver(networkReceiver, filter)
    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        context ?: return false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }

    private fun onNetworkConnectionChange(isConnection: Boolean?) {
        if (isConnection == false) {
            networkInfoState.networkStatus.value = TUICommonDefine.NetworkQuality.DOWN
            networkInfoState.videoStatus.value = NetworkInfoState.Status.Abnormal
        }
    }
}