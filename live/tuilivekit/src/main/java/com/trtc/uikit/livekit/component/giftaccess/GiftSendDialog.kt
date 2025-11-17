package com.trtc.uikit.livekit.component.giftaccess

import android.content.Context
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.component.gift.GiftListView
import io.trtc.tuikit.atomicxcore.api.gift.Gift

class GiftSendDialog(
    context: Context,
    val roomId: String,
    val ownerId: String,
    val ownerName: String,
    val ownerAvatarUrl: String
) : BottomSheetDialog(context), GiftListView.OnSendGiftListener {

    private val TAG = "GiftSendDialog"
    init {
        setContentView(R.layout.gift_layout_send_dialog_panel)
        init()
    }

    private fun init() {
        val mGiftListView = findViewById<GiftListView>(R.id.gift_list_view)
        mGiftListView?.let { giftListView ->
            giftListView.init(roomId)
            giftListView.setListener(this)
        }
        setCanceledOnTouchOutside(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundResource(com.trtc.tuikit.common.R.color.common_design_bottom_sheet_color)
    }

    override fun onSendGift(view: GiftListView, gift: Gift, count: Int) {
        view.sendGift(gift, count)
    }
}