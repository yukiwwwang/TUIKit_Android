package com.tencent.uikit.app.main.call

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.tencent.qcloud.tuikit.tuicallkit.manager.CallManager.Companion.instance
import com.tencent.qcloud.tuikit.tuicallkit.view.CallAdapter
import io.trtc.tuikit.atomicxcore.api.call.CallStore
import io.trtc.tuikit.atomicxcore.api.call.CallParticipantStatus

class CustomUIAdapter {
    val streamViewAdapter: CallAdapter = object : CallAdapter() {
        public override fun onCreateStreamView(view: ViewGroup): ViewGroup {
            val buttonHangup = Button(view.context)
            buttonHangup.text = "Hangup"
            buttonHangup.setBackgroundColor(Color.CYAN)
            buttonHangup.setOnClickListener(View.OnClickListener { v: View? ->
                val selfUser = CallStore.shared.observerState.selfInfo.value
                val callerId = CallStore.shared.observerState.activeCall.value.inviterId
                if (selfUser.id == callerId
                    && selfUser.status == CallParticipantStatus.Waiting
                ) {
                    instance.reject(null)
                } else {
                    instance.hangup(null)
                }
            })
            val lp = ConstraintLayout.LayoutParams(200, 120)
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            buttonHangup.setLayoutParams(lp)
            view.addView(buttonHangup)
            return view
        }
    }

    val mainViewAdapter: CallAdapter = object : CallAdapter() {
        public override fun onCreateMainView(view: ViewGroup): ViewGroup {
            val button = Button(view.context)
            button.setBackgroundColor(Color.LTGRAY)
            val lp = ConstraintLayout.LayoutParams(120, 120)
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            button.setLayoutParams(lp)
            view.addView(button)
            return view
        }
    }
}