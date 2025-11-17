package com.trtc.uikit.livekit.features.anchorprepare.view.function

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.component.audioeffect.AudioEffectPanel
import com.trtc.uikit.livekit.component.beauty.BeautyUtils
import com.trtc.uikit.livekit.component.beauty.tebeauty.TEBeautyManager
import com.trtc.uikit.livekit.features.anchorprepare.manager.AnchorPrepareManager
import com.trtc.uikit.livekit.features.anchorprepare.state.AnchorPrepareConfig
import com.trtc.uikit.livekit.features.anchorprepare.view.liveinfoedit.livetemplatepicker.LiveTemplatePicker
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.view.LiveCoreView

class PrepareFunctionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var audioEffectPanel: PopupDialog? = null
    private var videoSettingPanel: PrepareVideoSettingPanel? = null
    private var manager: AnchorPrepareManager? = null
    private var liveCoreView: LiveCoreView? = null

    private val disableAudioEffectObserver = Observer<Boolean> { onAudioEffectDisableChange(it) }
    private val disableBeautyObserver = Observer<Boolean> { onBeautyDisableChange(it) }
    private val disableMirrorObserver = Observer<Boolean> { onMirrorDisableChange(it) }

    init {
        LayoutInflater.from(context).inflate(R.layout.anchor_prepare_layout_function, this, true)
    }

    fun init(manager: AnchorPrepareManager, liveCoreView: LiveCoreView) {
        this.manager = manager
        this.liveCoreView = liveCoreView
        TEBeautyManager.setCustomVideoProcess()

        initView()
        addObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }

    private fun addObserver() {
        AnchorPrepareConfig.disableMenuAudioEffectButton.observeForever(disableAudioEffectObserver)
        AnchorPrepareConfig.disableMenuBeautyButton.observeForever(disableBeautyObserver)
        AnchorPrepareConfig.disableMenuSwitchButton.observeForever(disableMirrorObserver)
    }

    private fun removeObserver() {
        AnchorPrepareConfig.disableMenuAudioEffectButton.removeObserver(disableAudioEffectObserver)
        AnchorPrepareConfig.disableMenuBeautyButton.removeObserver(disableBeautyObserver)
        AnchorPrepareConfig.disableMenuSwitchButton.removeObserver(disableMirrorObserver)
    }

    private fun initView() {
        initBeautyButton()
        initAudioEffectButton()
        initFlipButton()
        initLayoutButton()
        initVideoSettingButton()
    }

    private fun initBeautyButton() {
        findViewById<android.view.View>(R.id.iv_beauty).setOnClickListener {
            BeautyUtils.showBeautyDialog(context)
        }
    }

    private fun initAudioEffectButton() {
        findViewById<android.view.View>(R.id.iv_audio_effect).setOnClickListener {
            if (audioEffectPanel == null) {
                audioEffectPanel = PopupDialog(context)
                val audioEffectPanelView = AudioEffectPanel(context)
                audioEffectPanelView.init(manager?.getState()?.roomId ?: "")
                audioEffectPanelView.setOnBackButtonClickListener(object : AudioEffectPanel.OnBackButtonClickListener {
                    override fun onClick() {
                        audioEffectPanel?.dismiss()
                    }
                })
                audioEffectPanel?.setView(audioEffectPanelView)
            }
            audioEffectPanel?.show()
        }
    }

    private fun initFlipButton() {
        findViewById<android.view.View>(R.id.iv_flip).setOnClickListener {
            val isFront = DeviceStore.shared().deviceState.isFrontCamera.value
            DeviceStore.shared().switchCamera(!isFront)
        }
    }

    private fun initLayoutButton() {
        findViewById<android.view.View>(R.id.iv_layout).setOnClickListener {
            manager?.let { mgr ->
                liveCoreView?.let { coreView ->
                    val picker = LiveTemplatePicker(context, mgr, coreView)
                    picker.show()
                }
            }
        }
    }

    private fun initVideoSettingButton() {
        findViewById<android.view.View>(R.id.iv_video_setting).setOnClickListener {
            if (videoSettingPanel == null) {
                liveCoreView?.let {
                    videoSettingPanel = PrepareVideoSettingPanel(context, it)
                }
            }
            videoSettingPanel?.show()
        }
    }

    private fun onMirrorDisableChange(disable: Boolean?) {
        if (AnchorPrepareConfig.disableMenuSwitchButton.value == true) {
            findViewById<android.view.View>(R.id.rl_flip).visibility = GONE
        } else {
            findViewById<android.view.View>(R.id.rl_flip).visibility = VISIBLE
        }
    }

    private fun onAudioEffectDisableChange(disable: Boolean?) {
        if (AnchorPrepareConfig.disableMenuAudioEffectButton.value == true) {
            findViewById<android.view.View>(R.id.rl_audio_effect).visibility = GONE
        } else {
            findViewById<android.view.View>(R.id.rl_audio_effect).visibility = VISIBLE
        }
    }

    private fun onBeautyDisableChange(disable: Boolean?) {
        if (AnchorPrepareConfig.disableMenuBeautyButton.value == true) {
            findViewById<android.view.View>(R.id.rl_beauty).visibility = GONE
        } else {
            findViewById<android.view.View>(R.id.rl_beauty).visibility = VISIBLE
        }
    }
}