package com.trtc.uikit.livekit.component.pippanel

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.core.net.toUri
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicx.widget.basicwidget.popover.AtomicPopover
import io.trtc.tuikit.atomicxcore.api.live.LiveEndedReason
import io.trtc.tuikit.atomicxcore.api.live.LiveListListener
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore

class PIPTogglePanel(
    context: Context,
    val liveID: String
) : AtomicPopover(context) {

    private val liveListListener = object : LiveListListener() {
        override fun onLiveEnded(liveID: String, reason: LiveEndedReason, message: String) {
            dismiss()
        }
    }

    init {
        initView()
    }

    override fun onStart() {
        super.onStart()
        LiveListStore.shared().addLiveListListener(liveListListener)
    }

    override fun onStop() {
        LiveListStore.shared().removeLiveListListener(liveListListener)
        super.onStop()
    }

    private fun initView() {
        val view = View.inflate(context, R.layout.livekit_layout_pip_toggle_panel, null)
        val switchPIPToggle = view.findViewById<SwitchCompat>(R.id.sc_enable_pip)
        val hasPictureInPicturePermission = hasPictureInPicturePermission()
        switchPIPToggle?.isChecked = (PIPPanelStore.sharedInstance().state.enablePictureInPictureToggle && hasPictureInPicturePermission)
        switchPIPToggle?.setOnCheckedChangeListener { button, enable ->
            PIPPanelStore.sharedInstance().state.enablePictureInPictureToggle = enable
            if (enable && !hasPictureInPicturePermission()) {
                val intent = Intent(
                    "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                    "package:${context.packageName}".toUri()
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                dismiss()
            }
        }

        setContent(view)
    }

    fun hasPictureInPicturePermission(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppOpsManager.MODE_ALLOWED == appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                context.applicationInfo.uid,
                context.packageName
            )
        } else {
            return false
        }
    }
}