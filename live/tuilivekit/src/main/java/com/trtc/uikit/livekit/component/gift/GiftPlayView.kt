package com.trtc.uikit.livekit.component.gift

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.LiveKitLogger.Companion.getComponentLogger
import com.trtc.uikit.livekit.common.ui.BasicView
import com.trtc.uikit.livekit.component.gift.service.GiftConstants.LANGUAGE_EN
import com.trtc.uikit.livekit.component.gift.service.GiftConstants.LANGUAGE_ZH_HANS
import com.trtc.uikit.livekit.component.gift.service.GiftConstants.LANGUAGE_ZH_HANT
import com.trtc.uikit.livekit.component.gift.view.animation.AnimationView
import com.trtc.uikit.livekit.component.gift.view.animation.ImageAnimationView
import com.trtc.uikit.livekit.component.gift.view.animation.ImageAnimationView.GiftImageAnimationInfo
import com.trtc.uikit.livekit.component.gift.view.animation.manager.AnimationPlayer
import com.trtc.uikit.livekit.component.gift.view.animation.manager.GiftAnimationManager
import com.trtc.uikit.livekit.component.gift.view.like.GiftHeartLayout
import com.trtc.uikit.livekit.component.gift.viewmodel.GiftModel
import io.trtc.tuikit.atomicxcore.api.gift.Gift
import io.trtc.tuikit.atomicxcore.api.gift.GiftListener
import io.trtc.tuikit.atomicxcore.api.gift.GiftStore
import io.trtc.tuikit.atomicxcore.api.live.LikeListener
import io.trtc.tuikit.atomicxcore.api.live.LikeStore
import io.trtc.tuikit.atomicxcore.api.live.LiveUserInfo
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class GiftPlayView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BasicView(
    mContext, attrs, defStyleAttr
) {
    private val logger: LiveKitLogger = getComponentLogger("GiftPlayView")
    private var imageAnimationView: ImageAnimationView? = null
    private var animationView: AnimationView? = null
    private var heartLayout: GiftHeartLayout? = null
    private var giftPlayViewListener: TUIGiftPlayViewListener? = null
    private var giftStore: GiftStore? = null
    private var likeStore: LikeStore? = null
    private val giftAnimationManager = GiftAnimationManager()
    private val giftImageAnimationManager = GiftAnimationManager()
    private val likeListenerImpl = LikeListenerImpl()
    private val giftListenerImpl = GiftListenerImpl()

    init {
        LayoutInflater.from(context).inflate(R.layout.gift_layout_animator, this, true)
        initView()
        initAnimationPlayer()
        initImageAnimationPlayer()
    }

    override fun init(roomId: String) {
        super.init(roomId)
        giftStore?.setLanguage(getLanguage())
        animationView?.roomId = roomId
    }

    override fun initStore() {
        giftStore = GiftStore.create(roomId)
        likeStore = LikeStore.create(roomId)
    }

    override fun addObserver() {
        giftStore?.addGiftListener(giftListenerImpl)
        likeStore?.addLikeListener(likeListenerImpl)
    }

    override fun removeObserver() {
        giftStore?.removeGiftListener(giftListenerImpl)
        likeStore?.removeLikeListener(likeListenerImpl)
    }

    private fun initView() {
        imageAnimationView = findViewById(R.id.gift_image_anim_view)
        animationView = findViewById(R.id.gift_anim_view)
        heartLayout = findViewById(R.id.heart_layout)
    }

    private fun initAnimationPlayer() {
        val animationPlayer: AnimationPlayer = object : AnimationPlayer() {
            override fun preparePlay(model: GiftModel) {
                if (isAttachedToWindow) {
                    model.gift?.let {
                        giftPlayViewListener?.onPlayGiftAnimation(this@GiftPlayView, model.gift!!)
                    }
                }
            }

            override fun startPlay(model: GiftModel) {
                if (isAttachedToWindow) {
                    model.gift?.let {
                        animationView?.playAnimation(model.gift!!.resourceURL)
                    }
                }
            }

            override fun stopPlay() {
                animationView?.stopPlay()
            }

            override fun setCallback(callback: PlayCallback?) {
                animationView?.callback = object : AnimationView.Callback {
                    override fun onFinished(error: Int) {
                        callback?.onFinished(error)
                    }
                }
            }
        }
        giftAnimationManager.setPlayer(animationPlayer)
    }

    private fun initImageAnimationPlayer() {
        val imageAnimationPlayer: AnimationPlayer = object : AnimationPlayer() {
            override fun preparePlay(model: GiftModel) {
                if (isAttachedToWindow) {
                    giftImageAnimationManager.startPlay(model)
                }
            }

            override fun startPlay(model: GiftModel) {
                if (isAttachedToWindow) {
                    model.gift?.let {
                        val info = GiftImageAnimationInfo()
                        info.giftImageUrl = model.gift!!.iconURL
                        info.giftName = model.gift!!.name
                        info.giftCount = model.giftCount
                        info.senderName = model.sender!!.userName
                        info.senderAvatarUrl = model.sender!!.avatarURL
                        imageAnimationView?.playAnimation(info)
                    }
                }
            }

            override fun stopPlay() {
                imageAnimationView?.stopPlay()
            }

            override fun setCallback(callback: PlayCallback?) {
                imageAnimationView?.setCallback(object : ImageAnimationView.Callback {
                    override fun onFinished(error: Int) {
                        callback?.onFinished(error)
                    }
                })
            }
        }
        giftImageAnimationManager.setPlayer(imageAnimationPlayer)
    }

    override fun onDetachedFromWindow() {
        giftAnimationManager.stopPlay()
        giftImageAnimationManager.stopPlay()
        super.onDetachedFromWindow()
    }

    fun playGiftAnimation(playUrl: String) {
        val model = GiftModel()
        model.gift = Gift(resourceURL = playUrl)
        if (Looper.myLooper() == Looper.getMainLooper()) {
            giftAnimationManager.startPlay(model)
        } else {
            post(Runnable { giftAnimationManager.startPlay(model) })
        }
    }

    fun setListener(listener: TUIGiftPlayViewListener?) {
        giftPlayViewListener = listener
    }

    interface TUIGiftPlayViewListener {
        fun onReceiveGift(view: GiftPlayView?, gift: Gift, giftCount: Int, sender: LiveUserInfo)

        fun onPlayGiftAnimation(view: GiftPlayView?, gift: Gift)
    }

    private fun playGift(gift: Gift, giftCount: Int, sender: LiveUserInfo) {
        val giftModel = GiftModel()
        giftModel.gift = gift
        giftModel.giftCount = giftCount
        giftModel.sender = sender
        giftModel.isFromSelf = TextUtils.equals(sender.userID, LoginStore.shared.loginState.loginUserInfo.value?.userID)
        if (TextUtils.isEmpty(gift.resourceURL)) {
            giftImageAnimationManager.add(giftModel)
        } else {
            giftAnimationManager.add(giftModel)
        }
        giftPlayViewListener?.onReceiveGift(this, gift, giftCount, sender)
    }

    private fun playLike() {
        heartLayout?.addFavor()
    }

    private fun playLikeFromSelf() {
        playLike()
    }

    private fun playLikeFromOther(count: Int) {
        var newCount = min(count, LIKE_ANIMATION_COUNT_MAX)
        newCount = max(newCount, 0)
        val likeCount = intArrayOf(newCount)
        val task: Runnable = object : Runnable {
            override fun run() {
                if (likeCount[0] > 0) {
                    playLike()
                    postDelayed(this, LIKE_ANIMATION_INTERVAL_MS.toLong())
                    likeCount[0]--
                }
            }
        }
        post(task)
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

    inner class GiftListenerImpl : GiftListener() {
        override fun onReceiveGift(liveId: String, gift: Gift, count: Int, sender: LiveUserInfo) {
            logger.info("onReceiveGift: liveId:$liveId, gift:$gift, count:$count, sender:$sender")
            if (liveId == roomId) {
                playGift(gift, count, sender)
            }
        }
    }

    inner class LikeListenerImpl : LikeListener() {
        override fun onReceiveLikesMessage(liveId: String, totalLikesReceived: Long, sender: LiveUserInfo) {
            if (liveId == roomId) {
                playLikeFromOther(3)
            }
        }
    }

    companion object {
        private const val TAG = "GiftPlayView"
        private const val LIKE_ANIMATION_INTERVAL_MS = 100
        private const val LIKE_ANIMATION_COUNT_MAX = 30
    }
}