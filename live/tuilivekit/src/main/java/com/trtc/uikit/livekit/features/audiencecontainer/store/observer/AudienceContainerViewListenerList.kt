package com.trtc.uikit.livekit.features.audiencecontainer.store.observer

import com.trtc.uikit.livekit.features.audiencecontainer.AudienceContainerViewDefine
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

class AudienceContainerViewListenerList {
    private val listeners: MutableList<WeakReference<AudienceContainerViewDefine.AudienceContainerViewListener>> =
        CopyOnWriteArrayList()

    fun addListener(listener: AudienceContainerViewDefine.AudienceContainerViewListener) {
        listeners.add(WeakReference(listener))
    }

    fun removeListener(listener: AudienceContainerViewDefine.AudienceContainerViewListener) {
        for (ref in listeners) {
            if (ref.get() == listener) {
                listeners.remove(ref)
            }
        }
    }

    fun clearListeners() {
        listeners.clear()
    }

    fun notifyListeners(callback: ListenerCallback) {
        val listenersToRemove = ArrayList<WeakReference<AudienceContainerViewDefine.AudienceContainerViewListener>>()
        for (ref in listeners) {
            val listener = ref.get()
            if (listener == null) {
                listenersToRemove.add(ref)
            } else {
                callback.onNotify(listener)
            }
        }
        listeners.removeAll(listenersToRemove)
    }

    fun interface ListenerCallback {
        fun onNotify(listener: AudienceContainerViewDefine.AudienceContainerViewListener)
    }
}
