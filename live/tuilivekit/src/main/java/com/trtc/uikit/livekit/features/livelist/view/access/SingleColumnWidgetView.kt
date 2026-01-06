package com.trtc.uikit.livekit.features.livelist.view.access

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicx.widget.basicwidget.avatar.AtomicAvatar
import io.trtc.tuikit.atomicx.widget.basicwidget.avatar.AtomicAvatar.AvatarContent
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo

class SingleColumnWidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val imageAvatar: AtomicAvatar
    private val textRoomName: TextView
    private val textAnchorName: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.livelist_single_column_widget_item, this, true)
        textRoomName = findViewById(R.id.tv_room_name)
        textAnchorName = findViewById(R.id.tv_anchor_name)
        imageAvatar = findViewById(R.id.iv_avatar)
    }

    fun init(liveInfo: LiveInfo) {
        updateLiveInfoView(liveInfo)
    }

    fun updateLiveInfoView(liveInfo: LiveInfo) {
        imageAvatar.apply {
            setContent(
                AvatarContent.URL(
                    url = liveInfo.liveOwner.avatarURL,
                    placeImage = R.drawable.livekit_ic_avatar
                )
            )
        }
        textRoomName.text = if (TextUtils.isEmpty(liveInfo.liveName)) liveInfo.liveID else liveInfo.liveName
        textAnchorName.text =
            if (TextUtils.isEmpty(liveInfo.liveOwner.userName)) liveInfo.liveOwner.userID else liveInfo.liveOwner.userName
    }
}
