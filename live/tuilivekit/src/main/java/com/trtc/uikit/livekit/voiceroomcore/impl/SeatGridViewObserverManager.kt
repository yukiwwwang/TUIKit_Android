package com.trtc.uikit.livekit.voiceroomcore.impl

import android.view.View
import com.google.gson.Gson
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.voiceroomcore.SeatGridViewObserver
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

class SeatGridViewObserverManager {

    companion object {
        private val LOGGER =
            LiveKitLogger.Companion.getComponentLogger("SeatGridViewObserverManager")
    }

    private val observers = CopyOnWriteArrayList<WeakReference<SeatGridViewObserver>>()

    fun addObserver(observer: SeatGridViewObserver) {
        if (observers.any { it.get() == observer }) return
        observers.add(WeakReference(observer))
    }

    fun removeObserver(observer: SeatGridViewObserver?) {
        observers.removeAll { ref -> ref.get() == null || ref.get() == observer }
    }

    fun onSeatViewClicked(seatView: View, seatInfo: TUIRoomDefine.SeatInfo) {
        LOGGER.info("Observer onSeatViewClicked view:$seatView seatInfo:${Gson().toJson(seatInfo)}")
        notifyObservers { it.onSeatViewClicked(seatView, seatInfo) }
    }

    private fun notifyObservers(action: (SeatGridViewObserver) -> Unit) {
        removeObserver(null)
        observers.forEach { ref ->
            ref.get()?.let { observer ->
                action(observer)
            }
        }
    }
}