package com.trtc.uikit.livekit.features.audiencecontainer.manager.observer

import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

class AudienceViewListenerList {
    private val listeners: MutableList<WeakReference<AudienceManager.AudienceViewListener>> =
        CopyOnWriteArrayList()

    fun addListener(listener: AudienceManager.AudienceViewListener) {
        listeners.add(WeakReference(listener))
    }

    fun removeListener(listener: AudienceManager.AudienceViewListener) {
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
        val observersToRemove = ArrayList<WeakReference<AudienceManager.AudienceViewListener>>()
        for (ref in listeners) {
            val listener = ref.get()
            if (listener == null) {
                observersToRemove.add(ref)
            } else {
                callback.onNotify(listener)
            }
        }
        listeners.removeAll(observersToRemove)
    }

    fun interface ListenerCallback {
        fun onNotify(listener: AudienceManager.AudienceViewListener)
    }
}
