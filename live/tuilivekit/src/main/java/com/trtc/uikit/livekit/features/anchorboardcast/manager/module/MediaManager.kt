package com.trtc.uikit.livekit.features.anchorboardcast.manager.module

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.TypedValue
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.common.TUIVideoView
import com.tencent.qcloud.tuicore.TUICore
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.component.beauty.tebeauty.TEBeautyManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.api.IAnchorAPI
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorState
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.DeviceControlPolicy
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore

class MediaManager(state: AnchorState, service: IAnchorAPI) : BaseManager(state, service) {
    private val logger = LiveKitLogger.getFeaturesLogger("MediaManager")

    enum class MediaDevice {
        MICROPHONE,
        CAMERA
    }

    override fun destroy() {
        logger.info("destroy")
        releaseVideoMuteBitmap()
        enableMultiPlaybackQuality(false)
    }

    fun setLocalVideoView(view: TUIVideoView) {
        liveService.setLocalVideoView(view)
    }

    fun enableMultiPlaybackQuality(enable: Boolean) {
        val params = hashMapOf<String, Any>("enable" to enable)
        TUICore.callService("AdvanceSettingManager", "enableMultiPlaybackQuality", params)
    }

    fun createVideoMuteBitmap(context: Context, bigResId: Int, smallResId: Int) {
        if (mediaState.bigMuteBitmap == null) {
            mediaState.bigMuteBitmap = createMuteBitmap(context, bigResId)
        }
        if (mediaState.smallMuteBitmap == null) {
            mediaState.smallMuteBitmap = createMuteBitmap(context, smallResId)
        }
    }

    private fun releaseVideoMuteBitmap() {
        mediaState.bigMuteBitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        mediaState.smallMuteBitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        mediaState.bigMuteBitmap = null
        mediaState.smallMuteBitmap = null
    }

    private fun createMuteBitmap(context: Context, resId: Int): Bitmap {
        val tv = TypedValue()
        context.resources.openRawResource(resId, tv)
        val opt = BitmapFactory.Options().apply {
            inDensity = tv.density
            inScaled = false
        }
        return BitmapFactory.decodeResource(context.resources, resId, opt)
    }

    fun setCustomVideoProcess() {
        TEBeautyManager.setCustomVideoProcess()
    }

    fun enablePipMode(enable: Boolean) {
        mediaState.isPipModeEnabled.value = enable
    }

    fun onError(errorCode: TUICommonDefine.Error, errorCode1: TUICommonDefine.Error) {
        if (TUICommonDefine.Error.CAMERA_OCCUPIED == errorCode) {
            mediaState.isCameraOccupied = true
        }
    }

    fun resetCameraOccupied() {
        mediaState.isCameraOccupied = false
    }

    fun disableUserMediaDevice(userId: String, device: MediaDevice, isDisable: Boolean) {
        val liveId = LiveListStore.shared().liveState.currentLive.value.liveID
        if (liveId.isEmpty()) {
            return
        }
        if (device == MediaDevice.CAMERA) {
            if (isDisable) {
                LiveSeatStore.create(liveId).openRemoteCamera(
                    userId, DeviceControlPolicy.UNLOCK_ONLY,
                    object : CompletionHandler {
                        override fun onSuccess() {
                            logger.info("openRemoteCamera:[success]")
                        }

                        override fun onFailure(code: Int, desc: String) {
                            logger.error("openRemoteCamera failed:code:$code,desc:$desc")
                            ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                        }

                    })
            } else {
                LiveSeatStore.create(liveId).closeRemoteCamera(
                    userId,
                    object : CompletionHandler {
                        override fun onSuccess() {
                            logger.info("closeRemoteCamera:[success]")
                        }

                        override fun onFailure(code: Int, desc: String) {
                            logger.error("closeRemoteCamera failed:code:$code,desc:$desc")
                            ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                        }

                    })
            }
        } else {
            if (isDisable) {
                LiveSeatStore.create(liveId).openRemoteMicrophone(
                    userId, DeviceControlPolicy.UNLOCK_ONLY,
                    object : CompletionHandler {
                        override fun onSuccess() {
                            logger.info("openRemoteMicrophone:[success]")
                        }

                        override fun onFailure(code: Int, desc: String) {
                            logger.error("openRemoteMicrophone failed:code:$code,desc:$desc")
                            ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                        }

                    })
            } else {
                LiveSeatStore.create(liveId).closeRemoteMicrophone(
                    userId,
                    object : CompletionHandler {
                        override fun onSuccess() {
                            logger.info("closeRemoteMicrophone:[success]")
                        }

                        override fun onFailure(code: Int, desc: String) {
                            logger.error("closeRemoteMicrophone failed:code:$code,desc:$desc")
                            ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                        }

                    })
            }
        }

    }
}