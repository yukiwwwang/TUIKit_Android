package com.trtc.uikit.livekit.features.audiencecontainer.store.observer

import com.trtc.uikit.livekit.features.audiencecontainer.store.AudienceStore
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

class AudienceViewListenerList {
    private val listeners: MutableList<WeakReference<AudienceStore.AudienceViewListener>> =
        CopyOnWriteArrayList()

    fun addListener(listener: AudienceStore.AudienceViewListener) {
        listeners.add(WeakReference(listener))
    }

    fun removeListener(listener: AudienceStore.AudienceViewListener) {
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
        val observersToRemove = ArrayList<WeakReference<AudienceStore.AudienceViewListener>>()
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
        fun onNotify(listener: AudienceStore.AudienceViewListener)
    }
}
