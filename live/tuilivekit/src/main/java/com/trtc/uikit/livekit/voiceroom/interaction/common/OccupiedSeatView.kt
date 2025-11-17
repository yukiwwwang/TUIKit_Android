package com.trtc.uikit.livekit.voiceroom.interaction.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.voiceroomcore.view.VoiceWaveView
import io.trtc.tuikit.atomicxcore.api.device.DeviceStatus
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import io.trtc.tuikit.atomicxcore.api.live.SeatInfo
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class OccupiedSeatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var imageAvatar: ImageFilterView
    private lateinit var soundWaveView: VoiceWaveView
    private lateinit var seatInfo: SeatInfo
    private lateinit var imageAvatarBg: ImageView
    private lateinit var textName: TextView
    private lateinit var imageMuteAudio: ImageView
    private lateinit var liveSeatStore: LiveSeatStore
    private lateinit var layoutRoot: FrameLayout
    private var lifecycleOwner: LifecycleOwner? = null
    private val jobs = mutableListOf<Job>()

    init {
        initView()
    }

    fun init(seatInfo: SeatInfo) {
        this.seatInfo = seatInfo
        this.liveSeatStore =
            LiveSeatStore.create(LiveListStore.shared().liveState.currentLive.value.liveID)

        bindData()
        initClickListener()
        addObservers()
    }

    private fun bindData() {
        updateAvatar()
        setBlurredBackground()
        updateUserName()
        updateMicrophoneStatus()
    }

    private fun updateAvatar() {
        ImageLoader.load(
            context,
            imageAvatar,
            seatInfo.userInfo.avatarURL,
            R.drawable.livekit_ic_avatar
        )
        ImageLoader.load(
            context,
            imageAvatarBg,
            seatInfo.userInfo.avatarURL,
            R.drawable.livekit_ic_avatar
        )
    }

    private fun setBlurredBackground() {
        val blurRadius = 20
        val sampling = 1

        Glide.with(this)
            .load(seatInfo.userInfo.avatarURL)
            .placeholder(R.drawable.livekit_ic_avatar)
            .error(R.drawable.livekit_ic_avatar)
            .apply(
                RequestOptions.bitmapTransform(
                    BlurTransformation(blurRadius, sampling)
                )
            )
            .into(imageAvatarBg)
    }


    private fun updateUserName() {
        textName.text = seatInfo.userInfo.userName.ifEmpty { seatInfo.userInfo.userID }
    }

    private fun updateMicrophoneStatus() {
        imageMuteAudio.visibility = if (seatInfo.userInfo.microphoneStatus == DeviceStatus.ON) GONE else VISIBLE
    }

    private fun initClickListener() {
        layoutRoot.setOnClickListener {
            val panel = SeatUserManagementPanel(context)
            panel.init(seatInfo)
            val popupDialog = PopupDialog(context)
            popupDialog.setView(panel)
            panel.setOnInviteButtonClickListener { popupDialog.hide() }
            popupDialog.show()
        }
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.livekit_voiceroom_occupied_seat_view, this, true)
        layoutRoot = findViewById(R.id.fl_root)
        imageAvatar = findViewById(R.id.iv_avatar)
        soundWaveView = findViewById(R.id.rv_sound_wave)
        imageAvatarBg = findViewById(R.id.iv_avatar_bg)
        imageMuteAudio = findViewById(R.id.iv_mute_audio)
        textName = findViewById(R.id.tv_name)
    }

    private fun addObservers() {
        lifecycleOwner?.lifecycleScope?.let { scope ->
            liveSeatStore.liveSeatState.speakingUsers
                .onEach(::onSpeakingUsersChange)
                .launchIn(scope)
                .let(jobs::add)
        }
    }

    private fun onSpeakingUsersChange(speakingUsers: Map<String, Int>) {
        val volume = speakingUsers[seatInfo.userInfo.userID]
        val isSpeaking = volume != null && volume > 25
        soundWaveView.visibility = if (isSpeaking) VISIBLE else GONE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        jobs.forEach { it.cancel() }
        jobs.clear()
        lifecycleOwner = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        jobs.forEach { it.cancel() }
        jobs.clear()
    }
}