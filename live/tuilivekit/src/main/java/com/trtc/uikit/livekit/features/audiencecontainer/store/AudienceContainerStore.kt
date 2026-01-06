package com.trtc.uikit.livekit.features.audiencecontainer.store

import com.trtc.uikit.livekit.features.audiencecontainer.AudienceContainerViewDefine
import com.trtc.uikit.livekit.features.audiencecontainer.store.observer.AudienceContainerViewListenerList

class AudienceContainerStore {
    private val viewListenerList: AudienceContainerViewListenerList =
        AudienceContainerViewListenerList()

    fun addListener(listener: AudienceContainerViewDefine.AudienceContainerViewListener) {
        viewListenerList.addListener(listener)
    }

    fun removeListener(listener: AudienceContainerViewDefine.AudienceContainerViewListener) {
        viewListenerList.removeListener(listener)
    }

    fun getAudienceContainerViewListenerList(): AudienceContainerViewListenerList {
        return viewListenerList
    }

    companion object {
        @JvmStatic
        fun disableSliding(disable: Boolean) {
            AudienceContainerConfig.disableSliding.value = disable
        }

        @JvmStatic
        fun disableHeaderFloatWin(disable: Boolean) {
            AudienceContainerConfig.disableHeaderFloatWin.value = disable
        }

        @JvmStatic
        fun disableHeaderLiveData(disable: Boolean) {
            AudienceContainerConfig.disableHeaderLiveData.value=disable
        }

        @JvmStatic
        fun disableHeaderVisitorCnt(disable: Boolean) {
            AudienceContainerConfig.disableHeaderVisitorCnt.value = disable
        }

        @JvmStatic
        fun disableFooterCoGuest(disable: Boolean) {
            AudienceContainerConfig.disableFooterCoGuest.value = disable
        }
    }
}