package com.trtc.uikit.livekit.component.roominfo

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.material.imageview.ShapeableImageView
import com.tencent.cloud.tuikit.engine.extension.TUILiveListManager
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.reportEventData
import com.trtc.uikit.livekit.component.roominfo.service.RoomInfoService
import com.trtc.uikit.livekit.component.roominfo.store.RoomInfoState
import com.trtc.uikit.livekit.component.roominfo.view.RoomInfoPopupDialog
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo

@SuppressLint("ViewConstructor")
class LiveInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val LIVEKIT_METRICS_PANEL_SHOW_LIVE_ROOM_LIVE_INFO = 190009
        private const val LIVEKIT_METRICS_PANEL_SHOW_VOICE_ROOM_LIVE_INFO = 191008
    }
    private lateinit var textNickName: TextView
    private lateinit var imageAvatar: ShapeableImageView
    private lateinit var layoutRoot: LinearLayout
    private lateinit var textUnfollow: TextView
    private lateinit var imageFollowIcon: ImageView
    private lateinit var layoutFollowPanel: FrameLayout
    private var roomInfoPopupDialog: RoomInfoPopupDialog? = null
    private val roomInfoService = RoomInfoService()
    private val roomInfoState: RoomInfoState = roomInfoService.roomInfoState

    private val hostIdObserver = Observer<String> { ownerId ->
        onHostIdChange(ownerId)
    }

    private val hostNickNameObserver = Observer<String> { name ->
        onHostNickNameChange(name)
    }

    private val hostAvatarObserver = Observer<String> { avatar ->
        onHostAvatarChange(avatar)
    }

    private val followStatusObserver = Observer<Set<String>> { followUsers ->
        onFollowStatusChange(followUsers)
    }

    private val roomObserver = object : TUIRoomObserver() {
        override fun onRoomDismissed(roomId: String, reason: TUIRoomDefine.RoomDismissedReason) {
            roomInfoPopupDialog?.dismiss()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.room_info_view, this, true)
        bindViewId()
    }

    fun init(liveInfo: LiveInfo) {
        init(liveInfo, true)
    }

    fun init(liveInfo: LiveInfo, enableFollow: Boolean) {
        roomInfoService.init(liveInfo)
        roomInfoState.enableFollow = enableFollow
        reportData(liveInfo.liveID)
        refreshView()
    }

    fun unInit() {
        roomInfoService.unInit()
    }

    fun setScreenOrientation(isPortrait: Boolean) {
        layoutRoot.isEnabled = isPortrait
    }

    private fun convertToLiveInfo(roomInfo: TUIRoomDefine.RoomInfo): TUILiveListManager.LiveInfo {
        return TUILiveListManager.LiveInfo().apply {
            roomId = roomInfo.roomId
            name = roomInfo.name
            ownerId = roomInfo.ownerId
            ownerName = roomInfo.ownerName
            ownerAvatarUrl = roomInfo.ownerAvatarUrl
        }
    }

    private fun initView() {
        initHostNameView()
        initHostAvatarView()
        initRoomInfoPanelView()
    }

    private fun refreshView() {
        if (!roomInfoState.enableFollow) {
            layoutFollowPanel.visibility = GONE
        }
    }

    private fun addObserver() {
        TUIRoomEngine.sharedInstance().addObserver(roomObserver)
        roomInfoState.ownerId.observeForever(hostIdObserver)
        roomInfoState.ownerName.observeForever(hostNickNameObserver)
        roomInfoState.ownerAvatarUrl.observeForever(hostAvatarObserver)
        roomInfoState.followingList.observeForever(followStatusObserver)
    }

    private fun removeObserver() {
        TUIRoomEngine.sharedInstance().removeObserver(roomObserver)
        roomInfoState.ownerId.removeObserver(hostIdObserver)
        roomInfoState.ownerName.removeObserver(hostNickNameObserver)
        roomInfoState.ownerAvatarUrl.removeObserver(hostAvatarObserver)
        roomInfoState.followingList.removeObserver(followStatusObserver)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initView()
        addObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }

    private fun bindViewId() {
        layoutRoot = findViewById(R.id.ll_root)
        textNickName = findViewById(R.id.tv_name)
        imageAvatar = findViewById(R.id.iv_avatar)
        textUnfollow = findViewById(R.id.tv_unfollow)
        imageFollowIcon = findViewById(R.id.iv_follow)
        layoutFollowPanel = findViewById(R.id.fl_follow_panel)
    }

    private fun initHostNameView() {
        val ownerName = roomInfoState.ownerName.value
        val ownerId = roomInfoState.ownerId.value
        textNickName.text = if (!TextUtils.isEmpty(ownerName)) ownerName else ownerId
    }

    private fun initHostAvatarView() {
        ImageLoader.load(
            context,
            imageAvatar,
            roomInfoState.ownerAvatarUrl.value,
            R.drawable.room_info_default_avatar
        )
    }

    private fun initRoomInfoPanelView() {
        layoutRoot.setOnClickListener {
            if (roomInfoPopupDialog == null) {
                roomInfoPopupDialog = RoomInfoPopupDialog(context, roomInfoService)
            }
            roomInfoPopupDialog?.show()
        }
    }

    private fun onHostIdChange(ownerId: String?) {
        if (!roomInfoState.enableFollow) {
            return
        }
        
        if (!TextUtils.isEmpty(ownerId) && !TextUtils.equals(roomInfoState.selfUserId, ownerId)) {
            layoutFollowPanel.visibility = View.VISIBLE
            ownerId?.let { roomInfoService.checkFollowUser(it) }
            refreshFollowButton()
            layoutFollowPanel.setOnClickListener { onFollowButtonClick() }
        }
    }

    private fun onHostNickNameChange(name: String?) {
        initHostNameView()
    }

    private fun onHostAvatarChange(avatar: String?) {
        ImageLoader.load(context, imageAvatar, avatar, R.drawable.room_info_default_avatar)
    }

    private fun onFollowStatusChange(followUsers: Set<String>?) {
        refreshFollowButton()
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

    private fun refreshFollowButton() {
        val followingList = roomInfoState.followingList.value ?: emptySet()
        val ownerId = roomInfoState.ownerId.value

        if (!followingList.contains(ownerId)) {
            imageFollowIcon.visibility = GONE
            textUnfollow.visibility = View.VISIBLE
        } else {
            textUnfollow.visibility = View.GONE
            imageFollowIcon.visibility = VISIBLE
        }
    }

    private fun reportData(roomId: String?) {
        val isVoiceRoom = !TextUtils.isEmpty(roomId) && roomId?.startsWith("voice_") == true
        if (isVoiceRoom) {
            reportEventData(LIVEKIT_METRICS_PANEL_SHOW_VOICE_ROOM_LIVE_INFO)
        } else {
            reportEventData(LIVEKIT_METRICS_PANEL_SHOW_LIVE_ROOM_LIVE_INFO)
        }
    }
}