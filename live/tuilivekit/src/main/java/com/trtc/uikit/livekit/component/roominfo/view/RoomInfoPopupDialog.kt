package com.trtc.uikit.livekit.component.roominfo.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.component.roominfo.service.RoomInfoService
import com.trtc.uikit.livekit.component.roominfo.store.RoomInfoState

@SuppressLint("ViewConstructor")
class RoomInfoPopupDialog(
    context: Context,
    private val roomInfoService: RoomInfoService
) : PopupDialog(context) {
    private val roomInfoState: RoomInfoState = roomInfoService.roomInfoState
    private lateinit var buttonFollow: Button
    private lateinit var textOwnerName: TextView
    private lateinit var textRoomId: TextView
    private lateinit var imageAvatar: ImageView
    private lateinit var textFans: TextView
    private lateinit var fansLayout: View

    private val followStatusObserver = Observer<Set<String>> { userInfo ->
        onFollowStatusChange(userInfo)
    }

    private val ownerFansCountObserver = Observer<Long> { fansCount ->
        onFansNumberChange(fansCount)
    }

    private val ownerIdObserver = Observer<String> { ownerId ->
        onHostChange(ownerId)
    }

    init {
        initView()
    }

    private fun addObserver() {
        roomInfoState.followingList.observeForever(followStatusObserver)
        roomInfoState.fansNumber.observeForever(ownerFansCountObserver)
        roomInfoState.ownerId.observeForever(ownerIdObserver)
    }

    private fun removeObserver() {
        roomInfoState.followingList.removeObserver(followStatusObserver)
        roomInfoState.fansNumber.removeObserver(ownerFansCountObserver)
        roomInfoState.ownerId.removeObserver(ownerIdObserver)
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.room_info_detail_panel, null)
        bindViewId(view)
        initAnchorNameView()
        initRoomIdView()
        initAvatarView()
        initFansView()
        setView(view)
    }

    private fun bindViewId(view: View) {
        buttonFollow = view.findViewById(R.id.btn_follow)
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
        textOwnerName.text = if (ownerName.isNullOrEmpty()) ownerId else ownerName
    }

    private fun initRoomIdView() {
        textRoomId.text = roomInfoState.roomId
    }

    private fun initAvatarView() {
        ImageLoader.load(
            context,
            imageAvatar,
            roomInfoState.ownerAvatarUrl.value,
            R.drawable.room_info_default_avatar
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

        val followingList = roomInfoState.followingList.value ?: emptySet()
        val ownerId = roomInfoState.ownerId.value

        if (followingList.contains(ownerId)) {
            buttonFollow.setText(R.string.common_unfollow_anchor)
            buttonFollow.setBackgroundResource(R.drawable.room_info_detail_button_unfollow)
        } else {
            buttonFollow.setText(R.string.common_follow_anchor)
            buttonFollow.setBackgroundResource(R.drawable.room_info_button_follow)
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
        
        buttonFollow.setOnClickListener { onFollowButtonClick() }
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