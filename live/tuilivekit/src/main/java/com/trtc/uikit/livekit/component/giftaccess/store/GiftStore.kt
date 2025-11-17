package com.trtc.uikit.livekit.component.giftaccess.store

import com.trtc.uikit.livekit.component.giftaccess.service.GiftCacheService

class GiftStore {
    val giftCacheService = GiftCacheService()

    companion object {
        @Volatile
        private var sInstance: GiftStore? = null

        @JvmStatic
        fun getInstance(): GiftStore {
            return sInstance ?: synchronized(this) {
                sInstance ?: GiftStore().also { sInstance = it }
            }
        }
    }
}