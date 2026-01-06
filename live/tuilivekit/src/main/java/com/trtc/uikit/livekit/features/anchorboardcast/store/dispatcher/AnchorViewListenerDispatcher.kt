package com.trtc.uikit.livekit.features.anchorboardcast.store.dispatcher

import com.trtc.uikit.livekit.features.anchorboardcast.AnchorViewListener
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

class AnchorViewListenerDispatcher {
    private val listenerList = CopyOnWriteArrayList<WeakReference<AnchorViewListener>>()

    fun addAnchorViewListener(listener: AnchorViewListener) {
        listenerList.add(WeakReference(listener))
    }

    fun removeAnchorViewListener(listener: AnchorViewListener) {
        val toRemove = mutableListOf<WeakReference<AnchorViewListener>>()
        for (ref in listenerList) {
            if (ref.get() == listener) {
                toRemove.add(ref)
            }
        }
        listenerList.removeAll(toRemove)
    }

    fun clearAnchorViewListeners() {
        listenerList.clear()
    }

    fun notifyAnchorViewListener(callback: (AnchorViewListener) -> Unit) {
        val observersToRemove = mutableListOf<WeakReference<AnchorViewListener>>()
        for (ref in listenerList) {
            val observer = ref.get()
            if (observer == null) {
                observersToRemove.add(ref)
            } else {
                callback(observer)
            }
        }
        listenerList.removeAll(observersToRemove)
    }
}