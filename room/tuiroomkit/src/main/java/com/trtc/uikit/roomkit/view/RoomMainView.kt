package com.trtc.uikit.roomkit.view

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import com.trtc.uikit.roomkit.R
import com.trtc.uikit.roomkit.base.error.ErrorLocalized
import com.trtc.uikit.roomkit.base.extension.getSenderDisplayName
import com.trtc.uikit.roomkit.base.log.RoomKitLogger
import com.trtc.uikit.roomkit.base.operator.DeviceOperator
import com.trtc.uikit.roomkit.base.ui.BaseView
import com.trtc.uikit.roomkit.base.ui.RoomAlertDialog
import com.trtc.uikit.roomkit.view.main.RoomBottomBarView
import com.trtc.uikit.roomkit.view.main.RoomTopBarView
import com.trtc.uikit.roomkit.view.main.RoomView
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.ListResultCompletionHandler
import io.trtc.tuikit.atomicxcore.api.device.AudioRoute
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.device.DeviceType
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.atomicxcore.api.room.CreateRoomOptions
import io.trtc.tuikit.atomicxcore.api.room.DeviceRequestInfo
import io.trtc.tuikit.atomicxcore.api.room.KickedOutOfRoomReason
import io.trtc.tuikit.atomicxcore.api.room.RoomInfo
import io.trtc.tuikit.atomicxcore.api.room.RoomListener
import io.trtc.tuikit.atomicxcore.api.room.RoomParticipant
import io.trtc.tuikit.atomicxcore.api.room.RoomParticipantListener
import io.trtc.tuikit.atomicxcore.api.room.RoomParticipantStore
import io.trtc.tuikit.atomicxcore.api.room.RoomStore
import io.trtc.tuikit.atomicxcore.api.room.RoomUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Main room view orchestrating all room UI components and handling room lifecycle.
 * Manages room connection, device controls, participant events, and dialog interactions.
 */
class RoomMainView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseView(context, attrs, defStyleAttr) {

    sealed class RoomBehavior {
        data class Create(val options: CreateRoomOptions) : RoomBehavior()
        object Join : RoomBehavior()
    }

    data class ConnectConfig(
        val autoEnableMicrophone: Boolean = true,
        val autoEnableCamera: Boolean = true,
        val autoEnableSpeaker: Boolean = false
    )

    private val logger = RoomKitLogger.getLogger("RoomMainView")

    private val scope = CoroutineScope(Dispatchers.Main)
    private val deviceOperator by lazy { DeviceOperator(context) }

    private val topBarView: RoomTopBarView by lazy { findViewById(R.id.room_top_bar) }
    private val roomView: RoomView by lazy { findViewById(R.id.room_view) }
    private val bottomBarView: RoomBottomBarView by lazy { findViewById(R.id.room_bottom_bar) }

    private val roomStore = RoomStore.shared()
    private val deviceStore = DeviceStore.shared()
    private var participantStore: RoomParticipantStore? = null
    private var cameraInvitationDialog: Dialog? = null
    private var microphoneInvitationDialog: Dialog? = null
    private var localUserID = LoginStore.shared.loginState.loginUserInfo.value?.userID
    private var connectConfig: ConnectConfig? = null

    private val participantListener = object : RoomParticipantListener() {
        override fun onDeviceInvitationReceived(invitation: DeviceRequestInfo) {
            logger.info("Device invitation received: device=${invitation.device}, from=${invitation.senderUserID}")
            when (invitation.device) {
                DeviceType.MICROPHONE -> showMicrophoneInvitationDialog(invitation)
                DeviceType.CAMERA -> showCameraInvitationDialog(invitation)
                else -> Unit
            }
        }

        override fun onDeviceInvitationCancelled(invitation: DeviceRequestInfo) {
            logger.info("Device invitation cancelled: device=${invitation.device}, from=${invitation.senderUserID}")
            when (invitation.device) {
                DeviceType.MICROPHONE -> dismissMicrophoneInvitationDialog()
                DeviceType.CAMERA -> dismissCameraInvitationDialog()
                else -> Unit
            }
        }

        override fun onDeviceInvitationTimeout(invitation: DeviceRequestInfo) {
            logger.info("Device invitation timeout: device=${invitation.device}, from=${invitation.senderUserID}")
            when (invitation.device) {
                DeviceType.MICROPHONE -> dismissMicrophoneInvitationDialog()
                DeviceType.CAMERA -> dismissCameraInvitationDialog()
                else -> Unit
            }
        }

        override fun onKickedFromRoom(reason: KickedOutOfRoomReason, message: String) {
            logger.info("onKickedFromRoom: reason=$reason, from=$message")
            showKickoutDialog()
        }

        override fun onOwnerChanged(newOwner: RoomUser, oldOwner: RoomUser) {
            logger.info("onOwnerChanged: newOwner=${newOwner.userID} oldOwner=${oldOwner.userID}")
            if (localUserID == newOwner.userID) {
                Toast.makeText(context, R.string.roomkit_toast_you_are_owner, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onAdminSet(userInfo: RoomUser) {
            logger.info("onAdminSet: userInfo=$userInfo")
            if (localUserID == userInfo.userID) {
                Toast.makeText(context, R.string.roomkit_toast_you_are_admin, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onAdminRevoked(userInfo: RoomUser) {
            logger.info("onAdminRevoked: userInfo=$userInfo")
            if (localUserID == userInfo.userID) {
                Toast.makeText(context, R.string.roomkit_toast_you_are_no_longer_admin, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onParticipantDeviceClosed(device: DeviceType, operator: RoomUser) {
            logger.info("onParticipantDeviceClosed: device=$device operator:$operator")
            when (device) {
                DeviceType.CAMERA -> Toast.makeText(
                    context,
                    R.string.roomkit_toast_camera_closed_by_host,
                    Toast.LENGTH_SHORT
                ).show()

                DeviceType.MICROPHONE -> Toast.makeText(
                    context,
                    R.string.roomkit_toast_muted_by_host,
                    Toast.LENGTH_SHORT
                ).show()

                else -> Unit
            }
        }

        override fun onAllDevicesDisabled(device: DeviceType, disable: Boolean, operator: RoomUser) {
            logger.info("onAllDevicesDisabled: device=$device disable:$disable operator:$operator")
            when (device) {
                DeviceType.CAMERA -> {
                    val messageId =
                        if (disable) R.string.roomkit_toast_all_video_disabled else R.string.roomkit_toast_all_video_enabled
                    Toast.makeText(context, context.getString(messageId), Toast.LENGTH_SHORT).show()
                }

                DeviceType.MICROPHONE -> {
                    val messageId =
                        if (disable) R.string.roomkit_toast_all_audio_disabled else R.string.roomkit_toast_all_audio_enabled
                    Toast.makeText(context, context.getString(messageId), Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }

    private val roomListener = object : RoomListener() {
        override fun onRoomEnded(roomInfo: RoomInfo) {
            logger.info("Room ended: roomID=${roomInfo.roomID}, roomName=${roomInfo.roomName}")
            showRoomDismissedDialog()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.roomkit_view_main, this)
    }

    fun init(roomID: String, behavior: RoomBehavior, config: ConnectConfig) {
        super.init(roomID)
        roomView.init(roomID)
        topBarView.init(roomID)
        bottomBarView.init(roomID)
        connectConfig = config
        when (behavior) {
            is RoomBehavior.Create -> createRoom(roomID, behavior.options)
            is RoomBehavior.Join -> joinRoom(roomID)
        }
    }

    override fun initStore(roomID: String) {
        participantStore = RoomParticipantStore.create(roomID)
    }

    override fun addObserver() {
        participantStore?.addRoomParticipantListener(participantListener)
        roomStore.addRoomListener(roomListener)
    }

    override fun removeObserver() {
        participantStore?.removeRoomParticipantListener(participantListener)
        roomStore.removeRoomListener(roomListener)
        dismissCameraInvitationDialog()
        dismissMicrophoneInvitationDialog()
        scope.cancel()
    }

    private fun createRoom(roomID: String, createRoomOptions: CreateRoomOptions) {
        roomStore.createAndJoinRoom(roomID, createRoomOptions, object : CompletionHandler {
            override fun onSuccess() {
                logger.info("createAndJoinRoom success ${roomStore.state.currentRoom.value}")
                getParticipantList()
                connectConfig?.let {
                    initConnectConfig(it)
                }
            }

            override fun onFailure(code: Int, desc: String) {
                logger.error("createAndJoinRoom failed:error:$code,desc:$desc")
                ErrorLocalized.showError(context, code)
                (context as? Activity)?.finish()
            }
        })
    }

    private fun joinRoom(roomID: String) {
        roomStore.joinRoom(roomID = roomID, completion = object : CompletionHandler {
            override fun onSuccess() {
                logger.info("joinRoom success ${roomStore.state.currentRoom.value}")
                getParticipantList()
                connectConfig?.let {
                    initConnectConfig(it)
                }
            }

            override fun onFailure(code: Int, desc: String) {
                logger.error("joinRoom failed:error:$code,desc:$desc")
                ErrorLocalized.showError(context, code)
                (context as? Activity)?.finish()
            }
        })
    }

    private fun getParticipantList() {
        logger.info("Store instance: ${participantStore.hashCode()}")

        participantStore?.getParticipantList("", object : ListResultCompletionHandler<RoomParticipant> {
            override fun onSuccess(result: List<RoomParticipant>, cursor: String) {
                logger.info("getParticipantList success result size:${result.size} cursor:$cursor")
            }

            override fun onFailure(code: Int, desc: String) {
                logger.error("getParticipantList failed:error:$code,desc:$desc")
                ErrorLocalized.showError(context, code)
            }
        })
    }

    private fun initConnectConfig(config: ConnectConfig) {
        scope.launch {
            if (config.autoEnableMicrophone) {
                try {
                    deviceOperator.unmuteMicrophone(participantStore)
                } catch (e: Exception) {
                    logger.error("Failed to open microphone: ${e.message}")
                }
            }

            if (config.autoEnableCamera) {
                try {
                    deviceOperator.openCamera()
                } catch (e: Exception) {
                    logger.error("Failed to open camera: ${e.message}")
                }
            }
        }
        enableSpeaker(config.autoEnableSpeaker)
    }

    private fun enableSpeaker(enableSpeaker: Boolean) {
        val audioRoute = if (enableSpeaker) AudioRoute.SPEAKERPHONE else AudioRoute.EARPIECE
        deviceStore.setAudioRoute(audioRoute)
        logger.info("Speaker enabled (SPEAKERPHONE mode)")
    }

    private fun showCameraInvitationDialog(invitation: DeviceRequestInfo) {
        if (cameraInvitationDialog != null) {
            cameraInvitationDialog?.dismiss()
            microphoneInvitationDialog = null
        }
        val title = context.getString(R.string.roomkit_msg_invite_start_video, invitation.getSenderDisplayName())
        cameraInvitationDialog = buildDeviceInvitationDialog(title, invitation)
        cameraInvitationDialog?.show()
    }

    private fun showMicrophoneInvitationDialog(invitation: DeviceRequestInfo) {
        if (microphoneInvitationDialog != null) {
            microphoneInvitationDialog?.dismiss()
            microphoneInvitationDialog = null
        }
        val title = context.getString(R.string.roomkit_msg_invite_unmute_audio, invitation.getSenderDisplayName())
        microphoneInvitationDialog = buildDeviceInvitationDialog(title, invitation)
        microphoneInvitationDialog?.show()
    }

    private fun dismissCameraInvitationDialog() {
        if (cameraInvitationDialog != null) {
            cameraInvitationDialog?.dismiss()
            cameraInvitationDialog = null
        }
    }

    private fun dismissMicrophoneInvitationDialog() {
        if (microphoneInvitationDialog != null) {
            microphoneInvitationDialog?.dismiss()
            microphoneInvitationDialog = null
        }
    }

    private fun buildDeviceInvitationDialog(tile: String, invitation: DeviceRequestInfo): Dialog {
        return RoomAlertDialog.Builder(context)
            .setTitle(tile)
            .setNegativeButton(R.string.roomkit_reject) { handleDeclineInvitation(invitation) }
            .setPositiveButton(R.string.roomkit_agree) { handleAcceptInvitation(invitation) }
            .build()
    }

    private fun handleAcceptInvitation(invitation: DeviceRequestInfo) {
        val device = invitation.device
        logger.info("Accepting $device invitation from ${invitation.senderUserID}")
        scope.launch {
            try {
                val hasPermission = when (device) {
                    DeviceType.MICROPHONE -> deviceOperator.requestPermission(Manifest.permission.RECORD_AUDIO)
                    DeviceType.CAMERA -> deviceOperator.requestPermission(Manifest.permission.CAMERA)
                    else -> {
                        logger.warn("Unsupported device type: $device")
                        false
                    }
                }
                
                logger.info("requestPermission result hasPermission: $hasPermission")
                
                if (hasPermission) {
                    val store = participantStore
                    if (store == null) {
                        logger.error("participantStore is null, cannot accept invitation")
                        return@launch
                    }
                    
                    store.acceptOpenDeviceInvitation(
                        userID = invitation.senderUserID,
                        device = device,
                        completion = object : CompletionHandler {
                            override fun onSuccess() {
                                logger.info("Successfully accepted $device invitation")
                            }

                            override fun onFailure(code: Int, desc: String) {
                                logger.error("Failed to accept $device invitation: code=$code, desc=$desc")
                                ErrorLocalized.showError(context, code)
                            }
                        }
                    )
                } else {
                    handleDeclineInvitation(invitation)
                }
            } catch (e: Exception) {
                logger.error("Error handling invitation acceptance: ${e.message}")
                handleDeclineInvitation(invitation)
            }
        }
    }

    private fun handleDeclineInvitation(invitation: DeviceRequestInfo) {
        logger.info("Declining ${invitation.device} invitation from ${invitation.senderUserID}")
        participantStore?.declineOpenDeviceInvitation(
            userID = invitation.senderUserID,
            device = invitation.device,
            completion = object : CompletionHandler {
                override fun onSuccess() {
                    logger.info("Successfully declined ${invitation.device} invitation")
                }

                override fun onFailure(code: Int, desc: String) {
                    logger.error("Failed to decline ${invitation.device} invitation: code=$code, desc=$desc")
                    ErrorLocalized.showError(context, code)
                }
            }
        )
    }

    private fun showRoomDismissedDialog() {
        RoomAlertDialog.Builder(context)
            .setTitle(R.string.roomkit_toast_room_closed)
            .setPositiveButton(android.R.string.ok) {
                (context as? Activity)?.finish()
            }
            .show()
    }

    private fun showKickoutDialog() {
        RoomAlertDialog.Builder(context)
            .setTitle(R.string.roomkit_toast_you_were_removed)
            .setPositiveButton(android.R.string.ok) {
                (context as? Activity)?.finish()
            }
            .show()
    }
}