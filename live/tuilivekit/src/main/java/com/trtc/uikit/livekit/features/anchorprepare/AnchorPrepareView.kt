package com.trtc.uikit.livekit.features.anchorprepare

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.qcloud.tuicore.util.ScreenUtil
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.ui.RoundFrameLayout
import com.trtc.uikit.livekit.component.beauty.BeautyUtils.resetBeauty
import com.trtc.uikit.livekit.component.beauty.tebeauty.store.TEBeautyStore
import com.trtc.uikit.livekit.features.anchorprepare.manager.AnchorPrepareManager
import com.trtc.uikit.livekit.features.anchorprepare.state.AnchorPrepareConfig
import com.trtc.uikit.livekit.features.anchorprepare.state.AnchorPrepareState
import com.trtc.uikit.livekit.features.anchorprepare.view.function.PrepareFunctionView
import com.trtc.uikit.livekit.features.anchorprepare.view.liveinfoedit.LiveInfoEditView
import com.trtc.uikit.livekit.features.anchorprepare.view.startlive.StartLiveButton
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.device.AudioEffectStore
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.view.CoreViewType
import io.trtc.tuikit.atomicxcore.api.view.LiveCoreView

class AnchorPrepareView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val logger = LiveKitLogger.getFeaturesLogger("AnchorPrepareView")
    private lateinit var layoutRoot: FrameLayout
    private var manager: AnchorPrepareManager? = null
    private var state: AnchorPrepareState? = null
    private var liveCoreView: LiveCoreView? = null
    private var functionView: PrepareFunctionView? = null
    private lateinit var imageBack: ImageView
    private var isDestroy = false

    private val disableMenuObserver = Observer<Boolean> { onFeatureMenuDisable(it) }

    init {
        logger.info("AnchorPrepareView Constructor.")
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.anchor_prepare_layout_prepare_view, this, true)
        layoutRoot = findViewById(R.id.fl_root)
        imageBack = findViewById(R.id.iv_back)
    }

    fun init(roomId: String, liveCoreView: LiveCoreView?) {
        logger.info("AnchorPrepareView init. roomId:$roomId,liveCoreView:$liveCoreView")
        this.liveCoreView = liveCoreView
            ?: LiveCoreView(context, null, 0, CoreViewType.PUSH_VIEW).apply {
                setLiveId(roomId)
            }
        initManager(roomId)
        initComponent()
    }

    fun getCoreView(): LiveCoreView? = liveCoreView

    fun addAnchorPrepareViewListener(listener: AnchorPrepareViewListener) {
        manager?.addAnchorPrepareViewListener(listener)
    }

    fun removeAnchorPrepareViewListener(listener: AnchorPrepareViewListener) {
        manager?.removeAnchorPrepareViewListener(listener)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }

    private fun addObserver() {
        AnchorPrepareConfig.disableFeatureMenu.observeForever(disableMenuObserver)
    }

    private fun removeObserver() {
        AnchorPrepareConfig.disableFeatureMenu.removeObserver(disableMenuObserver)
    }

    private fun initManager(roomId: String) {
        liveCoreView?.let { coreView ->
            manager = AnchorPrepareManager()
            state = manager?.getState()
            state?.roomId = roomId
        }
    }

    private fun initComponent() {
        initBackView()
        initCoreView()
        initLiveInfoEditView()
        initFunctionView()
        initStartLiveButton()
    }

    private fun initBackView() {
        imageBack.setOnClickListener {
            manager?.stopPreview()
            resetBeauty()
            TEBeautyStore.unInit()
            AudioEffectStore.shared().reset()
            DeviceStore.shared().reset()
        }
    }

    private fun initCoreView() {
        val coreView = liveCoreView
        if (coreView == null) {
            logger.error("Please call the AnchorPrepareView.init() method first.")
            return
        }

        val frameLayout = findViewById<RoundFrameLayout>(R.id.fl_video_view_container)
        if (coreView.parent == null) {
            frameLayout.setRadius(com.trtc.tuikit.common.util.ScreenUtil.dip2px(16.0f))
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            frameLayout.addView(coreView, layoutParams)
            layoutRoot.setBackgroundColor(resources.getColor(R.color.common_black))
        } else {
            frameLayout.visibility = GONE
            layoutRoot.setBackgroundColor(resources.getColor(R.color.common_design_standard_transparent))
        }

        state?.useFrontCamera?.value = DeviceStore.shared().deviceState.isFrontCamera.value
        manager?.startPreview(object : CompletionHandler {
            override fun onSuccess() {}

            override fun onFailure(code: Int, desc: String) {
                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                val context = this@AnchorPrepareView.context
                if (context is Activity) {
                    context.finish()
                }
            }
        })
    }

    private fun initLiveInfoEditView() {
        manager?.let { mgr ->
            val liveInfoEditView = LiveInfoEditView(context)
            liveInfoEditView.init(mgr)

            val layoutParams = FrameLayout.LayoutParams(
                ScreenUtil.dip2px(343.0f),
                ScreenUtil.dip2px(112.0f)
            ).apply {
                topMargin = ScreenUtil.dip2px(96.0f)
                gravity = Gravity.CENTER_HORIZONTAL
            }

            addView(liveInfoEditView, layoutParams)
        }
    }

    private fun initFunctionView() {
        if (AnchorPrepareConfig.disableFeatureMenu.value == true) {
            functionView?.let { view ->
                (view.parent as? ViewGroup)?.removeView(view)
            }
        } else {
            if (functionView != null) {
                return
            }

            manager?.let { mgr ->
                liveCoreView?.let { coreView ->
                    functionView = PrepareFunctionView(context)
                    functionView?.init(mgr, coreView)

                    val layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ScreenUtil.dip2px(56.0f)
                    ).apply {
                        marginStart = ScreenUtil.dip2px(22.0f)
                        marginEnd = ScreenUtil.dip2px(22.0f)
                        gravity = Gravity.BOTTOM
                        bottomMargin = ScreenUtil.dip2px(144.0f)
                    }
                    addView(functionView, layoutParams)
                }
            }
        }
    }

    private fun initStartLiveButton() {
        manager?.let { mgr ->
            val startLiveButton = StartLiveButton(context)
            startLiveButton.init(mgr)

            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ScreenUtil.dip2px(52.0f)
            ).apply {
                marginStart = ScreenUtil.dip2px(24.0f)
                marginEnd = ScreenUtil.dip2px(24.0f)
                bottomMargin = ScreenUtil.dip2px(60.0f)
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            }

            addView(startLiveButton, layoutParams)
        }
    }

    private fun onFeatureMenuDisable(disable: Boolean?) {
        initFunctionView()
    }

    fun getState(): PrepareState? {
        return manager?.getExternalState()
    }

    fun disableFeatureMenu(disable: Boolean) {
        logger.info("disableFeatureMenu: disable = $disable")
        AnchorPrepareManager.disableFeatureMenu(disable)
    }

    @Deprecated("Use disableMenuSwitchCamera instead")
    fun disableMenuSwitchButton(disable: Boolean) {
        logger.info("disableMenuSwitchButton: disable = $disable")
        AnchorPrepareManager.disableMenuSwitchButton(disable)
    }

    @Deprecated("Use disableMenuBeauty instead")
    fun disableMenuBeautyButton(disable: Boolean) {
        logger.info("disableMenuBeautyButton: disable = $disable")
        AnchorPrepareManager.disableMenuBeautyButton(disable)
    }

    @Deprecated("Use disableMenuAudioEffect instead")
    fun disableMenuAudioEffectButton(disable: Boolean) {
        logger.info("disableMenuAudioEffectButton: disable = $disable")
        AnchorPrepareManager.disableMenuAudioEffectButton(disable)
    }

    fun disableMenuSwitchCamera(disable: Boolean) {
        logger.info("disableMenuSwitchButton: disable = $disable")
        AnchorPrepareManager.disableMenuSwitchButton(disable)
    }

    fun disableMenuBeauty(disable: Boolean) {
        logger.info("disableMenuBeautyButton: disable = $disable")
        AnchorPrepareManager.disableMenuBeautyButton(disable)
    }

    fun disableMenuAudioEffect(disable: Boolean) {
        logger.info("disableMenuAudioEffectButton: disable = $disable")
        AnchorPrepareManager.disableMenuAudioEffectButton(disable)
    }
}