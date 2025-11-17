package com.trtc.uikit.livekit.component.giftaccess

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.cloud.tuikit.engine.room.TUIRoomObserver
import com.trtc.uikit.livekit.R

@SuppressLint("ViewConstructor")
class GiftButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var roomId: String? = null
    var ownerId: String? = null
    var ownerName: String? = null
    var ownerAvatarUrl: String? = null
    private lateinit var imageButton: ImageView
    private var giftSendDialog: GiftSendDialog? = null

    private val roomObserver = object : TUIRoomObserver() {
        override fun onRoomDismissed(roomId: String, reason: TUIRoomDefine.RoomDismissedReason) {
            giftSendDialog?.dismiss()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.gift_layout_extension_view, this)
    }

    fun init(roomId: String, ownerId: String, ownerName: String, ownerAvatarUrl: String) {
        this.roomId = roomId
        this.ownerId = ownerId
        this.ownerName = ownerName
        this.ownerAvatarUrl = ownerAvatarUrl
        initView()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        TUIRoomEngine.sharedInstance().addObserver(roomObserver)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        TUIRoomEngine.sharedInstance().removeObserver(roomObserver)
    }

    private fun initView() {
        bindViewId()
        initImageButton()
    }

    private fun bindViewId() {
        imageButton = findViewById(R.id.iv_gift)
    }

    private fun initImageButton() {
        imageButton.setOnClickListener {
            if (giftSendDialog == null) {
                giftSendDialog = GiftSendDialog(context, roomId!!, ownerId!!, ownerName!!, ownerAvatarUrl!!)
            }
            giftSendDialog?.show()
        }
    }
}