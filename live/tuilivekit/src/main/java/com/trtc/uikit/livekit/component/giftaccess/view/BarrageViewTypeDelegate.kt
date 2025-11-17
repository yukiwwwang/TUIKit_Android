package com.trtc.uikit.livekit.component.giftaccess.view

import com.trtc.uikit.livekit.component.barrage.view.adapter.BarrageItemTypeDelegate
import com.trtc.uikit.livekit.component.giftaccess.service.GiftConstants
import io.trtc.tuikit.atomicxcore.api.barrage.Barrage

class BarrageViewTypeDelegate : BarrageItemTypeDelegate {

    override fun getItemType(position: Int, barrage: Barrage): Int {
        if (barrage.extensionInfo.containsKey(GiftConstants.GIFT_VIEW_TYPE)) {
            val viewTypeString = barrage.extensionInfo[GiftConstants.GIFT_VIEW_TYPE].toString()
            if (GiftConstants.GIFT_VIEW_TYPE_1.toString() == viewTypeString) {
                return GiftConstants.GIFT_VIEW_TYPE_1
            }
        }
        return 0
    }
}