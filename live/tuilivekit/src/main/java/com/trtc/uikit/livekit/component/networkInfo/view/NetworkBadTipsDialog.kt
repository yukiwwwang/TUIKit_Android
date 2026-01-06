package com.trtc.uikit.livekit.component.networkInfo.view

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.trtc.uikit.livekit.R

class NetworkBadTipsDialog(context: Context) :
    Dialog(context, com.trtc.tuikit.common.R.style.TUICommonBottomDialogTheme) {

    companion object {
        private const val NETWORK_BAD_TIPS_DURATION = 5000L
    }

    private lateinit var mImageClose: ImageView
    private lateinit var mTextSwitchNetwork: TextView

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.network_info_network_bad_tips_view, null)
        bindViewId(view)
        initCloseView()
        initSwitchNetworkView()
        this@NetworkBadTipsDialog.setContentView(view)
    }

    private fun bindViewId(view: View) {
        mTextSwitchNetwork = view.findViewById(R.id.tv_switch_network)
        mImageClose = view.findViewById(R.id.iv_close)
    }

    private fun initCloseView() {
        mImageClose.setOnClickListener { dismiss() }
    }

    private fun initSwitchNetworkView() {
        mTextSwitchNetwork.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            dismiss()
        }
    }

    override fun show() {
        super.show()
        setAutoDismiss()
    }

    private fun setAutoDismiss() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isShowing) {
                dismiss()
            }
        }, NETWORK_BAD_TIPS_DURATION)
    }
}