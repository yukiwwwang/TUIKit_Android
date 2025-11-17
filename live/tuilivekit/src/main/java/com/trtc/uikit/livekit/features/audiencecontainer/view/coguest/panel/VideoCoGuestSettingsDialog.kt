package com.trtc.uikit.livekit.features.audiencecontainer.view.coguest.panel

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.common.TUIVideoView
import com.tencent.qcloud.tuicore.util.ScreenUtil
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.trtc.tuikit.common.permission.PermissionCallback
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.PermissionRequest
import com.trtc.uikit.livekit.common.completionHandler
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.common.ui.RoundFrameLayout
import com.trtc.uikit.livekit.component.beauty.BeautyUtils
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager

@SuppressLint("ViewConstructor")
class VideoCoGuestSettingsDialog(
    context: Context,
    private val audienceManager: AudienceManager
) : PopupDialog(context), AudienceManager.AudienceViewListener {

    companion object {
        private val LOGGER = LiveKitLogger.getLiveStreamLogger("VideoCoGuestSettingsDialog")
    }

    private lateinit var roundFrameLayout: RoundFrameLayout
    private lateinit var previewVideoView: TUIVideoView
    private lateinit var buttonApplyLinkMic: Button
    private lateinit var recycleSettingsOption: RecyclerView

    init {
        initView()
    }

    @SuppressLint("InflateParams")
    protected fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.livekit_dialog_link_video_settings, null)
        bindViewId(view)

        initRecycleSettingsOption()
        initPreviewVideoView()
        initApplyLinkMicButton()
        initRoundFrameLayout()

        setView(view)
    }

    private fun bindViewId(view: android.view.View) {
        previewVideoView = view.findViewById(R.id.preview_audience_video)
        buttonApplyLinkMic = view.findViewById(R.id.btn_apply_link_mic)
        recycleSettingsOption = view.findViewById(R.id.video_settings_options)
        roundFrameLayout = view.findViewById(R.id.fl_preview_audience_video)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        audienceManager.addAudienceViewListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        audienceManager.removeAudienceViewListener(this)
        audienceManager.getDeviceStore().closeLocalCamera()
    }

    private fun initRoundFrameLayout() {
        roundFrameLayout.setRadius(ScreenUtil.dip2px(16f))
    }

    private fun initApplyLinkMicButton() {
        buttonApplyLinkMic.setOnClickListener { view ->
            if (!view.isEnabled) {
                return@setOnClickListener
            }
            view.isEnabled = false
            ToastUtil.toastShortMessageCenter(context.getString(R.string.common_toast_apply_link_mic))
            LOGGER.info("requestMicrophonePermissions success")
            PermissionRequest.requestCameraPermissions(
                ContextProvider.getApplicationContext(),
                object : PermissionCallback() {
                    override fun onGranted() {
                        LOGGER.info("requestCameraPermissions:[onGranted]")
                        PermissionRequest.requestMicrophonePermissions(
                            ContextProvider.getApplicationContext(),
                            object : PermissionCallback() {
                                override fun onGranted() {
                                    audienceManager.getViewStore()
                                        .updateTakeSeatState(true)
                                    audienceManager.getViewStore().updateOpenCameraAfterTakeSeatState(true)
                                    audienceManager.getCoGuestStore()
                                        .applyForSeat(-1, 60, true.toString(), completionHandler {
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
                    }

                    override fun onDenied() {
                        LOGGER.error("requestCameraPermissions:[onDenied]")
                    }
                })
            dismiss()
        }
    }

    private fun initPreviewVideoView() {
        audienceManager.getMediaStore().setLocalVideoView(previewVideoView)
        val isFront = audienceManager.getDeviceStore().deviceState.isFrontCamera.value
        PermissionRequest.requestCameraPermissions(
            ContextProvider.getApplicationContext(),
            object : PermissionCallback() {
                override fun onGranted() {
                    audienceManager.getDeviceStore().openLocalCamera(isFront, null)
                }
            })
    }

    private fun initRecycleSettingsOption() {
        recycleSettingsOption.layoutManager = GridLayoutManager(context, 2)
        val adapter = VideoCoGuestSettingsAdapter(context)
        adapter.setOnItemClickListener(object : VideoCoGuestSettingsAdapter.OnItemClickListener {
            override fun onBeautyItemClicked() {
                BeautyUtils.showBeautyDialog(context)
            }

            override fun onFlipItemClicked() {
                val isFront = audienceManager.getDeviceStore().deviceState.isFrontCamera.value
                audienceManager.getDeviceStore().switchCamera(!isFront)
            }
        })
        recycleSettingsOption.adapter = adapter
    }

    override fun onRoomDismissed(roomId: String) {
        dismiss()
        BeautyUtils.resetBeauty()
        BeautyUtils.dismissBeautyDialog()
    }

    private val onMyItemClickListener = object : VideoCoGuestSettingsAdapter.OnItemClickListener {
        override fun onBeautyItemClicked() {
            BeautyUtils.showBeautyDialog(context)
        }

        override fun onFlipItemClicked() {
            val isFront = audienceManager.getDeviceStore().deviceState.isFrontCamera.value
            audienceManager.getDeviceStore().switchCamera(!isFront)
        }
    }
}
