package com.trtc.uikit.livekit.features.audiencecontainer.view.coguest.panel

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.trtc.tuikit.common.permission.PermissionCallback
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.PermissionRequest
import com.trtc.uikit.livekit.common.completionHandler
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager

@SuppressLint("ViewConstructor")
class TypeSelectDialog(
    context: Context,
    private val audienceManager: AudienceManager,
    private val seatIndex: Int
) : PopupDialog(context), AudienceManager.AudienceViewListener {

    companion object {
        private val LOGGER = LiveKitLogger.getLiveStreamLogger("TypeSelectDialog")
    }

    private lateinit var imageLinkSettings: ImageView
    private lateinit var layoutLinkVideo: ConstraintLayout
    private lateinit var layoutLinkAudio: ConstraintLayout

    init {
        initView()
    }

    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.livekit_dialog_link_mic_selector, null)
        bindViewId(view)

        initLinkSettingsView()
        initLinkVideoView()
        initLinkAudioView()

        setView(view)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        audienceManager.addAudienceViewListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        audienceManager.removeAudienceViewListener(this)
    }

    private fun bindViewId(view: android.view.View) {
        imageLinkSettings = view.findViewById(R.id.iv_link_settings)
        layoutLinkVideo = view.findViewById(R.id.cl_link_video)
        layoutLinkAudio = view.findViewById(R.id.cl_link_audio)
    }

    private fun initLinkAudioView() {
        layoutLinkAudio.setOnClickListener { view ->
            if (!view.isEnabled) {
                return@setOnClickListener
            }
            view.isEnabled = false
            applyLinkMic(false)
        }
    }

    private fun initLinkVideoView() {
        layoutLinkVideo.setOnClickListener { view ->
            if (!view.isEnabled) {
                return@setOnClickListener
            }
            view.isEnabled = false
            applyLinkMic(true)
        }
    }

    private fun initLinkSettingsView() {
        imageLinkSettings.setOnClickListener {
            val settingsDialog = VideoCoGuestSettingsDialog(context, audienceManager)
            settingsDialog.show()
            dismiss()
        }
    }

    private fun applyLinkMic(openCamera: Boolean) {
        ToastUtil.toastShortMessageCenter(context.getString(R.string.common_toast_apply_link_mic))
        PermissionRequest.requestMicrophonePermissions(
            ContextProvider.getApplicationContext(),
            object : PermissionCallback() {
                override fun onGranted() {
                    if (openCamera) {
                        PermissionRequest.requestCameraPermissions(
                            ContextProvider.getApplicationContext(),
                            object : PermissionCallback() {
                                override fun onGranted() {
                                    LOGGER.info("requestCameraPermissions:[onGranted]")
                                    audienceManager.getViewStore()
                                        .updateTakeSeatState(true)
                                    audienceManager.getViewStore().updateOpenCameraAfterTakeSeatState(openCamera)
                                    audienceManager.getCoGuestStore().applyForSeat(
                                        seatIndex, 60, openCamera.toString(),
                                        completionHandler {
                                            onSuccess {
                                                audienceManager.getViewStore()
                                                    .updateTakeSeatState(false)
                                            }
                                            onError { code, _ ->
                                                audienceManager.getViewStore()
                                                    .updateTakeSeatState(false)
                                                ErrorLocalized.onError(
                                                    TUICommonDefine.Error.fromInt(
                                                        code
                                                    )
                                                )
                                            }
                                        })
                                }

                                override fun onDenied() {
                                    LOGGER.error("requestCameraPermissions:[onDenied]")
                                }
                            })
                    } else {
                        audienceManager.getViewStore()
                            .updateTakeSeatState(true)
                        audienceManager.getViewStore().updateOpenCameraAfterTakeSeatState(openCamera)
                        audienceManager.getCoGuestStore().applyForSeat(
                            seatIndex, 60, openCamera.toString(),
                            completionHandler {
                                onSuccess {
                                    audienceManager.getViewStore()
                                        .updateTakeSeatState(false)
                                }
                                onError { code, _ ->
                                    audienceManager.getViewStore()
                                        .updateTakeSeatState(false)
                                    ErrorLocalized.onError(
                                        TUICommonDefine.Error.fromInt(
                                            code
                                        )
                                    )
                                }
                            })
                    }
                }

                override fun onDenied() {
                    LOGGER.error("requestCameraPermissions:[onDenied]")
                }
            })
        dismiss()
    }

    override fun onRoomDismissed(roomId: String) {
        dismiss()
    }
}
