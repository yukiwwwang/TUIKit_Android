package com.trtc.uikit.livekit.component.roominfo.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.setDebounceClickListener
import com.trtc.uikit.livekit.component.roominfo.service.RoomInfoService
import com.trtc.uikit.livekit.component.roominfo.store.RoomInfoState
import io.trtc.tuikit.atomicx.widget.basicwidget.avatar.AtomicAvatar
import io.trtc.tuikit.atomicx.widget.basicwidget.button.AtomicButton
import io.trtc.tuikit.atomicx.widget.basicwidget.button.ButtonColorType
import io.trtc.tuikit.atomicx.widget.basicwidget.button.ButtonIconPosition
import io.trtc.tuikit.atomicx.widget.basicwidget.button.ButtonVariant
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
class RoomInfoPanel(
    context: Context,
    private val roomInfoService: RoomInfoService
) : AtomicPopover(context) {
    private val roomInfoState: RoomInfoState = roomInfoService.roomInfoState
    private lateinit var buttonFollow: AtomicButton
    private lateinit var textOwnerName: TextView
    private lateinit var textRoomId: TextView
    private lateinit var imageAvatar: AtomicAvatar
    private lateinit var textFans: TextView
    private lateinit var fansLayout: View
    private var subscribeStateJob: Job? = null

    init {
        setTransparentBackground(true)
        initView()
    }

    private fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                roomInfoState.followingList.collect {
                    onFollowStatusChange(it)
                }
            }

            launch {
                roomInfoState.fansNumber.collect {
                    onFansNumberChange(it)
                }
            }

            launch {
                roomInfoState.ownerId.collect {
                    onHostChange(it)
                }
            }
        }
    }

    private fun removeObserver() {
        subscribeStateJob?.cancel()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.room_info_detail_panel, null)
        bindViewId(view)
        initAnchorNameView()
        initRoomIdView()
        initAvatarView()
        initFansView()
        setContent(view)
    }

    private fun bindViewId(view: View) {
        buttonFollow = view.findViewById(R.id.atomic_btn_follow)
        textOwnerName = view.findViewById(R.id.tv_anchor_name)
        textRoomId = view.findViewById(R.id.tv_liveroom_id)
        imageAvatar = view.findViewById(R.id.iv_avatar)
        textFans = view.findViewById(R.id.tv_fans)
        fansLayout = view.findViewById(R.id.ll_fans)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addObserver()
        getFansNumber()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }

    private fun initAnchorNameView() {
        val ownerName = roomInfoState.ownerName.value
        val ownerId = roomInfoState.ownerId.value
        textOwnerName.text = ownerName.ifEmpty { ownerId }
    }

    private fun initRoomIdView() {
        textRoomId.text = roomInfoState.roomId
    }

    private fun initAvatarView() {
        imageAvatar.setContent(
            AtomicAvatar.AvatarContent.URL(
                roomInfoState.ownerAvatarUrl.value,
                R.drawable.room_info_default_avatar
            )
        )
    }

    private fun initFansView() {
        fansLayout.visibility = if (roomInfoState.enableFollow) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun getFansNumber() {
        roomInfoService.getFansNumber()
    }

    private fun refreshFollowButton() {
        if (!roomInfoState.enableFollow) {
            buttonFollow.visibility = View.GONE
            return
        }

        val followingList = roomInfoState.followingList.value
        val ownerId = roomInfoState.ownerId.value
        val isFollowed = followingList.contains(ownerId)
        buttonFollow.visibility = View.VISIBLE
        if (isFollowed) {
            buttonFollow.apply {
                text = context.getString(R.string.common_unfollow_anchor)
                colorType = ButtonColorType.SECONDARY
            }
        } else {
            buttonFollow.apply {
                text = context.getString(R.string.common_follow_anchor)
                colorType = ButtonColorType.PRIMARY
            }
        }
    }

    private fun onFollowStatusChange(userInfo: Set<String>?) {
        refreshFollowButton()
    }

    private fun onFansNumberChange(fansCount: Long?) {
        textFans.text = fansCount?.toString() ?: "0"
    }

    private fun onHostChange(ownerId: String?) {
        if (!roomInfoState.enableFollow) {
            return
        }

        if (TextUtils.isEmpty(ownerId)) {
            return
        }

        if (TextUtils.equals(roomInfoState.selfUserId, ownerId)) {
            buttonFollow.text = ""
            buttonFollow.visibility = View.GONE
        } else {
            ownerId?.let { roomInfoService.checkFollowUser(it) }
            refreshFollowButton()
        }

        buttonFollow.setDebounceClickListener { onFollowButtonClick() }
    }

    private fun onFollowButtonClick() {
        val followingList = roomInfoState.followingList.value ?: emptySet()
        val ownerId = roomInfoState.ownerId.value

        ownerId?.let { id ->
            if (followingList.contains(id)) {
                roomInfoService.unfollowUser(id)
            } else {
                roomInfoService.followUser(id)
            }
        }
    }
}