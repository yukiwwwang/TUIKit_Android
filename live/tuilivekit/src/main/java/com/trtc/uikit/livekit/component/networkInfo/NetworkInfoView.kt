package com.trtc.uikit.livekit.component.networkInfo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.component.networkInfo.service.NetworkInfoService
import com.trtc.uikit.livekit.component.networkInfo.store.NetworkInfoState
import com.trtc.uikit.livekit.component.networkInfo.view.NetworkBadTipsDialog
import com.trtc.uikit.livekit.component.networkInfo.view.NetworkInfoPanel
import kotlin.math.min

class NetworkInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val service: NetworkInfoService = NetworkInfoService(context)
    private val state: NetworkInfoState = service.networkInfoState
    private lateinit var imageNetworkStatus: ImageView
    private lateinit var textCreateTime: TextView
    private var networkInfoPanel: NetworkInfoPanel? = null
    private lateinit var layoutNetworkInfo: LinearLayout
    private var createTime: Long = 0L
    private var liveTimeRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    private val netWorkQualityObserver = Observer<TUICommonDefine.NetworkQuality> { networkQuality ->
        onNetworkQualityChange(networkQuality)
    }

    private val networkWeakTipsObserver = Observer<Boolean> { isShow ->
        onNetworkWeakTipsChange(isShow)
    }

    private val roomDismissedObserver = Observer<Boolean> { dismissed ->
        onRoomRoomDismissed(dismissed)
    }

    init {
        initView()
    }

    fun init(createTime: Long) {
        val now = System.currentTimeMillis()
        this@NetworkInfoView.createTime = if (createTime <= 0) {
            now
        } else {
            min(createTime, now)
        }
        startLiveTimer()
    }

    fun setScreenOrientation(isPortrait: Boolean) {
        layoutNetworkInfo.isEnabled = isPortrait
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.network_info_view, this, true)
        bindViewId()
        initNetworkView()
    }

    private fun bindViewId() {
        layoutNetworkInfo = findViewById(R.id.ll_network_info)
        imageNetworkStatus = findViewById(R.id.iv_network_status)
        textCreateTime = findViewById(R.id.tv_live_time)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
        stopLiveTimer()
    }

    private fun addObserver() {
        service.addObserver()
        state.networkStatus.observeForever(netWorkQualityObserver)
        state.isDisplayNetworkWeakTips.observeForever(networkWeakTipsObserver)
        state.roomDismissed.observeForever(roomDismissedObserver)
    }

    private fun removeObserver() {
        service.removeObserver()
        state.networkStatus.removeObserver(netWorkQualityObserver)
        state.isDisplayNetworkWeakTips.removeObserver(networkWeakTipsObserver)
        state.roomDismissed.removeObserver(roomDismissedObserver)
    }

    private fun initNetworkView() {
        layoutNetworkInfo.setOnClickListener {
            networkInfoPanel = NetworkInfoPanel(
                context,
                service,
                state.isTakeInSeat.value == true
            )
            networkInfoPanel?.show()
        }
    }

    private fun startLiveTimer() {
        liveTimeRunnable?.let { handler.removeCallbacks(it) }
        
        liveTimeRunnable = object : Runnable {
            override fun run() {
                val now = System.currentTimeMillis()
                val duration = now - createTime
                textCreateTime.text = formatDuration(duration)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(liveTimeRunnable!!)
    }

    private fun stopLiveTimer() {
        liveTimeRunnable?.let {
            handler.removeCallbacks(it)
            liveTimeRunnable = null
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(durationMillis: Long): String {
        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun onNetworkQualityChange(networkQuality: TUICommonDefine.NetworkQuality) {
        val resId = when (networkQuality) {
            TUICommonDefine.NetworkQuality.POOR -> R.drawable.network_info_network_status_poor
            TUICommonDefine.NetworkQuality.BAD -> R.drawable.network_info_network_status_very_bad
            TUICommonDefine.NetworkQuality.VERY_BAD,
            TUICommonDefine.NetworkQuality.DOWN -> R.drawable.network_info_network_status_down
            else -> R.drawable.network_info_network_status_good
        }
        imageNetworkStatus.setImageResource(resId)
    }

    private fun onNetworkWeakTipsChange(isShow: Boolean?) {
        if (isShow == true) {
            val dialog = NetworkBadTipsDialog(context)
            dialog.show()
        }
    }

    private fun onRoomRoomDismissed(dismissed: Boolean?) {
        if (dismissed == true && networkInfoPanel?.isShowing == true) {
            networkInfoPanel?.dismiss()
        }
    }
}