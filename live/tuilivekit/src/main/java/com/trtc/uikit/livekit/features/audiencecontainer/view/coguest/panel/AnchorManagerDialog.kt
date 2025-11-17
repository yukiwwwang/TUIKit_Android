package com.trtc.uikit.livekit.features.audiencecontainer.view.coguest.panel

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.tuikit.common.permission.PermissionCallback
import com.trtc.tuikit.common.system.ContextProvider

import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.PermissionRequest
import com.trtc.uikit.livekit.common.completionHandler
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager
import com.trtc.uikit.livekit.features.audiencecontainer.view.ConfirmDialog
import io.trtc.tuikit.atomicxcore.api.device.DeviceStatus
import io.trtc.tuikit.atomicxcore.api.live.GuestListener
import io.trtc.tuikit.atomicxcore.api.live.LiveAudienceListener
import io.trtc.tuikit.atomicxcore.api.live.LiveEndedReason
import io.trtc.tuikit.atomicxcore.api.live.LiveListListener
import io.trtc.tuikit.atomicxcore.api.live.LiveUserInfo
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AnchorManagerDialog(
    context: Context,
    private val audienceManager: AudienceManager,
) : PopupDialog(context) {

    private var seatUserInfo: LiveUserInfo? = null
    private var confirmDialog: ConfirmDialog? = null
    private var subscribeStateJob: Job? = null

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

    init {
        initView()
    }

    fun init(seatInfo: LiveUserInfo) {
        seatUserInfo = seatInfo
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
        if (seatUserInfo == null) {
            return
        }
        if (TextUtils.isEmpty(seatUserInfo!!.userID)) {
            return
        }
        val avatarUrl = seatUserInfo!!.avatarURL
        if (TextUtils.isEmpty(avatarUrl)) {
            imageHeadView.setImageResource(R.drawable.livekit_ic_avatar)
        } else {
            ImageLoader.load(context, imageHeadView, avatarUrl, R.drawable.livekit_ic_avatar)
        }
        userNameText.text = seatUserInfo!!.userName
        userIdText.text = context.getString(R.string.common_user_id, seatUserInfo!!.userID)
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
        } else {
            flipCameraContainer.visibility = GONE
            handUpContainer.visibility = VISIBLE
            followContainer.visibility = VISIBLE
        }
    }

    private fun updateGeneralUserDeviceButton() {
        if (isSelfUser()) {
            flipCameraContainer.visibility = VISIBLE
            handUpContainer.visibility = VISIBLE
            followContainer.visibility = GONE
            updateCameraButton(
                audienceManager.getLiveSeatState().seatList.value.find { it.userInfo.userID == seatUserInfo?.userID }?.userInfo?.allowOpenCamera == false
            )
            updateMicrophoneButton(
                audienceManager.getLiveSeatState().seatList.value.find { it.userInfo.userID == seatUserInfo?.userID }?.userInfo?.allowOpenMicrophone == false
            )
        }
    }

    private fun isSelfUser(): Boolean {
        if (seatUserInfo == null) {
            return false
        }
        if (TextUtils.isEmpty(seatUserInfo!!.userID)) {
            return false
        }
        return seatUserInfo!!.userID == TUIRoomEngine.getSelfInfo().userId
    }

    private fun isAdmin(): Boolean {
        val selfUserId = TUIRoomEngine.getSelfInfo().userId
        if (TextUtils.isEmpty(selfUserId)) {
            return false
        }
        return selfUserId == audienceManager.getLiveListState().currentLive.value.liveOwner.userID
    }

    private fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                audienceManager.getLiveSeatState().seatList
                    .map { seatList -> seatList.find { it.userInfo.userID == seatUserInfo?.userID }?.userInfo?.allowOpenMicrophone }
                    .distinctUntilChanged()
                    .collect { allowOpenMicrophone ->
                        if (allowOpenMicrophone == null) return@collect
                        updateMicrophoneButton(!allowOpenMicrophone)
                    }
            }
            launch {
                audienceManager.getLiveSeatState().seatList
                    .map { seatList -> seatList.find { it.userInfo.userID == seatUserInfo?.userID }?.userInfo?.allowOpenCamera }
                    .distinctUntilChanged()
                    .collect { allowOpenCamera ->
                        if (allowOpenCamera == null) return@collect
                        updateCameraButton(!allowOpenCamera)
                    }
            }
            launch {
                audienceManager.getIMState().followingUserList.collect {
                    onFollowingUserChanged(it)
                }
            }
            launch {
                audienceManager.getCoGuestState().connected.collect {
                    onConnectUserListChanged(it)
                }
            }
        }
        audienceManager.getLiveListStore().addLiveListListener(liveLisListener)
        audienceManager.getCoGuestStore().addGuestListener(coGuestListener)
        audienceManager.getLiveAudienceStore().addLiveAudienceListener(liveAudienceListener)
    }

    private fun removeObserver() {
        subscribeStateJob?.cancel()
        audienceManager.getLiveListStore().removeLiveListListener(liveLisListener)
        audienceManager.getCoGuestStore().removeGuestListener(coGuestListener)
        audienceManager.getLiveAudienceStore().removeLiveAudienceListener(liveAudienceListener)
    }

    private fun initFollowButtonView(rootView: View) {
        rootView.findViewById<View>(R.id.fl_follow_panel).setOnClickListener {
            if (seatUserInfo == null) {
                return@setOnClickListener
            }
            if (audienceManager.getIMState().followingUserList.value.contains(seatUserInfo!!.userID) == true) {
                audienceManager.getIMStore().unfollowUser(seatUserInfo!!.userID)
            } else {
                audienceManager.getIMStore().followUser(seatUserInfo!!.userID)
            }
        }
    }

    private fun onFollowingUserChanged(followUsers: Set<String>) {
        if (seatUserInfo == null) {
            return
        }
        if (followUsers.contains(seatUserInfo!!.userID)) {
            textUnfollow.visibility = GONE
            imageFollowIcon.visibility = VISIBLE
        } else {
            imageFollowIcon.visibility = GONE
            textUnfollow.visibility = VISIBLE
        }
    }

    private fun onHangupButtonClicked() {
        if (seatUserInfo == null) {
            return
        }

        if (confirmDialog == null) {
            confirmDialog = ConfirmDialog(context)
        }

        if (isAdmin()) {
            confirmDialog!!.setContent(context.getString(R.string.common_disconnect_tips))
            confirmDialog!!.setPositiveText(context.getString(R.string.common_disconnection))
            confirmDialog!!.setPositiveListener {
                audienceManager.getLiveSeatStore().kickUserOutOfSeat(
                    seatUserInfo!!.userID,
                    completionHandler {
                        onError { code, _ ->
                            ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                        }
                    })
                dismiss()
            }
            confirmDialog!!.show()
            return
        }

        if (isSelfUser()) {
            confirmDialog!!.setContent(context.getString(R.string.common_terminate_room_connection_message))
            confirmDialog!!.setPositiveText(context.getString(R.string.common_disconnection))
            confirmDialog!!.setPositiveListener {
                audienceManager.getCoGuestStore().disconnect(null)
                audienceManager.getViewStore().updateTakeSeatState(false)
                dismiss()
            }
            confirmDialog!!.show()
        }
    }

    private fun onMicrophoneButtonClicked() {
        if (seatUserInfo == null) {
            return
        }

        if (isSelfUser()) {
            val isSelfMicrophoneOpened = audienceManager.getLiveSeatState().seatList.value.find {
                it.userInfo.userID ==
                        TUIRoomEngine.getSelfInfo().userId
            }?.userInfo?.microphoneStatus == DeviceStatus.ON
            if (isSelfMicrophoneOpened) {
                audienceManager.getLiveSeatStore().muteMicrophone()
            } else {
                unMuteMicrophone()
            }
            dismiss()
            return
        }

        if (isAdmin()) {
            audienceManager.getLiveSeatStore().closeRemoteMicrophone(seatUserInfo?.userID, null)
            dismiss()
        }
    }

    private fun onCameraButtonClicked() {
        if (seatUserInfo == null) {
            return
        }

        if (isSelfUser()) {
            val isSelfCameraOpened = audienceManager.getLiveSeatState().seatList.value.find {
                it.userInfo.userID == TUIRoomEngine.getSelfInfo().userId
            }?.userInfo?.cameraStatus == DeviceStatus.ON
            if (isSelfCameraOpened) {
                audienceManager.getDeviceStore().closeLocalCamera()
                flipCameraContainer.visibility = GONE
            } else {
                startCamera()
            }
            dismiss()
            return
        }

        if (isAdmin()) {
            audienceManager.getLiveSeatStore().closeRemoteCamera(seatUserInfo?.userID, null)
            dismiss()
        }
    }

    private fun onSwitchCameraButtonClicked() {
        val isFront = audienceManager.getDeviceStore().deviceState.isFrontCamera.value
        audienceManager.getDeviceStore().switchCamera(!isFront)
        dismiss()
    }

    private fun unMuteMicrophone() {
        audienceManager.getLiveSeatStore().unmuteMicrophone(completionHandler {
            onError { code, _ ->
                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
            }
        })
    }

    private fun startCamera() {
        val isFrontCamera = audienceManager.getDeviceStore().deviceState.isFrontCamera.value
        PermissionRequest.requestCameraPermissions(
            ContextProvider.getApplicationContext(),
            object : PermissionCallback() {
                override fun onRequesting() {
                    LOGGER.info("requestCameraPermissions:[onRequesting]")
                }

                override fun onGranted() {
                    LOGGER.info("requestCameraPermissions:[onGranted]")
                    audienceManager.getDeviceStore()
                        .openLocalCamera(isFrontCamera, completionHandler {
                            onError { code, _ ->
                                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                            }
                        })
                }
            })
    }

    private fun updateMicrophoneButton(isAudioLocked: Boolean) {
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
            return
        }

        if (isAudioLocked == true) {
            audioContainer.isEnabled = false
            ivAudio.alpha = BUTTON_DISABLE_ALPHA
            ivAudio.setImageResource(R.drawable.livekit_ic_mute_audio)
            tvAudio.setText(R.string.common_unmute_audio)
            return
        }

        audioContainer.isEnabled = true
        ivAudio.alpha = BUTTON_ENABLE_ALPHA
        val isMicrophoneMuted =
            audienceManager.getLiveSeatState().seatList.value.find {
                it.userInfo.userID == TUIRoomEngine.getSelfInfo().userId
            }?.userInfo?.microphoneStatus == DeviceStatus.OFF
        if (isMicrophoneMuted) {
            ivAudio.setImageResource(R.drawable.livekit_ic_mute_audio)
            tvAudio.setText(R.string.common_unmute_audio)
        } else {
            ivAudio.setImageResource(R.drawable.livekit_ic_unmute_audio)
            tvAudio.setText(R.string.common_mute_audio)
        }
    }

    private fun updateCameraButton(isVideoLocked: Boolean) {
        if (!isSelfUser()) {
            if (isAdmin()) {
                videoContainer.isEnabled = true
                ivVideo.alpha = BUTTON_ENABLE_ALPHA
                ivVideo.setImageResource(
                    if (isVideoLocked == true) R.drawable.livekit_ic_disable_video
                    else R.drawable.livekit_ic_start_video
                )
                tvVideo.setText(
                    if (isVideoLocked == true) R.string.common_enable_video
                    else R.string.common_disable_video
                )
            }
            return
        }


        if (isVideoLocked == true) {
            videoContainer.isEnabled = false
            ivVideo.alpha = BUTTON_DISABLE_ALPHA
            ivVideo.setImageResource(R.drawable.livekit_ic_stop_video)
            tvVideo.setText(R.string.common_start_video)
            flipCameraContainer.visibility = GONE
            return
        }

        videoContainer.isEnabled = true
        ivVideo.alpha = BUTTON_ENABLE_ALPHA
        val isCameraOpened =
            audienceManager.getLiveSeatState().seatList.value.find {
                it.userInfo.userID == TUIRoomEngine.getSelfInfo().userId
            }?.userInfo?.cameraStatus == DeviceStatus.ON
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

    private fun onConnectUserListChanged(connectedUserList: List<SeatUserInfo>) {
        if (seatUserInfo == null) {
            return
        }
        if (TextUtils.isEmpty(seatUserInfo!!.userID)) {
            return
        }
        var isConnected = false
        for (userInfo in connectedUserList) {
            if (this.seatUserInfo!!.userID == userInfo.userID) {
                isConnected = true
                break
            }
        }
        if (!isConnected) {
            dismiss()
            confirmDialog?.dismiss()
        }
    }

    private val liveLisListener = object : LiveListListener() {
        override fun onLiveEnded(liveID: String, reason: LiveEndedReason, message: String) {
            dismiss()
            confirmDialog?.dismiss()
        }
    }

    private val liveAudienceListener = object : LiveAudienceListener() {
        override fun onAudienceLeft(audience: LiveUserInfo) {
            if (this@AnchorManagerDialog.seatUserInfo == null) {
                return
            }
            if (TextUtils.isEmpty(this@AnchorManagerDialog.seatUserInfo!!.userID)) {
                return
            }
            if (audience.userID == this@AnchorManagerDialog.seatUserInfo!!.userID) {
                dismiss()
                confirmDialog?.dismiss()
            }
        }
    }

    private val coGuestListener = object : GuestListener() {
        override fun onKickedOffSeat(seatIndex: Int, hostUser: LiveUserInfo) {
            dismiss()
            confirmDialog?.dismiss()
        }
    }

    companion object {
        private val LOGGER = LiveKitLogger.getLiveStreamLogger("AnchorManagerDialog")
        private const val BUTTON_DISABLE_ALPHA = 0.24f
        private const val BUTTON_ENABLE_ALPHA = 1.0f
    }
}
