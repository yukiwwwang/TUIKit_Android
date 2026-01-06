package com.trtc.uikit.livekit.features.livelist.view.access

import android.annotation.SuppressLint
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

class DoubleColumnWidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val avatar: AtomicAvatar
    private val textRoomName: TextView
    private val textAnchorName: TextView
    private val textAudienceCountInfo: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.livelist_double_column_widget_item, this, true)
        textRoomName = findViewById(R.id.tv_room_name)
        textAnchorName = findViewById(R.id.tv_anchor_name)
        avatar = findViewById(R.id.iv_avatar)
        textAudienceCountInfo = findViewById(R.id.tv_audience_count_info)
    }

    fun init(liveInfo: LiveInfo) {
        updateLiveInfoView(liveInfo)
    }

    @SuppressLint("StringFormatMatches")
    fun updateLiveInfoView(liveInfo: LiveInfo) {
        avatar.setContent(AvatarContent.URL(liveInfo.liveOwner.avatarURL, R.drawable.livelist_default_avatar))
        textRoomName.text = if (TextUtils.isEmpty(liveInfo.liveName)) liveInfo.liveID else liveInfo.liveName
        textAnchorName.text = if (TextUtils.isEmpty(liveInfo.liveOwner.userName)) liveInfo.liveOwner.userID else liveInfo.liveOwner.userName
        textAudienceCountInfo.text = context.getString(R.string.livelist_viewed_audience_count, liveInfo.totalViewerCount)
    }
}
