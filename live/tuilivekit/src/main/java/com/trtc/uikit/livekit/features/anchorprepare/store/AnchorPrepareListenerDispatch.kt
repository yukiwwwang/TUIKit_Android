package com.trtc.uikit.livekit.features.anchorprepare.store

import com.trtc.uikit.livekit.features.anchorprepare.AnchorPrepareViewListener
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

class AnchorPrepareListenerDispatch {
    private val listenerList = CopyOnWriteArrayList<WeakReference<AnchorPrepareViewListener>>()

    fun addAnchorPrepareViewListener(listener: AnchorPrepareViewListener) {
        listenerList.add(WeakReference(listener))
    }

    fun removeAnchorPrepareViewListener(listener: AnchorPrepareViewListener) {
        val toRemove = mutableListOf<WeakReference<AnchorPrepareViewListener>>()
        for (ref in listenerList) {
            if (ref.get() == listener) {
                toRemove.add(ref)
            }
        }
        listenerList.removeAll(toRemove)
    }

    fun clearAnchorPrepareViewListeners() {
        listenerList.clear()
    }

    fun notifyAnchorPrepareViewListener(callback: AnchorPrepareViewCallback) {
        val observersToRemove = mutableListOf<WeakReference<AnchorPrepareViewListener>>()
        for (ref in listenerList) {
            val observer = ref.get()
            if (observer == null) {
                observersToRemove.add(ref)
            } else {
                callback.onNotify(observer)
            }
        }
        listenerList.removeAll(observersToRemove)
    }

    fun interface AnchorPrepareViewCallback {
        fun onNotify(observer: AnchorPrepareViewListener)
    }
}