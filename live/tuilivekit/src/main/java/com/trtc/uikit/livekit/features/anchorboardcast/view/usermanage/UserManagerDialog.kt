package com.trtc.uikit.livekit.features.anchorboardcast.view.usermanage

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.common.ui.setDebounceClickListener
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import com.trtc.uikit.livekit.features.anchorboardcast.state.UserState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UserManagerDialog(
    private val context: Context,
    private val liveStreamManager: AnchorManager
) : PopupDialog(context) {

    private var userInfo: UserState.UserInfo? = null
    private lateinit var imageHeadView: ImageFilterView
    private lateinit var userIdText: TextView
    private lateinit var userNameText: TextView
    private lateinit var ivDisableMessage: ImageView
    private lateinit var tvDisableMessage: TextView
    private lateinit var textUnfollow: TextView
    private lateinit var imageFollowIcon: ImageView
    private var confirmDialog: ConfirmDialog? = null
    private var subscribeStateJob: Job? = null

    private val tuiRoomObserver = object : TUIRoomObserver() {
        override fun onKickedOffSeat(seatIndex: Int, operateUser: TUIRoomDefine.UserInfo?) {
            dismiss()
            confirmDialog?.dismiss()
        }

        override fun onRemoteUserLeaveRoom(roomId: String?, userInfo: TUIRoomDefine.UserInfo?) {
            if (this@UserManagerDialog.userInfo == null || userInfo == null) {
                return
            }
            if (TextUtils.isEmpty(this@UserManagerDialog.userInfo?.userId) ||
                TextUtils.isEmpty(userInfo.userId)
            ) {
                return
            }
            if (userInfo.userId == this@UserManagerDialog.userInfo?.userId) {
                dismiss()
                confirmDialog?.dismiss()
            }
        }
    }

    init {
        initView()
    }

    fun init(userInfo: TUIRoomDefine.UserInfo) {
        this.userInfo = liveStreamManager.getUserManager().getUserFromUserList(userInfo.userId)
        if (this.userInfo == null) {
            this.userInfo = liveStreamManager.getUserManager().addUserInUserList(userInfo)
        }
        updateView()
        liveStreamManager.getUserManager().checkFollowUser(userInfo.userId)
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
        val rootView = View.inflate(context, R.layout.livekit_user_manager, null)
        setView(rootView)
        bindViewId(rootView)
        initFollowButtonView(rootView)
    }

    private fun bindViewId(rootView: View) {
        userIdText = rootView.findViewById(R.id.user_id)
        userNameText = rootView.findViewById(R.id.user_name)
        imageHeadView = rootView.findViewById(R.id.iv_head)
        ivDisableMessage = rootView.findViewById(R.id.iv_disable_message)
        tvDisableMessage = rootView.findViewById(R.id.tv_disable_message)
        textUnfollow = rootView.findViewById(R.id.tv_unfollow)
        imageFollowIcon = rootView.findViewById(R.id.iv_follow)

        rootView.findViewById<View>(R.id.disable_message_container)
            .setOnClickListener { onDisableMessageButtonClicked() }
        rootView.findViewById<View>(R.id.kick_out_room_container)
            .setOnClickListener { onKickUserButtonClicked() }
    }

    private fun initFollowButtonView(rootView: View) {
        rootView.findViewById<View>(R.id.fl_follow_panel).setDebounceClickListener {
            val currentUserInfo = userInfo ?: return@setDebounceClickListener
            val followingUsers = liveStreamManager.getUserState().followingUserList.value
            if (followingUsers.contains(currentUserInfo.userId)) {
                liveStreamManager.getUserManager().unfollowUser(currentUserInfo.userId)
            } else {
                liveStreamManager.getUserManager().followUser(currentUserInfo.userId)
            }
        }
    }

    private fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                onFollowingUserChanged()
            }
            launch {
                updateDisableMessageButton()
            }
        }
        TUIRoomEngine.sharedInstance().addObserver(tuiRoomObserver)
    }

    private fun removeObserver() {
        subscribeStateJob?.cancel()
        TUIRoomEngine.sharedInstance().removeObserver(tuiRoomObserver)
    }

    private fun updateView() {
        val currentUserInfo = userInfo ?: return

        if (TextUtils.isEmpty(currentUserInfo.userId)) {
            return
        }

        userIdText.text = context.getString(R.string.common_user_id, currentUserInfo.userId)

        val name = if (TextUtils.isEmpty(currentUserInfo.name.value)) {
            currentUserInfo.userId
        } else {
            currentUserInfo.name.value
        }
        userNameText.text = name

        if (TextUtils.isEmpty(currentUserInfo.avatarUrl.value)) {
            imageHeadView.setImageResource(R.drawable.livekit_ic_avatar)
        } else {
            ImageLoader.load(
                context,
                imageHeadView,
                currentUserInfo.avatarUrl.value,
                R.drawable.livekit_ic_avatar
            )
        }
    }

    private suspend fun updateDisableMessageButton() {
        userInfo?.isMessageDisabled?.collect {
            if (it) {
                ivDisableMessage.setImageResource(R.drawable.livekit_ic_disable_message)
                tvDisableMessage.setText(R.string.common_enable_message)
            } else {
                ivDisableMessage.setImageResource(R.drawable.livekit_ic_enable_message)
                tvDisableMessage.setText(R.string.common_disable_message)
            }
        }
    }

    private suspend fun onFollowingUserChanged() {
        liveStreamManager.getUserState().followingUserList.collect { followUsers ->
            userInfo?.let { currentUserInfo ->
                if (followUsers.contains(currentUserInfo.userId)) {
                    textUnfollow.visibility = View.GONE
                    imageFollowIcon.visibility = View.VISIBLE
                } else {
                    imageFollowIcon.visibility = View.GONE
                    textUnfollow.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun onDisableMessageButtonClicked() {
        val currentUserInfo = userInfo ?: return
        val isMessageDisabled = currentUserInfo.isMessageDisabled.value
        liveStreamManager.getUserManager().disableSendingMessageByAdmin(
            currentUserInfo.userId,
            !isMessageDisabled
        )
    }

    private fun onKickUserButtonClicked() {
        val currentUserInfo = userInfo ?: return

        if (confirmDialog == null) {
            confirmDialog = ConfirmDialog(context)
        }

        val name = if (TextUtils.isEmpty(currentUserInfo.name.value)) {
            currentUserInfo.userId
        } else {
            currentUserInfo.name.value
        }

        confirmDialog?.apply {
            setContent(context.getString(R.string.common_kick_user_confirm_message, name))
            setPositiveText(context.getString(R.string.common_kick_out_of_room))
            setPositiveListener {
                liveStreamManager.getUserManager().kickRemoteUserOutOfRoom(currentUserInfo.userId)
                dismiss()
            }
            show()
        }
    }
}