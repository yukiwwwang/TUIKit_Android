package com.trtc.uikit.roomkit

import android.os.Bundle
import com.trtc.tuikit.common.FullScreenActivity
import com.trtc.uikit.roomkit.view.RoomJoinView

/**
 * RoomJoinActivity - Room joining screen container activity.
 * Pure container that loads RoomJoinView for entering existing rooms.
 */
class RoomJoinActivity : FullScreenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(RoomJoinView(this))
    }
}