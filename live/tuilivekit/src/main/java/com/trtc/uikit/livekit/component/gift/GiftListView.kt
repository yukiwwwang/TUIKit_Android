package com.trtc.uikit.livekit.component.gift

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.LiveKitLogger.Companion.getComponentLogger
import com.trtc.uikit.livekit.common.reportEventData
import com.trtc.uikit.livekit.component.gift.service.GiftConstants
import com.trtc.uikit.livekit.component.gift.service.GiftConstants.LANGUAGE_EN
import com.trtc.uikit.livekit.component.gift.service.GiftConstants.LANGUAGE_ZH_HANS
import com.trtc.uikit.livekit.component.gift.service.GiftConstants.LANGUAGE_ZH_HANT
import com.trtc.uikit.livekit.component.gift.view.GiftCategoryViewPagerManager
import com.trtc.uikit.livekit.component.gift.view.GiftTabLayoutManager
import io.trtc.tuikit.atomicxcore.api.gift.Gift
import io.trtc.tuikit.atomicxcore.api.gift.GiftCategory
import io.trtc.tuikit.atomicxcore.api.gift.GiftStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class GiftListView : LinearLayout, LifecycleOwner {
    private val logger: LiveKitLogger = getComponentLogger("GiftListPanel")
    private var giftTabLayoutManager: GiftTabLayoutManager? = null
    private var roomId: String = ""
    private var onSendGiftListener: OnSendGiftListener? = null
    private var giftStore: GiftStore? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val collectJobs = mutableListOf<Job>()
    private var isObserverAdded = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        orientation = VERTICAL
    }

    fun init(roomId: String) {
        if (roomId.isEmpty()) {
            return
        }
        this.roomId = roomId
        initStore()
        setupLifecycleIfNeeded()
    }

    private fun getLanguage(): String {
        val language = Locale.getDefault().getLanguage()
        var languageTag = Locale.getDefault().toLanguageTag()
        logger.info("getLanguage language:$language, languageTag:$languageTag")
        if (TextUtils.isEmpty(language) || TextUtils.isEmpty(languageTag)) {
            return LANGUAGE_EN
        }
        languageTag = languageTag.lowercase(Locale.getDefault())
        if ("zh".equals(language, ignoreCase = true)) {
            if (languageTag.contains("zh-hans")
                || languageTag == "zh"
                || languageTag == "zh-cn"
                || languageTag == "zh-sg"
                || languageTag == "zh-my"
            ) {
                return LANGUAGE_ZH_HANS
            } else {
                return LANGUAGE_ZH_HANT
            }
        } else {
            return LANGUAGE_EN
        }
    }

    private fun initStore() {
        giftStore = GiftStore.create(roomId)
        giftStore?.setLanguage(getLanguage())
        giftStore?.refreshUsableGifts(null)
    }

    private fun addObserver() {
        launch {
            giftStore?.giftState?.usableGifts?.collect {
                onGiftListChange(it)
            }
        }
    }

    private fun removeObserver() {}

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupLifecycleIfNeeded()
    }

    override fun onDetachedFromWindow() {
        collectJobs.forEach { it.cancel() }
        collectJobs.clear()
        removeObserver()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        isObserverAdded = false
        super.onDetachedFromWindow()
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private fun setGiftCategoryList(categoryList: List<GiftCategory>) {
        if (categoryList.isEmpty()) {
            logger.error("setGiftCategoryList categoryList is empty")
            return
        }

        removeAllViews()

        if (giftTabLayoutManager == null) {
            giftTabLayoutManager = GiftTabLayoutManager(context)
            giftTabLayoutManager?.setGiftClickListener(object :
                GiftCategoryViewPagerManager.GiftClickListener {
                override fun onClick(position: Int, gift: Gift) {
                    onSendGiftListener?.onSendGift(this@GiftListView, gift, 1)
                }
            })
        }

        val tabLayout = giftTabLayoutManager!!.createLayout(categoryList, COLUMNS, ROWS)
        addView(tabLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun setupLifecycleIfNeeded() {
        if (roomId.isEmpty()) {
            return
        }
        if (!isObserverAdded) {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
            addObserver()
            isObserverAdded = true
        }
    }

    fun sendGift(gift: Gift, giftCount: Int) {
        giftStore?.sendGift(gift.giftID, giftCount, null)
        if (!TextUtils.isEmpty(gift.resourceURL)) {
            val isSvgGift = gift.resourceURL.lowercase(Locale.getDefault()).endsWith(".svga")
            val key = getReportKey(isSvgGift)
            reportEventData(key)
        }
    }

    fun setListener(listener: OnSendGiftListener?) {
        onSendGiftListener = listener
    }

    interface OnSendGiftListener {
        fun onSendGift(view: GiftListView, gift: Gift, giftCount: Int)
    }

    private fun onGiftListChange(list: List<GiftCategory>) {
        if (!TextUtils.isEmpty(roomId)) {
            setGiftCategoryList(list)
        }
    }

    private fun getReportKey(isSvgGift: Boolean): Int {
        val isVoiceRoom = !TextUtils.isEmpty(roomId) && roomId!!.startsWith("voice_")
        val key: Int
        if (isVoiceRoom) {
            key = if (isSvgGift)
                GiftConstants.DATA_REPORT_VOICE_GIFT_SVGA_SEND_COUNT
            else
                GiftConstants.DATA_REPORT_VOICE_GIFT_EFFECT_SEND_COUNT
        } else {
            key = if (isSvgGift)
                GiftConstants.DATA_REPORT_LIVE_GIFT_SVGA_SEND_COUNT
            else
                GiftConstants.DATA_REPORT_LIVE_GIFT_EFFECT_SEND_COUNT
        }
        return key
    }


    private fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return lifecycleScope.launch(block = block).also { job ->
            collectJobs.add(job)
        }
    }

    companion object {
        private const val COLUMNS = 4
        private const val ROWS = 2
    }
}