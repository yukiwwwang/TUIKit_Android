package com.trtc.uikit.livekit.livestream.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Rational
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager.LiveInfo
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.qcloud.tuicore.TUILogin
import com.trtc.tuikit.common.util.ToastUtil
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.DEFAULT_BACKGROUND_URL
import com.trtc.uikit.livekit.common.DEFAULT_COVER_URL
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.livestream.VideoLiveAnchorActivity
import com.trtc.uikit.livekit.livestream.VideoLiveAudienceActivity
import com.trtc.uikit.livekit.livestream.VideoLiveKit
import com.trtc.uikit.livekit.livestream.impl.LiveInfoUtils.asStoreLiveInfo
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

class VideoLiveKitImpl private constructor(context: Context) : VideoLiveKit {
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance: VideoLiveKitImpl? = null
        
        @JvmStatic
        fun createInstance(context: Context): VideoLiveKitImpl {
            return sInstance ?: synchronized(VideoLiveKitImpl::class.java) {
                sInstance ?: VideoLiveKitImpl(context).also { sInstance = it }
            }
        }
    }

    private val logger = LiveKitLogger.getFeaturesLogger("VideoLiveKitImpl")
    private val context: Context = context.applicationContext
    private var audioModeListener: AudioModeListener? = null
    private var phoneListener: PhoneCallStateListener? = null
    private var audioManager: AudioManager? = null
    private var telephonyManager: TelephonyManager? = null
    private var audioModeListenerRegistered = false

    private val callingAPIListenerList = CopyOnWriteArrayList<WeakReference<CallingAPIListener>>()

    init {
        initAudioModeListener(this.context)
    }

    private fun initAudioModeListener(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioModeListener = AudioModeListener()
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            phoneListener = null
            telephonyManager = null
        } else {
            audioModeListener = null
            audioManager = null
            phoneListener = PhoneCallStateListener()
            telephonyManager = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        }
    }

    override fun startLive(roomId: String) {
        logger.info("startLive, roomId:$roomId")
        
        val startTask = Runnable {
            val intent = Intent(context, VideoLiveAnchorActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(VideoLiveAnchorActivity.INTENT_KEY_ROOM_ID, roomId)
            }
            context.startActivity(intent)
        }
        
        val liveListManager = TUIRoomEngine.sharedInstance()
            .getExtension(TUICommonDefine.ExtensionType.LIVE_LIST_MANAGER) as TUILiveListManager
            
        liveListManager.getLiveInfo(roomId, object : TUILiveListManager.LiveInfoCallback {
            override fun onSuccess(liveInfo: LiveInfo) {
                logger.info("getLiveInfo, onSuccess, liveInfo:${Gson().toJson(liveInfo)}")
                if (liveInfo.keepOwnerOnSeat) {
                    startTask.run()
                } else {
                    joinLive(liveInfo.asStoreLiveInfo())
                }
            }

            override fun onError(error: TUICommonDefine.Error, message: String) {
                logger.warn("getLiveInfo onError:$message")
                startTask.run()
            }
        })
    }

    override fun joinLive(roomId: String) {
        val liveInfo = io.trtc.tuikit.atomicxcore.api.live.LiveInfo().apply {
            liveID = roomId
            backgroundURL = DEFAULT_BACKGROUND_URL
            coverURL = DEFAULT_COVER_URL
            isPublicVisible = true
        }
        
        val intent = Intent(context, VideoLiveAudienceActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtras(LiveInfoUtils.convertLiveInfoToBundle(liveInfo))
        }
        context.startActivity(intent)
    }

    override fun joinLive(liveInfo: io.trtc.tuikit.atomicxcore.api.live.LiveInfo) {
        if (TextUtils.isEmpty(liveInfo.liveID)) {
            return
        }

        val intent = if (liveInfo.liveOwner.userID == TUILogin.getUserId()) {
            Intent(context, VideoLiveAnchorActivity::class.java).apply {
                putExtra(VideoLiveAnchorActivity.INTENT_KEY_NEED_CREATE, false)
            }
        } else {
            Intent(context, VideoLiveAudienceActivity::class.java)
        }

        intent.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtras(LiveInfoUtils.convertLiveInfoToBundle(liveInfo))
        }
        context.startActivity(intent)
    }

    override fun leaveLive(callback: TUIRoomDefine.ActionCallback?) {
        TUIRoomEngine.sharedInstance().exitRoom(true, callback)
        notifyLeaveLive()
    }

    override fun stopLive(callback: TUIRoomDefine.ActionCallback?) {
        TUIRoomEngine.sharedInstance().destroyRoom(callback)
        notifyStopLive()
    }

    fun enterPictureInPictureMode(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appOpsManager = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            if (AppOpsManager.MODE_ALLOWED == appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    activity.applicationInfo.uid, 
                    activity.packageName
                )) {
                val aspectRatio = Rational(9, 16)
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()
                activity.enterPictureInPictureMode(params)
                true
            } else {
                val intent = Intent(
                    "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                    Uri.parse("package:${activity.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
                logger.warn("Picture in Picture not permission")
                false
            }
        } else {
            ToastUtil.toastShortMessage(activity.getString(R.string.common_picture_in_picture_android_system_tips))
            logger.warn("Picture-in-picture mode is not supported under android 8.0 lower version")
            false
        }
    }

    fun addCallingAPIListener(listener: CallingAPIListener?) {
        listener?.let {
            callingAPIListenerList.add(WeakReference(it))
        }
    }

    fun removeCallingAPIListener(listener: CallingAPIListener?) {
        callingAPIListenerList.removeAll { it.get() == listener }
    }

    fun startPushLocalVideoOnResume() {
        val isInCall = isInCall()
        if (isInCall) {
            stopPushLocalVideo()
        } else {
            startPushLocalVideo()
        }
        startListeningPhoneState()
    }

    fun stopPushLocalVideoOnStop() {
        stopPushLocalVideo()
        stopListeningPhoneState()
    }

    private fun startListeningPhoneState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioModeListener?.let { listener ->
                if (!audioModeListenerRegistered) {
                    audioManager?.addOnModeChangedListener(context.mainExecutor, listener)
                    audioModeListenerRegistered = true
                }
            }
        } else {
            telephonyManager?.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun stopListeningPhoneState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioModeListener?.let { listener ->
                if (audioModeListenerRegistered) {
                    audioManager?.removeOnModeChangedListener(listener)
                    audioModeListenerRegistered = false
                }
            }
        } else {
            telephonyManager?.listen(phoneListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    private fun startPushLocalVideo() {
        TUIRoomEngine.sharedInstance().startPushLocalVideo()
    }

    fun stopPushLocalVideo() {
        TUIRoomEngine.sharedInstance().stopPushLocalVideo()
    }

    private fun isInCall(): Boolean {
        return audioManager?.let { manager ->
            manager.mode == AudioManager.MODE_IN_CALL
        } ?: run {
            telephonyManager?.callState == TelephonyManager.CALL_STATE_OFFHOOK
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private inner class AudioModeListener : AudioManager.OnModeChangedListener {
        override fun onModeChanged(mode: Int) {
            logger.info("onModeChanged, mode:$mode")
            when (mode) {
                AudioManager.MODE_IN_CALL -> stopPushLocalVideo()
                AudioManager.MODE_NORMAL -> startPushLocalVideo()
                else -> startPushLocalVideo()
            }
        }
    }

    private inner class PhoneCallStateListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            super.onCallStateChanged(state, incomingNumber)
            logger.info("onCallStateChanged, state:$state, incomingNumber:$incomingNumber")
            when (state) {
                TelephonyManager.CALL_STATE_OFFHOOK -> stopPushLocalVideo()
                TelephonyManager.CALL_STATE_IDLE -> startPushLocalVideo()
                else -> startPushLocalVideo()
            }
        }
    }

    interface CallingAPIListener {
        fun onLeaveLive()
        fun onStopLive()
    }

    private fun notifyLeaveLive() {
        callingAPIListenerList.forEach { listenerRef ->
            listenerRef.get()?.onLeaveLive()
        }
    }

    private fun notifyStopLive() {
        callingAPIListenerList.forEach { listenerRef ->
            listenerRef.get()?.onStopLive()
        }
    }
}