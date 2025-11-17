package com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.panel

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.tencent.qcloud.tuicore.TUILogin
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.tuikit.common.permission.PermissionCallback
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.PermissionRequest
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import com.trtc.uikit.livekit.features.anchorboardcast.manager.module.MediaManager
import com.trtc.uikit.livekit.features.anchorboardcast.view.usermanage.ConfirmDialog
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.device.DeviceStatus
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveSeatStore
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import io.trtc.tuikit.atomicxcore.api.view.LiveCoreView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AnchorManagerDialog(
    private val context: Context,
    private val anchorManager: AnchorManager,
    private val liveCoreView: LiveCoreView
) : PopupDialog(context) {

    companion object {
        private val LOGGER = LiveKitLogger.getFeaturesLogger("AnchorManagerDialog")
        private const val BUTTON_DISABLE_ALPHA = 0.24f
        private const val BUTTON_ENABLE_ALPHA = 1.0f
    }

    private var userInfo: TUIRoomDefine.SeatFullInfo? = null
    private lateinit var imageHeadView: ImageFilterView
    private lateinit var userIdText: TextView
    private lateinit var userNameText: TextView
    private lateinit var flipCameraContainer: View
    private lateinit var followContainer: View
    private lateinit var handUpContainer: View
    private lateinit var audioContainer: View
    private lateinit var videoContainer: View
    private lateinit var ivAudio: ImageView
    private lateinit var tvAudio: TextView
    private lateinit var ivVideo: ImageView
    private lateinit var tvVideo: TextView
    private lateinit var textUnfollow: TextView
    private lateinit var imageFollowIcon: ImageView
    private var confirmDialog: ConfirmDialog? = null
    private var subscribeStateJob: Job? = null

    init {
        initView()
    }

    fun init(userInfo: TUIRoomDefine.SeatFullInfo) {
        this.userInfo = userInfo
        anchorManager.getUserManager().checkFollowUser(userInfo.userId)
        updateView()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }

    private fun initView() {
        val rootView = View.inflate(context, R.layout.livekit_anchor_manager_panel, null)
        setView(rootView)
        bindViewId(rootView)
        initFollowButtonView(rootView)
    }

    private fun bindViewId(rootView: View) {
        userIdText = rootView.findViewById(R.id.user_id)
        userNameText = rootView.findViewById(R.id.user_name)
        imageHeadView = rootView.findViewById(R.id.iv_head)
        handUpContainer = rootView.findViewById(R.id.hand_up_container)
        flipCameraContainer = rootView.findViewById(R.id.flip_camera_container)
        followContainer = rootView.findViewById(R.id.fl_follow_panel)
        ivAudio = rootView.findViewById(R.id.iv_audio)
        audioContainer = rootView.findViewById(R.id.audio_container)
        tvAudio = rootView.findViewById(R.id.tv_audio)
        videoContainer = rootView.findViewById(R.id.video_container)
        ivVideo = rootView.findViewById(R.id.iv_video)
        tvVideo = rootView.findViewById(R.id.tv_video)
        textUnfollow = rootView.findViewById(R.id.tv_unfollow)
        imageFollowIcon = rootView.findViewById(R.id.iv_follow)

        handUpContainer.setOnClickListener { onHangupButtonClicked() }
        flipCameraContainer.setOnClickListener { onSwitchCameraButtonClicked() }
        audioContainer.setOnClickListener { onMicrophoneButtonClicked() }
        videoContainer.setOnClickListener { onCameraButtonClicked() }
    }

    private fun updateView() {
        val currentUserInfo = userInfo ?: return
        if (TextUtils.isEmpty(currentUserInfo.userId)) {
            return
        }

        val avatarUrl = currentUserInfo.userAvatar
        if (TextUtils.isEmpty(avatarUrl)) {
            imageHeadView.setImageResource(R.drawable.livekit_ic_avatar)
        } else {
            ImageLoader.load(context, imageHeadView, avatarUrl, R.drawable.livekit_ic_avatar)
        }
        userNameText.text = currentUserInfo.userName
        userIdText.text = context.getString(R.string.common_user_id, currentUserInfo.userId)
        updateMediaDeviceButton()
    }

    private fun updateMediaDeviceButton() {
        if (isAdmin()) {
            updateAdminDeviceButton()
        } else {
            updateGeneralUserDeviceButton()
        }
    }

    private fun updateAdminDeviceButton() {
        if (isSelfUser()) {
            flipCameraContainer.visibility = VISIBLE
            handUpContainer.visibility = GONE
            followContainer.visibility = GONE
            videoContainer.visibility = GONE
        } else {
            flipCameraContainer.visibility = GONE
            handUpContainer.visibility = VISIBLE
            followContainer.visibility = VISIBLE
            videoContainer.visibility = VISIBLE
        }
    }

    private fun updateGeneralUserDeviceButton() {
        if (isSelfUser()) {
            flipCameraContainer.visibility = VISIBLE
            handUpContainer.visibility = VISIBLE
            followContainer.visibility = GONE
        }
    }

    private fun isSelfUser(): Boolean {
        val currentUserInfo = userInfo ?: return false
        if (TextUtils.isEmpty(currentUserInfo.userId)) {
            return false
        }
        return currentUserInfo.userId == TUILogin.getUserId()
    }

    private fun isAdmin(): Boolean {
        return TUILogin.getUserId() == LiveListStore.shared().liveState.currentLive.value.liveOwner.userID
    }

    private fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                onCameraStatusChanged()
            }
            launch {
                onLockAudioUserListChanged()
            }
            launch {
                onFollowingUserChanged()
            }
            launch {
                onMicrophoneStatusChanged()
            }
            launch {
                onCameraStatusChanged()
            }
            launch {
                onConnectUserListChanged()
            }
        }
        TUIRoomEngine.sharedInstance().addObserver(tuiRoomObserver)
    }

    private fun removeObserver() {
        subscribeStateJob?.cancel()
        TUIRoomEngine.sharedInstance().removeObserver(tuiRoomObserver)
    }

    private fun initFollowButtonView(rootView: View) {
        rootView.findViewById<View>(R.id.fl_follow_panel).setOnClickListener {
            val currentUserInfo = userInfo ?: return@setOnClickListener
            if (anchorManager.getUserState().followingUserList.value!!.contains(currentUserInfo.userId)) {
                anchorManager.getUserManager().unfollowUser(currentUserInfo.userId)
            } else {
                anchorManager.getUserManager().followUser(currentUserInfo.userId)
            }
        }
    }

    private suspend fun onFollowingUserChanged() {
        anchorManager.getUserState().followingUserList.collect { followUsers ->
            userInfo?.let { currentUserInfo ->
                if (followUsers.contains(currentUserInfo.userId)) {
                    textUnfollow.visibility = GONE
                    imageFollowIcon.visibility = VISIBLE
                } else {
                    imageFollowIcon.visibility = GONE
                    textUnfollow.visibility = VISIBLE
                }
            }
        }
    }

    private fun onHangupButtonClicked() {
        val currentUserInfo = userInfo ?: return

        if (confirmDialog == null) {
            confirmDialog = ConfirmDialog(context)
        }

        if (isAdmin()) {
            confirmDialog?.apply {
                setContent(context.getString(R.string.common_disconnect_tips))
                setPositiveText(context.getString(R.string.common_disconnection))
                setPositiveListener {
                    LiveSeatStore.create(LiveListStore.shared().liveState.currentLive.value.liveID)
                        .kickUserOutOfSeat(currentUserInfo.userId, object : CompletionHandler {
                            override fun onSuccess() {}

                            override fun onFailure(code: Int, desc: String) {
                                LOGGER.error("disconnectUser failed:code:$code,desc:$desc")
                                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                            }
                        })
                    dismiss()
                }
                show()
            }
            return
        }

        if (isSelfUser()) {
            confirmDialog?.apply {
                setContent(context.getString(R.string.common_terminate_room_connection_message))
                setPositiveText(context.getString(R.string.common_disconnection))
                setPositiveListener {
                    CoGuestStore.create(LiveListStore.shared().liveState.currentLive.value.liveID).disconnect(null)
                    dismiss()
                }
                show()
            }
        }
    }

    private fun onMicrophoneButtonClicked() {
        userInfo?.let {
            if (isSelfUser()) {
                if (it.userMicrophoneStatus == TUIRoomDefine.DeviceStatus.OPENED) {
                    LiveSeatStore.create(LiveListStore.shared().liveState.currentLive.value.liveID).muteMicrophone()
                } else {
                    LiveSeatStore.create(LiveListStore.shared().liveState.currentLive.value.liveID)
                        .unmuteMicrophone(object : CompletionHandler {
                            override fun onSuccess() {}

                            override fun onFailure(code: Int, desc: String) {
                                LOGGER.error("unMuteMicrophone failed:code:$code,desc:$desc")
                                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                            }

                        })
                }
                dismiss()
                return
            }

            if (isAdmin()) {
                val isAudioLocked = anchorManager.getState().lockAudioUserList.value?.contains(it.userId)
                anchorManager.getMediaManager().disableUserMediaDevice(
                    it.userId,
                    MediaManager.MediaDevice.MICROPHONE,
                    isAudioLocked == true
                )
                dismiss()
            }
        }
    }

    private fun onCameraButtonClicked() {
        val currentUserInfo = userInfo ?: return

        if (isSelfUser()) {
            if (DeviceStore.shared().deviceState.cameraStatus.value == DeviceStatus.ON) {
                DeviceStore.shared().closeLocalCamera()
                flipCameraContainer.visibility = GONE
            } else {
                startCamera()
            }
            dismiss()
            return
        }

        if (isAdmin()) {
            userInfo?.let {
                val isVideoLocked = anchorManager.getState().lockVideoUserList.value?.contains(it.userId)
                anchorManager.getMediaManager().disableUserMediaDevice(
                    currentUserInfo.userId,
                    MediaManager.MediaDevice.CAMERA,
                    isVideoLocked == true
                )
            }
            dismiss()
        }
    }

    private fun onSwitchCameraButtonClicked() {
        val isFront = DeviceStore.shared().deviceState.isFrontCamera.value
        DeviceStore.shared().switchCamera(!isFront)
        dismiss()
    }

    private fun startCamera() {
        val isFrontCamera = DeviceStore.shared().deviceState.isFrontCamera.value
        PermissionRequest.requestCameraPermissions(ContextProvider.getApplicationContext(), object :
            PermissionCallback() {
            override fun onRequesting() {
                LOGGER.info("requestCameraPermissions:[onRequesting]")
            }

            override fun onGranted() {
                LOGGER.info("requestCameraPermissions:[onGranted]")
                DeviceStore.shared().openLocalCamera(isFrontCamera, object : CompletionHandler {
                    override fun onSuccess() {
                        TODO("Not yet implemented")
                    }

                    override fun onFailure(code: Int, desc: String) {
                        LOGGER.error("startCamera failed:code:$code,desc:$desc")
                        ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                    }

                })
            }
        })
    }

    private suspend fun onMicrophoneStatusChanged() {
        DeviceStore.shared().deviceState.microphoneStatus.collect {
            userInfo?.let {
                val isAudioLocked = anchorManager.getState().lockAudioUserList.value?.contains(it.userId)
                if (!isSelfUser()) {
                    if (isAdmin()) {
                        audioContainer.isEnabled = true
                        ivAudio.alpha = BUTTON_ENABLE_ALPHA
                        ivAudio.setImageResource(
                            if (isAudioLocked == true) R.drawable.livekit_ic_disable_audio
                            else R.drawable.livekit_ic_unmute_audio
                        )
                        tvAudio.setText(
                            if (isAudioLocked == true) R.string.common_enable_audio
                            else R.string.common_disable_audio
                        )
                    }
                } else if (isAudioLocked == true) {
                    audioContainer.isEnabled = false
                    ivAudio.alpha = BUTTON_DISABLE_ALPHA
                    ivAudio.setImageResource(R.drawable.livekit_ic_mute_audio)
                    tvAudio.setText(R.string.common_unmute_audio)
                } else {
                    audioContainer.isEnabled = true
                    ivAudio.alpha = BUTTON_ENABLE_ALPHA
                    val isMicrophoneMuted = it.userMicrophoneStatus != TUIRoomDefine.DeviceStatus.OPENED
                    if (isMicrophoneMuted) {
                        ivAudio.setImageResource(R.drawable.livekit_ic_mute_audio)
                        tvAudio.setText(R.string.common_unmute_audio)
                    } else {
                        ivAudio.setImageResource(R.drawable.livekit_ic_unmute_audio)
                        tvAudio.setText(R.string.common_mute_audio)
                    }
                }
            }
        }
    }

    private suspend fun onLockAudioUserListChanged() {
        anchorManager.getState().lockAudioUserList.collect { lockAudioUserList ->
            userInfo?.let {
                val isAudioLocked = lockAudioUserList.contains(it.userId)
                if (!isSelfUser()) {
                    if (isAdmin()) {
                        audioContainer.isEnabled = true
                        ivAudio.alpha = BUTTON_ENABLE_ALPHA
                        ivAudio.setImageResource(
                            if (isAudioLocked) R.drawable.livekit_ic_disable_audio
                            else R.drawable.livekit_ic_unmute_audio
                        )
                        tvAudio.setText(
                            if (isAudioLocked) R.string.common_enable_audio
                            else R.string.common_disable_audio
                        )
                    }
                } else if (isAudioLocked) {
                    audioContainer.isEnabled = false
                    ivAudio.alpha = BUTTON_DISABLE_ALPHA
                    ivAudio.setImageResource(R.drawable.livekit_ic_mute_audio)
                    tvAudio.setText(R.string.common_unmute_audio)

                } else {
                    audioContainer.isEnabled = true
                    ivAudio.alpha = BUTTON_ENABLE_ALPHA
                    val isMicrophoneMuted = DeviceStore.shared().deviceState.microphoneStatus.value == DeviceStatus.OFF
                    if (isMicrophoneMuted) {
                        ivAudio.setImageResource(R.drawable.livekit_ic_mute_audio)
                        tvAudio.setText(R.string.common_unmute_audio)
                    } else {
                        ivAudio.setImageResource(R.drawable.livekit_ic_unmute_audio)
                        tvAudio.setText(R.string.common_mute_audio)
                    }
                }
            }
        }
    }

    private suspend fun onCameraStatusChanged() {
        anchorManager.getState().lockVideoUserList.collect { lockVideoUserList ->
            userInfo?.let {
                val isVideoLocked = lockVideoUserList.contains(it.userId)

                if (!isSelfUser()) {
                    if (isAdmin()) {
                        videoContainer.isEnabled = true
                        ivVideo.alpha = BUTTON_ENABLE_ALPHA
                        ivVideo.setImageResource(
                            if (isVideoLocked) R.drawable.livekit_ic_disable_video
                            else R.drawable.livekit_ic_start_video
                        )
                        tvVideo.setText(
                            if (isVideoLocked) R.string.common_enable_video
                            else R.string.common_disable_video
                        )
                    }
                } else {
                    val isCameraOpened = (DeviceStatus.ON == DeviceStore.shared().deviceState.cameraStatus.value)
                    if (isVideoLocked) {
                        videoContainer.isEnabled = false
                        ivVideo.alpha = BUTTON_DISABLE_ALPHA
                        ivVideo.setImageResource(R.drawable.livekit_ic_stop_video)
                        tvVideo.setText(R.string.common_start_video)
                        flipCameraContainer.visibility = GONE
                    } else {
                        videoContainer.isEnabled = true
                        ivVideo.alpha = BUTTON_ENABLE_ALPHA
                        if (isCameraOpened) {
                            ivVideo.setImageResource(R.drawable.livekit_ic_start_video)
                            tvVideo.setText(R.string.common_stop_video)
                            flipCameraContainer.visibility = VISIBLE
                        } else {
                            ivVideo.setImageResource(R.drawable.livekit_ic_stop_video)
                            tvVideo.setText(R.string.common_start_video)
                            flipCameraContainer.visibility = GONE
                        }
                    }
                }
            }
        }
    }

    private suspend fun onConnectUserListChanged() {
        CoGuestStore.create(LiveListStore.shared().liveState.currentLive.value.liveID).coGuestState.connected.collect {
            val userList: List<SeatUserInfo> =
                it.filterNot { it.liveID != LiveListStore.shared().liveState.currentLive.value.liveID }
            userInfo?.let {
                val isConnected = userList.any { item ->
                    it.userId == item.userID
                }
                if (!isConnected) {
                    dismiss()
                    confirmDialog?.dismiss()
                }
            }

        }

    }

    private val tuiRoomObserver = object : TUIRoomObserver() {
        override fun onKickedOffSeat(seatIndex: Int, operateUser: TUIRoomDefine.UserInfo) {
            dismiss()
            confirmDialog?.dismiss()
        }

        override fun onRemoteUserLeaveRoom(roomId: String, userInfo: TUIRoomDefine.UserInfo) {
            val currentUserInfo = this@AnchorManagerDialog.userInfo ?: return
            if (TextUtils.isEmpty(currentUserInfo.userId)) {
                return
            }
            if (userInfo.userId == currentUserInfo.userId) {
                dismiss()
                confirmDialog?.dismiss()
            }
        }
    }
}