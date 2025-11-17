package com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.panel

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import com.trtc.uikit.livekit.features.anchorboardcast.view.BasicView
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.view.LiveCoreView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

@SuppressLint("ViewConstructor")
class ApplyCoGuestFloatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BasicView(context, attrs, defStyleAttr) {

    private lateinit var layoutRoot: LinearLayout
    private lateinit var imageFirstApplyLinkAudience: ImageView
    private lateinit var imageSecondApplyLinkAudience: ImageView
    private lateinit var layoutSecondApplyLinkAudience: RelativeLayout
    private lateinit var layoutEllipsis: RelativeLayout
    private lateinit var textApplyLinkAudienceCount: TextView
    private lateinit var liveStream: LiveCoreView
    private var subscribeStateJob: Job? = null

    fun init(manager: AnchorManager, liveStream: LiveCoreView) {
        this.liveStream = liveStream
        super.init(manager)
    }

    override fun initView() {
        LayoutInflater.from(context).inflate(R.layout.livekit_layout_anchor_apply_link_audience, this, true)
        layoutRoot = findViewById(R.id.ll_root)
        textApplyLinkAudienceCount = findViewById(R.id.tv_apply_link_audience_count)
        layoutSecondApplyLinkAudience = findViewById(R.id.rl_second_apply_link_audience)
        layoutEllipsis = findViewById(R.id.rl_ellipsis)
        imageFirstApplyLinkAudience = findViewById(R.id.iv_first_apply_link_audience)
        imageSecondApplyLinkAudience = findViewById(R.id.iv_second_apply_link_audience)
    }

    override fun refreshView() {
        initApplyLinkAudienceListView()
        initRootView()
    }

    private fun initRootView() {
        layoutRoot.setOnClickListener { view ->
            if (!view.isEnabled) {
                return@setOnClickListener
            }
            view.isEnabled = false
            val dialog = AnchorCoGuestManageDialog(baseContext, anchorManager, liveStream)
            dialog.setOnDismissListener { view.isEnabled = true }
            dialog.show()
        }
    }

    override fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            onApplyLinkAudienceListChange()
        }
    }

    override fun removeObserver() {
        subscribeStateJob?.cancel()
    }

    @SuppressLint("StringFormatMatches")
    private fun initApplyLinkAudienceListView() {
        val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
        val seatApplicationList = CoGuestStore.create(currentLiveId).coGuestState.applicants.value
        if (seatApplicationList.isNotEmpty()) {
            visibility = VISIBLE
        } else {
            visibility = GONE
        }

        val applyLinkAudienceList = CopyOnWriteArrayList(seatApplicationList)
        when {
            seatApplicationList.size == 1 -> {
                layoutSecondApplyLinkAudience.visibility = GONE
                layoutEllipsis.visibility = GONE
                ImageLoader.load(
                    baseContext, imageFirstApplyLinkAudience,
                    applyLinkAudienceList[0].avatarURL, R.drawable.livekit_ic_avatar
                )
            }

            seatApplicationList.size == 2 -> {
                layoutSecondApplyLinkAudience.visibility = VISIBLE
                layoutEllipsis.visibility = GONE
                ImageLoader.load(
                    baseContext, imageFirstApplyLinkAudience,
                    applyLinkAudienceList[0].avatarURL, R.drawable.livekit_ic_avatar
                )
                ImageLoader.load(
                    baseContext, imageSecondApplyLinkAudience,
                    applyLinkAudienceList[1].avatarURL, R.drawable.livekit_ic_avatar
                )
            }

            seatApplicationList.size > 2 -> {
                layoutSecondApplyLinkAudience.visibility = VISIBLE
                layoutEllipsis.visibility = VISIBLE
                ImageLoader.load(
                    baseContext, imageFirstApplyLinkAudience,
                    applyLinkAudienceList[0].avatarURL, R.drawable.livekit_ic_avatar
                )
                ImageLoader.load(
                    baseContext, imageSecondApplyLinkAudience,
                    applyLinkAudienceList[1].avatarURL, R.drawable.livekit_ic_avatar
                )
            }

            else -> {
                visibility = GONE
            }
        }
        textApplyLinkAudienceCount.text = baseContext.getString(
            R.string.common_seat_application_title,
            seatApplicationList.size
        )
    }

    private suspend fun onApplyLinkAudienceListChange() {
        val currentLiveId = LiveListStore.shared().liveState.currentLive.value.liveID
        CoGuestStore.create(currentLiveId).coGuestState.applicants.collect {
            initApplyLinkAudienceListView()
        }
    }
}