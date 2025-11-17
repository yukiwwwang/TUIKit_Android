package com.trtc.uikit.livekit.features.audiencecontainer.view.userinfo

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.imsdk.v2.V2TIMFollowInfo
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager
import io.trtc.tuikit.atomicxcore.api.live.LiveUserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
class UserInfoDialog(
    private val context: Context,
    private val audienceManager: AudienceManager
) : PopupDialog(context) {

    private lateinit var buttonFollow: Button
    private lateinit var textUserName: TextView
    private lateinit var textUserId: TextView
    private lateinit var imageAvatar: ImageView
    private lateinit var textFans: TextView
    private var userInfo: LiveUserInfo? = null
    private var subscribeStateJob: Job? = null

    init {
        initView()
    }

    fun init(userInfo: LiveUserInfo) {
        this.userInfo = userInfo
        audienceManager.getIMStore().checkFollowUser(userInfo.userID)
        updateView()
    }

    fun init(userInfo: TUIRoomDefine.UserInfo?) {
        if (userInfo == null) {
            return
        }
        if (this.userInfo == null) {
            this.userInfo = LiveUserInfo()
        }
        this.userInfo?.userID = userInfo.userId
        this.userInfo?.userName = userInfo.userName
        this.userInfo?.avatarURL = userInfo.avatarUrl
        audienceManager.getIMStore().checkFollowUser(userInfo.userId)
        updateView()
    }

    private fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            audienceManager.getIMState().followingUserList.collect {
                onFollowingUserChanged()
            }
        }
    }

    private fun removeObserver() {
        subscribeStateJob?.cancel()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.livekit_user_info, null)
        bindViewId(view)
        updateView()
        setView(view)
    }

    private fun bindViewId(view: View) {
        buttonFollow = view.findViewById(R.id.btn_follow)
        textUserName = view.findViewById(R.id.tv_anchor_name)
        textUserId = view.findViewById(R.id.tv_user_id)
        imageAvatar = view.findViewById(R.id.iv_avatar)
        textFans = view.findViewById(R.id.tv_fans)
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

    @SuppressLint("SetTextI18n")
    private fun updateView() {
        val userInfo = this.userInfo ?: return
        if (TextUtils.isEmpty(userInfo.userID)) {
            return
        }
        textUserName.text =
            if (TextUtils.isEmpty(userInfo.userName)) userInfo.userID else userInfo.userName
        textUserId.text = "UserId:" + userInfo.userID
        val avatarUrl = userInfo.avatarURL
        if (TextUtils.isEmpty(avatarUrl)) {
            imageAvatar.setImageResource(R.drawable.livekit_ic_avatar)
        } else {
            ImageLoader.load(context, imageAvatar, avatarUrl, R.drawable.livekit_ic_avatar)
        }

        refreshFollowButton()
        buttonFollow.setOnClickListener { onFollowButtonClick() }
    }

    private fun getFansNumber() {
        val userInfo = this.userInfo ?: return
        if (TextUtils.isEmpty(userInfo.userID)) {
            return
        }
        val userIDList = ArrayList<String>()
        userIDList.add(userInfo.userID)
        V2TIMManager.getFriendshipManager().getUserFollowInfo(
            userIDList,
            object : V2TIMValueCallback<List<V2TIMFollowInfo>> {
                override fun onSuccess(v2TIMFollowInfos: List<V2TIMFollowInfo>?) {
                    if (v2TIMFollowInfos != null && v2TIMFollowInfos.isNotEmpty()) {
                        textFans.text = v2TIMFollowInfos[0].followersCount.toString()
                    }
                }

                override fun onError(code: Int, desc: String) {
                    LOGGER.error("UserInfoDialog getUserFollowInfo failed:errorCode:message:$desc")
                    ToastUtil.toastShortMessage("$code,$desc")
                }
            })
    }

    private fun refreshFollowButton() {
        val userInfo = this.userInfo ?: return
        if (audienceManager.getIMState().followingUserList.value.contains(userInfo.userID) == true) {
            buttonFollow.setText(R.string.common_unfollow_anchor)
            buttonFollow.setBackgroundResource(R.drawable.livekit_user_info_detail_button_unfollow)
        } else {
            buttonFollow.setText(R.string.common_follow_anchor)
            buttonFollow.setBackgroundResource(R.drawable.livekit_user_info_button_follow)
        }
        getFansNumber()
    }

    private fun onFollowingUserChanged() {
        val userInfo = this.userInfo ?: return
        if (TextUtils.isEmpty(userInfo.userID)) {
            return
        }
        refreshFollowButton()
    }

    private fun onFollowButtonClick() {
        val userInfo = this.userInfo ?: return
        if (audienceManager.getIMState().followingUserList.value.contains(userInfo.userID) == true) {
            audienceManager.getIMStore().unfollowUser(userInfo.userID)
        } else {
            audienceManager.getIMStore().followUser(userInfo.userID)
        }
    }

    companion object {
        private val LOGGER = LiveKitLogger.getLiveStreamLogger("UserInfoDialog")
    }
}
