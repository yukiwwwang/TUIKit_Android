package com.trtc.uikit.livekit.voiceroom.view.dashboard

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.voiceroom.manager.VoiceRoomManager
import com.trtc.uikit.livekit.voiceroom.view.basic.BasicView
import io.trtc.tuikit.atomicx.widget.basicwidget.avatar.AtomicAvatar
import io.trtc.tuikit.atomicx.widget.basicwidget.avatar.AtomicAvatar.AvatarContent

class AudienceDashboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BasicView(context, attrs, defStyleAttr) {

    private var textName: TextView
    private var imageHead: AtomicAvatar

    init {
        LayoutInflater.from(context).inflate(R.layout.livekit_audience_dashboard_view, this, true)
        textName = findViewById(R.id.tv_name)
        imageHead = findViewById(R.id.iv_head)

        findViewById<View>(R.id.iv_back).setOnClickListener {
            voiceRoomManager?.prepareStore?.destroy()
            (context as? Activity)?.finish()
        }
    }

    override fun init(liveID: String, voiceRoomManager: VoiceRoomManager) {
        super.init(liveID,voiceRoomManager)
        val ownerInfo =
            voiceRoomManager.prepareStore.prepareState.liveInfo.value.liveOwner
        textName.text = ownerInfo.userName.ifEmpty { ownerInfo.userID }

        imageHead.setContent(AvatarContent.URL(ownerInfo.avatarURL, R.drawable.livekit_ic_avatar))
    }

    override fun addObserver() = Unit

    override fun removeObserver() = Unit

    override fun initStore() = Unit
}
