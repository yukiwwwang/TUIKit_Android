package com.trtc.uikit.livekit.features.anchorboardcast

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.gson.Gson
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.extension.TUILiveBattleManager
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine.SeatFullInfo
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine.UserInfo
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.TUIThemeManager
import com.trtc.tuikit.common.foregroundservice.VideoForegroundService
import com.trtc.tuikit.common.permission.PermissionCallback
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.tuikit.common.util.ScreenUtil
import com.trtc.tuikit.common.util.ToastUtil
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ErrorLocalized
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.common.PermissionRequest
import com.trtc.uikit.livekit.common.ui.RoundFrameLayout
import com.trtc.uikit.livekit.component.audiencelist.AudienceListView
import com.trtc.uikit.livekit.component.barrage.BarrageInputView
import com.trtc.uikit.livekit.component.barrage.BarrageStreamView
import com.trtc.uikit.livekit.component.beauty.BeautyUtils
import com.trtc.uikit.livekit.component.beauty.tebeauty.store.TEBeautyStore
import com.trtc.uikit.livekit.component.gift.GiftPlayView
import com.trtc.uikit.livekit.component.giftaccess.service.GiftCacheService
import com.trtc.uikit.livekit.component.giftaccess.service.GiftConstants.GIFT_COUNT
import com.trtc.uikit.livekit.component.giftaccess.service.GiftConstants.GIFT_ICON_URL
import com.trtc.uikit.livekit.component.giftaccess.service.GiftConstants.GIFT_NAME
import com.trtc.uikit.livekit.component.giftaccess.service.GiftConstants.GIFT_RECEIVER_USERNAME
import com.trtc.uikit.livekit.component.giftaccess.service.GiftConstants.GIFT_VIEW_TYPE
import com.trtc.uikit.livekit.component.giftaccess.service.GiftConstants.GIFT_VIEW_TYPE_1
import com.trtc.uikit.livekit.component.giftaccess.store.GiftStore
import com.trtc.uikit.livekit.component.giftaccess.view.BarrageViewTypeDelegate
import com.trtc.uikit.livekit.component.giftaccess.view.GiftBarrageAdapter
import com.trtc.uikit.livekit.component.networkInfo.NetworkInfoView
import com.trtc.uikit.livekit.component.pictureinpicture.PictureInPictureStore
import com.trtc.uikit.livekit.component.roominfo.LiveInfoView
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorConfig
import com.trtc.uikit.livekit.features.anchorboardcast.state.BattleState
import com.trtc.uikit.livekit.features.anchorboardcast.state.BattleState.Companion.BATTLE_DURATION
import com.trtc.uikit.livekit.features.anchorboardcast.state.BattleState.Companion.BATTLE_REQUEST_TIMEOUT
import com.trtc.uikit.livekit.features.anchorboardcast.view.BasicView
import com.trtc.uikit.livekit.features.anchorboardcast.view.EndLiveStreamDialog
import com.trtc.uikit.livekit.features.anchorboardcast.view.battle.panel.AnchorEndBattleDialog
import com.trtc.uikit.livekit.features.anchorboardcast.view.battle.panel.BattleCountdownDialog
import com.trtc.uikit.livekit.features.anchorboardcast.view.battle.widgets.BattleInfoView
import com.trtc.uikit.livekit.features.anchorboardcast.view.battle.widgets.BattleMemberInfoView
import com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.panel.AnchorCoGuestManageDialog
import com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.panel.AnchorManagerDialog
import com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.panel.ApplyCoGuestFloatView
import com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.panel.CoGuestIconView
import com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.widgets.AnchorEmptySeatView
import com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.widgets.CoGuestBackgroundWidgetsView
import com.trtc.uikit.livekit.features.anchorboardcast.view.coguest.widgets.CoGuestForegroundWidgetsView
import com.trtc.uikit.livekit.features.anchorboardcast.view.cohost.panel.AnchorCoHostManageDialog
import com.trtc.uikit.livekit.features.anchorboardcast.view.cohost.panel.StandardDialog
import com.trtc.uikit.livekit.features.anchorboardcast.view.cohost.widgets.CoHostBackgroundWidgetsView
import com.trtc.uikit.livekit.features.anchorboardcast.view.cohost.widgets.CoHostForegroundWidgetsView
import com.trtc.uikit.livekit.features.anchorboardcast.view.settings.SettingsPanelDialog
import com.trtc.uikit.livekit.features.anchorboardcast.view.usermanage.UserManagerDialog
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.barrage.Barrage
import io.trtc.tuikit.atomicxcore.api.device.DeviceStatus
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.gift.Gift
import io.trtc.tuikit.atomicxcore.api.live.BattleConfig
import io.trtc.tuikit.atomicxcore.api.live.BattleEndedReason
import io.trtc.tuikit.atomicxcore.api.live.BattleInfo
import io.trtc.tuikit.atomicxcore.api.live.BattleListener
import io.trtc.tuikit.atomicxcore.api.live.BattleRequestCallback
import io.trtc.tuikit.atomicxcore.api.live.BattleStore
import io.trtc.tuikit.atomicxcore.api.live.CoGuestStore
import io.trtc.tuikit.atomicxcore.api.live.CoHostListener
import io.trtc.tuikit.atomicxcore.api.live.CoHostStore
import io.trtc.tuikit.atomicxcore.api.live.HostListener
import io.trtc.tuikit.atomicxcore.api.live.LiveEndedReason
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo
import io.trtc.tuikit.atomicxcore.api.live.LiveInfoCompletionHandler
import io.trtc.tuikit.atomicxcore.api.live.LiveKickedOutReason
import io.trtc.tuikit.atomicxcore.api.live.LiveListListener
import io.trtc.tuikit.atomicxcore.api.live.LiveListStore
import io.trtc.tuikit.atomicxcore.api.live.LiveUserInfo
import io.trtc.tuikit.atomicxcore.api.live.NoResponseReason
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import io.trtc.tuikit.atomicxcore.api.live.TakeSeatMode
import io.trtc.tuikit.atomicxcore.api.live.deprecated.LiveCoreViewDeprecated
import io.trtc.tuikit.atomicxcore.api.view.CoreViewType
import io.trtc.tuikit.atomicxcore.api.view.LiveCoreView
import io.trtc.tuikit.atomicxcore.api.view.VideoViewAdapter
import io.trtc.tuikit.atomicxcore.api.view.ViewLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import java.util.Objects

@SuppressLint("ViewConstructor")
class AnchorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BasicView(context, attrs, defStyleAttr), EndLiveStreamDialog.EndLiveStreamDialogListener,
    AnchorManager.LiveStateListener {

    private val logger = LiveKitLogger.getFeaturesLogger("AnchorView")
    private var behavior: RoomBehavior = RoomBehavior.CREATE_ROOM
    private lateinit var layoutCoreViewContainer: RoundFrameLayout
    private lateinit var liveCoreView: LiveCoreView
    private lateinit var layoutComponentsContainer: FrameLayout
    private lateinit var layoutHeaderContainer: FrameLayout
    private lateinit var imageEndLive: ImageView
    private lateinit var imageFloatWindow: ImageView
    private lateinit var viewCoGuest: View
    private lateinit var viewCoHost: View
    private lateinit var viewBattle: View
    private lateinit var audienceListView: AudienceListView
    private lateinit var roomInfoView: LiveInfoView
    private lateinit var networkInfoView: NetworkInfoView
    private lateinit var barrageInputView: BarrageInputView
    private lateinit var barrageStreamView: BarrageStreamView
    private lateinit var giftPlayView: GiftPlayView
    private lateinit var applyCoGuestFloatView: ApplyCoGuestFloatView

    private var anchorCoHostManageDialog: AnchorCoHostManageDialog? = null
    private var processConnectionDialog: StandardDialog? = null
    private var processBattleDialog: StandardDialog? = null
    private var battleCountdownDialog: BattleCountdownDialog? = null
    private var anchorManagerDialog: AnchorManagerDialog? = null
    private var userManagerDialog: UserManagerDialog? = null
    private var anchorEndBattleDialog: AnchorEndBattleDialog? = null
    private lateinit var liveInfo: LiveInfo
    private var isDestroy = false
    private var subscribeStateJob: Job? = null
    private val EVENT_KEY_TIME_LIMIT: String = "RTCRoomTimeLimitService"
    private val EVENT_SUB_KEY_COUNTDOWN_START: String = "CountdownStart"
    private val EVENT_SUB_KEY_COUNTDOWN_END: String = "CountdownEnd"

    private val coHostListener = object : CoHostListener() {
        override fun onCoHostRequestReceived(
            inviter: SeatUserInfo, extensionInfo: String
        ) {
            logger.info("${hashCode()} onCoHostRequestReceived:[inviter:${Gson().toJson(inviter)}]")
            val coGuestStore = CoGuestStore.create(liveInfo.liveID)
            val coHostStore = CoHostStore.create(liveInfo.liveID)
            val list = mutableListOf<SeatUserInfo>()

            for (userInfo in coGuestStore.coGuestState.connected.value) {
                if (userInfo.userID != TUIRoomEngine.getSelfInfo().userId && userInfo.liveID == liveInfo.liveID) {
                    list.add(userInfo)
                }
            }

            if (list.isNotEmpty() || coGuestStore.coGuestState.applicants.value.isNotEmpty() || coGuestStore.coGuestState.invitees.value.isNotEmpty()) {
                coHostStore.rejectHostConnection(inviter.liveID, null)
                return
            }

            if (mediaState?.isPipModeEnabled?.value == true) {
                return
            }

            val content = context.getString(R.string.common_connect_inviting_append, inviter.userName)
            showConnectionRequestDialog(content, inviter.avatarURL, inviter.liveID)
            anchorManager?.getCoHostManager()?.onConnectionRequestReceived(inviter)
        }

        override fun onCoHostRequestCancelled(inviter: SeatUserInfo, invitee: SeatUserInfo?) {
            logger.info("${hashCode()} onCrossRoomConnectionCancelled:[inviter:$inviter")
        }

        override fun onCoHostRequestAccepted(invitee: SeatUserInfo) {
            logger.info("${hashCode()} onCrossRoomConnectionAccepted:[invitee:$invitee]")
            anchorManager?.getCoHostManager()?.onConnectionRequestAccept(invitee)
        }

        override fun onCoHostRequestRejected(invitee: SeatUserInfo) {
            logger.info("${hashCode()} onConnectionRequestReject:[invitee:$invitee]")
            anchorManager?.getCoHostManager()?.onConnectionRequestReject(invitee)
        }

        override fun onCoHostRequestTimeout(inviter: SeatUserInfo, invitee: SeatUserInfo) {
            logger.info("${hashCode()} onCrossRoomConnectionTimeout:[inviter:$inviter,invitee:$invitee")
            processConnectionDialog?.dismiss()
            anchorManager?.getCoHostManager()?.onConnectionRequestTimeout(inviter, invitee)
        }
    }

    private val coGuestListener = object : HostListener() {
        override fun onGuestApplicationReceived(guestUser: LiveUserInfo) {
            logger.info("${hashCode()} onGuestApplicationReceived:[inviterUser:${Gson().toJson(guestUser)}]")
            val coGuestStore = CoGuestStore.create(liveInfo.liveID)
            val coHostStore = CoHostStore.create(liveInfo.liveID)

            if (coHostStore.coHostState.invitees.value.isNotEmpty() || coHostStore.coHostState.connected.value.isNotEmpty() || coHostStore.coHostState.applicant.value != null) {
                coGuestStore.rejectApplication(guestUser.userID, null)
            }
        }

        override fun onGuestApplicationCancelled(guestUser: LiveUserInfo) {
            logger.info("${hashCode()} onUserConnectionCancelled:[inviterUser:$guestUser]")
        }

        override fun onGuestApplicationProcessedByOtherHost(guestUser: LiveUserInfo, hostUser: LiveUserInfo) {

        }

        override fun onHostInvitationResponded(isAccept: Boolean, guestUser: LiveUserInfo) {
        }

        override fun onHostInvitationNoResponse(guestUser: LiveUserInfo, reason: NoResponseReason) {
            if (reason == NoResponseReason.TIMEOUT) {
                logger.info("${hashCode()} onUserConnectionAccepted:[guestUser:$guestUser]")
                com.tencent.qcloud.tuicore.util.ToastUtil.toastShortMessage(
                    ContextProvider.getApplicationContext().resources.getString(R.string.common_voiceroom_take_seat_timeout)
                )
            }
        }
    }

    private val battleListener = object : BattleListener() {
        override fun onBattleStarted(battleInfo: BattleInfo, inviter: SeatUserInfo, invitees: List<SeatUserInfo>) {
            logger.info("${hashCode()} onBattleStarted:[battleInfo:$battleInfo]")
            anchorManager?.getBattleManager()?.onBattleStarted(battleInfo, inviter, invitees)
        }

        override fun onBattleEnded(battleInfo: BattleInfo, reason: BattleEndedReason?) {
            logger.info("${hashCode()} onBattleEnded:[battleInfo:$battleInfo]")
            anchorManager?.getBattleManager()?.onBattleEnded(battleInfo)
        }

        override fun onUserJoinBattle(battleID: String, battleUser: SeatUserInfo) {
            logger.info("${hashCode()} onUserJoinBattle:[battleID:$battleID,battleUser:$battleUser]")
        }

        override fun onUserExitBattle(battleID: String, battleUser: SeatUserInfo) {
            logger.info("${hashCode()} onUserExitBattle:[battleID:$battleID,battleUser:$battleUser]")
            anchorManager?.getBattleManager()?.onUserExitBattle(battleUser)
        }

        override fun onBattleRequestReceived(battleID: String, inviter: SeatUserInfo, invitee: SeatUserInfo) {
            logger.info("${hashCode()} onBattleRequestReceived:[battleID:$battleID,inviter:$inviter,invitee:$invitee]")
            anchorManager?.getBattleManager()?.onBattleRequestReceived(battleID, inviter)
        }

        override fun onBattleRequestCancelled(battleID: String, inviter: SeatUserInfo, invitee: SeatUserInfo) {
            logger.info("${hashCode()} onBattleRequestCancelled:[battleID:$battleID,inviter:$inviter,invitee:$invitee]")
            anchorManager?.getBattleManager()?.onBattleRequestCancelled(inviter)
        }

        override fun onBattleRequestTimeout(battleID: String, inviter: SeatUserInfo, invitee: SeatUserInfo) {
            logger.info("${hashCode()} onBattleRequestTimeout:[battleID:$battleID,inviter:$inviter,invitee:$invitee]")
            anchorManager?.getBattleManager()?.onBattleRequestTimeout(inviter, invitee)
        }

        override fun onBattleRequestAccept(battleID: String, inviter: SeatUserInfo, invitee: SeatUserInfo) {
            logger.info("${hashCode()} onBattleRequestAccept:[battleID:$battleID,inviter:$inviter,invitee:$invitee]")
            anchorManager?.getBattleManager()?.onBattleRequestAccept(invitee)
        }

        override fun onBattleRequestReject(battleID: String, inviter: SeatUserInfo, invitee: SeatUserInfo) {
            logger.info("${hashCode()} onBattleRequestReject:[battleID:$battleID,inviter:$inviter,invitee:$invitee]")
            anchorManager?.getBattleManager()?.onBattleRequestReject(invitee)
        }
    }

    private val liveListListener = object : LiveListListener() {
        override fun onLiveEnded(liveID: String, reason: LiveEndedReason, message: String) {
            if (liveID == liveInfo.liveID) {
                ToastUtil.toastShortMessage(baseContext.getString(R.string.common_live_has_stop))
                endLive(false)
            }
        }

        override fun onKickedOutOfLive(liveID: String, reason: LiveKickedOutReason, message: String) {
            if (liveID == liveInfo.liveID) {
                ToastUtil.toastShortMessage(baseContext.getString(R.string.common_kicked_out_of_room_by_owner))
                endLive()
            }
        }
    }

    override fun initView() {
        LayoutInflater.from(baseContext).inflate(R.layout.livekit_livestream_anchor_view, this, true)

        layoutCoreViewContainer = findViewById(R.id.fl_video_view_container)
        layoutComponentsContainer = findViewById(R.id.rl_component_container)
        layoutHeaderContainer = findViewById(R.id.fl_header_container)
        audienceListView = findViewById(R.id.audience_list_view)
        imageEndLive = findViewById(R.id.iv_end_live_stream)
        imageFloatWindow = findViewById(R.id.iv_float_window)
        viewCoGuest = findViewById(R.id.ll_co_guest)
        viewCoHost = findViewById(R.id.ll_co_host)
        viewBattle = findViewById(R.id.ll_battle)
        barrageInputView = findViewById(R.id.barrage_input_view)
        barrageStreamView = findViewById(R.id.barrage_stream_view)
        roomInfoView = findViewById(R.id.room_info_view)
        networkInfoView = findViewById(R.id.network_info_view)
        applyCoGuestFloatView = findViewById(R.id.rl_apply_link_audience)
        giftPlayView = findViewById(R.id.gift_play_view)

        layoutCoreViewContainer.setRadius(ScreenUtil.dip2px(16f))
    }

    fun init(
        liveInfo: LiveInfo, coreView: LiveCoreView?, behavior: RoomBehavior, params: Map<String, Any>?
    ) {
        this.behavior = behavior
        this.liveInfo = liveInfo
        anchorManager = AnchorManager(liveInfo)
        anchorManager?.setLiveStateListener(this)
        initLiveCoreView(coreView)
        super.init(anchorManager!!)
        parseParams(params)
        createVideoMuteBitmap()
        createOrEnterRoom()
        startForegroundService()
    }

    fun unInit() {
        destroy()
        LiveListStore.shared().endLive(null)
        TUICore.notifyEvent(
            TUIConstants.Privacy.EVENT_ROOM_STATE_CHANGED, TUIConstants.Privacy.EVENT_SUB_KEY_ROOM_STATE_STOP, null
        )
        TUICore.notifyEvent(EVENT_KEY_TIME_LIMIT, EVENT_SUB_KEY_COUNTDOWN_END, null)
    }

    fun addAnchorViewListener(listener: AnchorViewListener) {
        anchorManager?.addAnchorViewListener(listener)
    }

    fun removeAnchorViewListener(listener: AnchorViewListener) {
        anchorManager?.removeAnchorViewListener(listener)
    }

    fun getState(): AnchorBoardcastState {
        return anchorManager?.getExternalState() ?: AnchorBoardcastState()
    }

    /**
     * This API call is called in the Activity.onPictureInPictureModeChanged(boolean)
     * The code example is as follows:
     * override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
     *     super.onPictureInPictureModeChanged(isInPictureInPictureMode)
     *     mAnchorView?.enablePipMode(isInPictureInPictureMode)
     * }
     *
     * @param enable true:Turn on picture-in-picture mode; false:Turn off picture-in-picture mode
     */
    fun enablePipMode(enable: Boolean) {
        anchorManager?.enablePipMode(enable)

        val layoutParams = layoutCoreViewContainer.layoutParams as FrameLayout.LayoutParams
        if (enable) {
            layoutParams.setMargins(0, 0, 0, 0)
            layoutCoreViewContainer.setRadius(ScreenUtil.dip2px(0f))
            layoutComponentsContainer.visibility = GONE
        } else {
            layoutParams.setMargins(0, ScreenUtil.dip2px(44f), 0, ScreenUtil.dip2px(96f))
            layoutCoreViewContainer.setRadius(ScreenUtil.dip2px(16f))
            layoutComponentsContainer.visibility = VISIBLE
        }
        layoutCoreViewContainer.layoutParams = layoutParams
    }

    fun disableHeaderLiveData(disable: Boolean?) {
        logger.info("disableHeaderLiveData: disable = $disable")
        AnchorManager.disableHeaderLiveData(disable == true)
    }

    fun disableHeaderVisitorCnt(disable: Boolean?) {
        logger.info("disableHeaderVisitorCnt: disable = $disable")
        AnchorManager.disableHeaderVisitorCnt(disable == true)
    }

    fun disableFooterCoGuest(disable: Boolean?) {
        logger.info("disableFooterCoGuest: disable = $disable")
        AnchorManager.disableFooterCoGuest(disable == true)
    }

    fun disableFooterCoHost(disable: Boolean?) {
        logger.info("disableFooterCoHost: disable = $disable")
        AnchorManager.disableFooterCoHost(disable == true)
    }

    fun disableFooterBattle(disable: Boolean?) {
        logger.info("disableFooterBattle: disable = $disable")
        AnchorManager.disableFooterBattle(disable == true)
    }

    fun disableFooterSoundEffect(disable: Boolean?) {
        logger.info("disableFooterSoundEffect: disable = $disable")
        AnchorManager.disableFooterSoundEffect(disable == true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroy()
    }

    override fun refreshView() {
        // Empty implementation
    }

    private fun showCoGuestManageDialog(userInfo: SeatFullInfo?) {
        if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
            return
        }
        anchorManager?.let {
            if (anchorManagerDialog == null) {
                anchorManagerDialog = AnchorManagerDialog(baseContext, it, liveCoreView)
            }
            anchorManagerDialog?.init(userInfo)
            anchorManagerDialog?.show()
        }
    }

    private fun initLiveCoreView(coreView: LiveCoreView?) {
        liveCoreView = if (coreView != null) {
            if (coreView.parent != null) {
                (coreView.parent as ViewGroup).removeView(coreView)
            }
            coreView
        } else {
            LiveCoreView(context, null, 0, CoreViewType.PUSH_VIEW)
        }
        liveCoreView.setLiveId(liveInfo.liveID)
        layoutCoreViewContainer.addView(liveCoreView)
    }

    private fun createVideoMuteBitmap() {
        val bigMuteImageResId = if (Locale.ENGLISH.language == TUIThemeManager.getInstance().currentLanguage) {
            R.drawable.livekit_local_mute_image_en
        } else {
            R.drawable.livekit_local_mute_image_zh
        }
        val smallMuteImageResId = R.drawable.livekit_local_mute_image_multi
        mediaManager?.createVideoMuteBitmap(context, bigMuteImageResId, smallMuteImageResId)
    }

    private fun createOrEnterRoom() {
        setComponent()
        liveCoreView.setVideoViewAdapter(object : VideoViewAdapter {
            override fun createCoGuestView(userInfo: SeatFullInfo?, viewLayer: ViewLayer?): View? {
                if (TextUtils.isEmpty(userInfo?.userId)) {
                    if (viewLayer == ViewLayer.BACKGROUND) {
                        val emptySeatView = AnchorEmptySeatView(context)
                        if (anchorManager != null && userInfo != null) {
                            emptySeatView.init(anchorManager!!, userInfo)
                        }
                        return emptySeatView
                    }
                    return null
                }

                if (viewLayer == ViewLayer.BACKGROUND) {
                    val backgroundView = CoGuestBackgroundWidgetsView(context)
                    if (anchorManager != null && userInfo != null) {
                        backgroundView.init(anchorManager!!, userInfo)
                    }
                    return backgroundView
                } else {
                    val foregroundView = CoGuestForegroundWidgetsView(context)
                    if (anchorManager != null && userInfo != null) {
                        foregroundView.init(anchorManager!!, userInfo)
                    }
                    foregroundView.setOnClickListener { showCoGuestManageDialog(userInfo) }
                    return foregroundView
                }
            }

            override fun createCoHostView(coHostUser: SeatFullInfo?, viewLayer: ViewLayer?): View? {
                if (anchorManager == null || coHostUser == null) {
                    return null
                }
                return if (viewLayer == ViewLayer.BACKGROUND) {
                    CoHostBackgroundWidgetsView(baseContext).apply {
                        init(anchorManager!!, coHostUser)
                    }
                } else {
                    CoHostForegroundWidgetsView(baseContext).apply {
                        init(anchorManager!!, coHostUser)
                    }
                }
            }

            override fun createBattleView(battleUser: TUILiveBattleManager.BattleUser?): View? {
                if (anchorManager == null || battleUser == null) {
                    return null
                }
                return BattleMemberInfoView(baseContext).apply {
                    init(anchorManager!!, battleUser!!.userId)
                }
            }

            override fun createBattleContainerView(): View? {
                anchorManager?.let {
                    return BattleInfoView(baseContext).apply {
                        init(it)
                    }
                }
                return null
            }
        })

        PictureInPictureStore.sharedInstance().state.isAnchorStreaming = true

        if (behavior == RoomBehavior.ENTER_ROOM) {
            enterRoom()
        } else {
            createRoom()
        }
    }

    private fun enterRoom() {
        anchorState?.let {
            if (it.liveInfo.keepOwnerOnSeat) {
                PermissionRequest.requestCameraPermissions(
                    ContextProvider.getApplicationContext(), object : PermissionCallback() {
                        override fun onGranted() {
                            logger.info("requestCameraPermissions:[onGranted]")
                            DeviceStore.shared().openLocalCamera(true, object : CompletionHandler {
                                override fun onSuccess() {
                                    logger.info("startCamera success, requestMicrophonePermissions")
                                    PermissionRequest.requestMicrophonePermissions(
                                        ContextProvider.getApplicationContext(), object : PermissionCallback() {
                                            override fun onGranted() {
                                                logger.info("requestMicrophonePermissions success")
                                                DeviceStore.shared().openLocalMicrophone(null)
                                            }

                                            override fun onDenied() {
                                                logger.error("requestMicrophonePermissions:[onDenied]")
                                            }
                                        })
                                }

                                override fun onFailure(code: Int, desc: String) {
                                    logger.error("startCamera failed:code:$code,desc:$desc")
                                }

                            })
                        }

                        override fun onDenied() {
                            logger.error("requestCameraPermissions:[onDenied]")
                        }
                    })
            }
            // TODO @xander 这个 api 被废弃，看下是否迁移到非 废弃方法中
            liveCoreView.setLocalVideoMuteImage(mediaState?.bigMuteBitmap, mediaState?.smallMuteBitmap)

            val liveListStore = LiveListStore.shared()
            liveListStore.joinLive(it.roomId, object : LiveInfoCompletionHandler {
                override fun onSuccess(liveInfo: LiveInfo) {
                    val activity = baseContext as Activity
                    if (activity.isFinishing || activity.isDestroyed) {
                        logger.warn("activity is exit")
                        liveCoreView.setVideoViewAdapter(null)
                        if (liveInfo.keepOwnerOnSeat) {
                            liveListStore.endLive(null)
                        } else {
                            liveListStore.leaveLive(null)
                        }
                        TUICore.notifyEvent(
                            TUIConstants.Privacy.EVENT_ROOM_STATE_CHANGED,
                            TUIConstants.Privacy.EVENT_SUB_KEY_ROOM_STATE_STOP,
                            null
                        )
                        TUICore.notifyEvent(EVENT_KEY_TIME_LIMIT, EVENT_SUB_KEY_COUNTDOWN_END, null)
                        liveCoreView.setLocalVideoMuteImage(null, null)
                        PictureInPictureStore.sharedInstance().state.isAnchorStreaming = false
                        return
                    }
                    anchorManager?.updateRoomState(liveInfo)
                    userManager?.getAudienceList()
                    initComponentView()
                }

                override fun onFailure(code: Int, desc: String) {
                    PictureInPictureStore.sharedInstance().state.isAnchorStreaming = false
                    ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                    finishActivity()
                }
            })
        }
    }

    private fun createRoom() {
        liveInfo.keepOwnerOnSeat = true
        liveInfo.isSeatEnabled = true
        liveInfo.seatMode = TakeSeatMode.APPLY
        liveCoreView.setLocalVideoMuteImage(mediaState?.bigMuteBitmap, mediaState?.smallMuteBitmap)
        val liveListStore = LiveListStore.shared()
        liveListStore.createLive(liveInfo, object : LiveInfoCompletionHandler {
            override fun onSuccess(liveInfo: LiveInfo) {
                val activity = baseContext as Activity
                if (activity.isFinishing || activity.isDestroyed) {
                    logger.warn("activity is exit, stopLiveStream")
                    LiveListStore.shared().endLive(null)
                    TUICore.notifyEvent(
                        TUIConstants.Privacy.EVENT_ROOM_STATE_CHANGED,
                        TUIConstants.Privacy.EVENT_SUB_KEY_ROOM_STATE_STOP,
                        null
                    )
                    TUICore.notifyEvent(EVENT_KEY_TIME_LIMIT, EVENT_SUB_KEY_COUNTDOWN_END, null)
                    liveCoreView.setLocalVideoMuteImage(null, null)
                    return
                }
                anchorManager?.updateRoomState(liveInfo)
                userManager?.getAudienceList()
                initComponentView()
                showAlertUserLiveTips()
                TUICore.notifyEvent(EVENT_KEY_TIME_LIMIT, EVENT_SUB_KEY_COUNTDOWN_START, null)
            }

            override fun onFailure(code: Int, desc: String) {
                logger.error("startLiveStream failed:error:$code,desc:$desc")
                PictureInPictureStore.sharedInstance().state.isAnchorStreaming = false
                ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                if (code == TUICommonDefine.Error.SDK_NOT_INITIALIZED.value) {
                    finishActivity()
                }
            }

        })
    }

    private fun setComponent() {
        try {
            val jsonObject = JSONObject().apply {
                put("api", "component")
                put("component", 21)
            }
            LiveCoreViewDeprecated.callExperimentalAPI(jsonObject.toString())
        } catch (e: JSONException) {
            logger.error("dataReport:${Log.getStackTraceString(e)}")
        }
    }

    private fun initComponentView() {
        initRoomInfoView()
        initAudienceListView()
        initNetworkView()
        initEndLiveStreamView()
        initFloatWindowView()
        initBarrageInputView()
        initBarrageStreamView()
        initCoGuestView()
        initCoHostView()
        initBattleView()
        initSettingsPanel()
        initApplyCoGuestFloatView()
        initGiftPlayView()
    }

    private fun initNetworkView() {
        anchorState?.let {
            networkInfoView.init(it.liveInfo.createTime)
        }
    }

    private fun initSettingsPanel() {
        findViewById<View>(R.id.ll_more).setOnClickListener { view ->
            if (!view.isEnabled) return@setOnClickListener
            view.isEnabled = false
            anchorManager?.let {
                val settingsPanelDialog = SettingsPanelDialog(baseContext, it, liveCoreView)
                settingsPanelDialog.setOnDismissListener { view.isEnabled = true }
                settingsPanelDialog.show()
            }
        }
    }

    private fun showAlertUserLiveTips() {
        try {
            val map = hashMapOf<String, Any>(
                TUIConstants.Privacy.PARAM_DIALOG_CONTEXT to Objects.requireNonNull(context)
            )
            TUICore.notifyEvent(
                TUIConstants.Privacy.EVENT_ROOM_STATE_CHANGED, TUIConstants.Privacy.EVENT_SUB_KEY_ROOM_STATE_START, map
            )
        } catch (e: Exception) {
            logger.error("showAlertUserLiveTips exception:${e.message}")
        }
    }

    private fun initAudienceListView() {
        anchorState?.let {
            audienceListView.init(it.liveInfo)
            audienceListView.setOnUserItemClickListener(object : AudienceListView.OnUserItemClickListener {
                override fun onUserItemClick(userInfo: UserInfo) {
                    anchorManager?.let {
                        if (userManagerDialog == null) {
                            userManagerDialog = UserManagerDialog(baseContext, it)
                        }
                        userManagerDialog?.init(userInfo)
                        userManagerDialog?.show()
                    }
                }
            })
        }
    }

    private fun initEndLiveStreamView() {
        imageEndLive.setOnClickListener { showLiveStreamEndDialog() }
    }

    private fun initFloatWindowView() {
        imageFloatWindow.setOnClickListener {
            anchorManager?.notifyPictureInPictureClick()
        }
    }

    private fun initRoomInfoView() {
        anchorState?.let {
            roomInfoView.init(it.liveInfo)
        }
    }

    private fun initBarrageInputView() {
        anchorState?.let {
            barrageInputView.init(it.roomId)
        }
    }

    private fun initBarrageStreamView() {
        val ownerUserId = LiveListStore.shared().liveState.currentLive.value.liveOwner.userID
        val liveId = LiveListStore.shared().liveState.currentLive.value.liveID
        barrageStreamView.init(liveId, ownerUserId)
        barrageStreamView.setItemTypeDelegate(BarrageViewTypeDelegate())
        barrageStreamView.setItemAdapter(GIFT_VIEW_TYPE_1, GiftBarrageAdapter(baseContext))
        barrageStreamView.setOnMessageClickListener(object : BarrageStreamView.OnMessageClickListener {
            override fun onMessageClick(userInfo: UserInfo) {
                if (TextUtils.isEmpty(userInfo.userId) || userInfo.userId == TUILogin.getUserId()) {
                    return
                }
                anchorManager?.let {
                    if (userManagerDialog == null) {
                        userManagerDialog = UserManagerDialog(baseContext, it)
                    }
                    userManagerDialog?.init(userInfo)
                    userManagerDialog?.show()
                }
            }
        })
    }

    private fun initCoGuestView() {
        viewCoGuest.setOnClickListener { view ->
            if (!view.isEnabled) return@setOnClickListener
            view.isEnabled = false
            val dialog = AnchorCoGuestManageDialog(baseContext, anchorManager, liveCoreView)
            dialog.setOnDismissListener { view.isEnabled = true }
            dialog.show()
        }
    }

    private fun initCoHostView() {
        viewCoHost.setOnClickListener { view ->
            if (!view.isEnabled) return@setOnClickListener
            view.isEnabled = false
            anchorManager?.let {
                anchorCoHostManageDialog = AnchorCoHostManageDialog(baseContext, it, liveCoreView)
                anchorCoHostManageDialog?.setOnDismissListener { view.isEnabled = true }
                anchorCoHostManageDialog?.show()
            }
        }
    }

    private fun initBattleView() {
        viewBattle.setOnClickListener { view ->
            if (battleManager == null || anchorManager == null || coHostManager == null) {
                return@setOnClickListener
            }
            if (battleState?.isBattleRunning?.value == true && battleManager!!.isSelfInBattle()) {
                if (anchorEndBattleDialog == null) {
                    anchorEndBattleDialog = AnchorEndBattleDialog(baseContext, anchorManager!!)
                }
                anchorEndBattleDialog?.show()
            } else {
                if (battleState?.isOnDisplayResult?.value == true || !coHostManager!!.isSelfInCoHost()) {
                    logger.warn("can not requestBattle")
                    return@setOnClickListener
                }

                val list = mutableListOf<String>()
                val selfId = TUILogin.getUserId()
                anchorState?.let {
                    for (user in CoHostStore.create(it.roomId).coHostState.connected.value) {
                        if (user.userID != selfId) {
                            list.add(user.userID)
                        }
                    }
                }

                val battleConfig = BattleConfig().apply {
                    duration = BATTLE_DURATION
                    needResponse = true
                    extensionInfo = ""
                }
                BattleStore.create(liveInfo.liveID).requestBattle(
                    battleConfig, list, BATTLE_REQUEST_TIMEOUT, object : BattleRequestCallback {
                        override fun onSuccess(
                            battleInfo: BattleInfo, resultMap: Map<String, Int>
                        ) {
                            anchorManager?.getBattleManager()?.onRequestBattle(battleInfo.battleID, list)
                        }

                        override fun onError(code: Int, desc: String) {
                            logger.error("requestBattle failed:code:$code,desc:$desc")
                            ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                        }

                    })
            }
        }
    }

    private fun initApplyCoGuestFloatView() {
        anchorManager?.let {
            applyCoGuestFloatView.init(it, liveCoreView)
        }
    }

    private fun initGiftPlayView() {
        val giftCacheService: GiftCacheService = GiftStore.getInstance().giftCacheService
        giftPlayView.setListener(object : GiftPlayView.TUIGiftPlayViewListener {
            override fun onReceiveGift(
                view: GiftPlayView?, gift: Gift, giftCount: Int, sender: LiveUserInfo
            ) {
                val barrage = Barrage().apply {
                    textContent = "gift"
                    this.sender.userID = sender.userID
                    this.sender.userName = if (TextUtils.isEmpty(sender.userName)) {
                        sender.userID
                    } else {
                        sender.userName
                    }
                    this.sender.avatarURL = sender.avatarURL

                    val extInfo = hashMapOf<String, String>(
                        GIFT_VIEW_TYPE to GIFT_VIEW_TYPE_1.toString(),
                        GIFT_NAME to gift.name,
                        GIFT_COUNT to giftCount.toString(),
                        GIFT_ICON_URL to gift.iconURL,
                        GIFT_RECEIVER_USERNAME to context.getString(R.string.common_gift_me)
                    )
                    extensionInfo = extInfo
                }
                barrageStreamView.insertBarrages(barrage)
            }

            override fun onPlayGiftAnimation(
                view: GiftPlayView?, gift: Gift
            ) {
                giftCacheService.request(gift.resourceURL, object : GiftCacheService.Callback<String> {
                    override fun onResult(error: Int, result: String?) {
                        if (error == 0) {
                            result?.let {
                                view?.playGiftAnimation(it)
                            }
                        }
                    }

                })
            }
        })
        anchorState?.let {
            giftPlayView.init(it.roomId)
        }
    }

    override fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                onDisableLiveDataChange()
            }
            launch {
                onDisableVisitorCntChange()
            }
            launch {
                ondDisableCoGuestChange()
            }
            launch {
                onDisableCoHostChange()
            }
            launch {
                onDisableBattleChange()
            }
            launch {
                onCoHostUserListChange()
            }
            launch {
                val coGuestStore = CoGuestStore.create(liveInfo.liveID)
                coGuestStore.coGuestState.applicants.collect { applicants ->
                    onCoGuestApplicantsChange(applicants)
                }
            }
            launch {
                onCoGuestUserListChange()
            }
            launch {
                onPipModeObserver()
            }
            launch {
                onBattleUserChange()
            }
            launch {
                battleState?.receivedBattleRequest?.collect { user ->
                    onReceivedBattleRequestChange(user)
                }
            }
            launch {
                onBattleStartChange()
            }
            launch {
                onInWaitingChange()
            }
            launch {
                onBattleResultDisplay()
            }
        }
        CoHostStore.create(liveInfo.liveID).addCoHostListener(coHostListener)
        CoGuestStore.create(liveInfo.liveID).addHostListener(coGuestListener)
        BattleStore.create(liveInfo.liveID).addBattleListener(battleListener)
        LiveListStore.shared().addLiveListListener(liveListListener)
    }

    override fun removeObserver() {
        subscribeStateJob?.cancel()
        CoHostStore.create(liveInfo.liveID).removeCoHostListener(coHostListener)
        CoGuestStore.create(liveInfo.liveID).removeHostListener(coGuestListener)
        BattleStore.create(liveInfo.liveID).removeBattleListener(battleListener)
        LiveListStore.shared().removeLiveListListener(liveListListener)
    }

    private fun showLiveStreamEndDialog() {
        anchorManager?.let {
            val endLiveStreamDialog = EndLiveStreamDialog(baseContext, liveCoreView, it, this)
            endLiveStreamDialog.show()
        }
    }

    private fun onReceivedCoHostRequest(receivedConnectionRequest: SeatUserInfo?) {
        if (mediaState?.isPipModeEnabled?.value == true) {
            return
        }

        if (receivedConnectionRequest == null) {
            processConnectionDialog?.dismiss()
            return
        }

        val content = context.getString(
            R.string.common_connect_inviting_append, receivedConnectionRequest.userName
        )
        showConnectionRequestDialog(content, receivedConnectionRequest.avatarURL, receivedConnectionRequest.liveID)
    }

    private fun showConnectionRequestDialog(content: String, avatarUrl: String, roomId: String) {
        processConnectionDialog = StandardDialog(context).apply {
            setContent(content)
            setAvatar(avatarUrl)

            val rejectText = context.getString(R.string.common_reject)
            setNegativeText(rejectText) {
                CoHostStore.create(liveInfo.liveID).rejectHostConnection(roomId, null)
                dismiss()
            }

            val receiveText = context.getString(R.string.common_receive)
            setPositiveText(receiveText) {
                CoHostStore.create(liveInfo.liveID).acceptHostConnection(roomId, null)
                dismiss()
            }
        }
        processConnectionDialog?.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun onCoHostUserListChange() {
        val coHostStore = CoHostStore.create(liveInfo.liveID)
        coHostStore.coHostState.connected.collect { userList ->
            updateBattleView()
            enableView(viewCoGuest, userList.isEmpty())
        }
    }

    private suspend fun onBattleUserChange() {
        battleState?.battledUsers?.collect {
            post { updateBattleView() }
        }
    }

    private suspend fun onCoGuestUserListChange() {
        val coGuestStore = CoGuestStore.create(liveInfo.liveID)
        coGuestStore.coGuestState.connected.collect { seatList ->
            enableView(viewCoHost, seatList.size <= 1)
            val coGuestIconView = findViewById<CoGuestIconView>(R.id.co_guest_icon)
            if (seatList.size > 1) {
                coGuestIconView.startAnimation()
            } else {
                coGuestIconView.stopAnimation()
            }
        }
    }

    private fun onReceivedBattleRequestChange(user: BattleState.BattleUser?) {
        if (mediaState?.isPipModeEnabled?.value != true) {
            processBattleDialog?.dismiss()
            processBattleDialog = null

            user?.let {
                val content = context.getString(
                    R.string.common_battle_inviting, user.userName
                )
                processBattleDialog = StandardDialog(context).apply {
                    setContent(content)
                    setAvatar(user.avatarUrl)

                    val rejectText = context.getString(R.string.common_reject)
                    setNegativeText(rejectText) {
                        dismiss()
                        processBattleDialog = null
                        battleState?.let {
                            BattleStore.create(liveInfo.liveID)
                                .rejectBattle(it.battleId, object : CompletionHandler {
                                    override fun onSuccess() {
                                        anchorManager?.getBattleManager()?.onResponseBattle()
                                    }

                                    override fun onFailure(code: Int, desc: String) {
                                        logger.error("respondToBattle failed:code:$code,desc:$desc")
                                        ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                                    }
                                })
                        }

                    }

                    val receiveText = context.getString(R.string.common_receive)
                    setPositiveText(receiveText) {
                        dismiss()
                        processBattleDialog = null
                        battleState?.let {
                            BattleStore.create(liveInfo.liveID)
                                .acceptBattle(it.battleId, object : CompletionHandler {
                                    override fun onSuccess() {
                                        anchorManager?.getBattleManager()?.onResponseBattle()
                                    }

                                    override fun onFailure(code: Int, desc: String) {
                                        logger.error("respondToBattle failed:code:$code,desc:$desc")
                                        ErrorLocalized.onError(TUICommonDefine.Error.fromInt(code))
                                    }
                                })
                        }
                    }
                }
                processBattleDialog?.show()
            }
        }
    }

    private fun showBattleCountdownDialog() {
        anchorManager?.let {
            if (battleCountdownDialog == null) {
                battleCountdownDialog = BattleCountdownDialog(baseContext, it)
            }
            battleCountdownDialog?.show()
        }
    }

    private fun dismissBattleCountdownDialog() {
        battleCountdownDialog?.dismiss()
        battleCountdownDialog = null
    }

    private fun finishActivity() {
        if (baseContext is Activity) {
            val intent = Intent()
            baseContext.setResult(RESULT_OK, intent)
            baseContext.finishAndRemoveTask()
        }
    }

    private suspend fun onInWaitingChange() {
        battleState?.isInWaiting?.collect { isInWaiting ->
            when (isInWaiting) {
                true -> showBattleCountdownDialog()
                else -> dismissBattleCountdownDialog()
            }
        }
    }

    private suspend fun onBattleStartChange() {
        battleState?.isBattleRunning?.collect { it ->
            if (it == true) {
                battleState?.battledUsers?.value?.let { battledUsers ->
                    for (user in battledUsers) {
                        if (TextUtils.equals(TUILogin.getUserId(), user.userId)) {
                            enableView(viewCoHost, false)
                            break
                        }
                    }
                }
            } else {
                enableView(viewCoHost, true)
                if (anchorEndBattleDialog?.isShowing == true) {
                    anchorEndBattleDialog?.dismiss()
                }
            }
        }
    }

    private suspend fun onBattleResultDisplay() {
        battleState?.isOnDisplayResult?.collect {
            post { updateBattleView() }
        }
    }

    private fun updateBattleView() {
        val battleIconView = viewBattle.findViewById<View>(R.id.v_battle_icon)
        val battleResultDisplay = battleState?.isOnDisplayResult?.value
        if (coHostManager == null || battleManager == null) {
            return
        }
        if (coHostManager!!.isSelfInCoHost()) {
            if (battleManager!!.isSelfInBattle()) {
                battleIconView.setBackgroundResource(R.drawable.livekit_function_battle_exit)
            } else {
                battleIconView.setBackgroundResource(R.drawable.livekit_function_battle)
            }
            if (battleResultDisplay == true) {
                battleIconView.setBackgroundResource(R.drawable.livekit_function_battle_disable)
            } else {
                battleIconView.setBackgroundResource(R.drawable.livekit_function_battle)
            }
        } else {
            battleIconView.setBackgroundResource(R.drawable.livekit_function_battle_disable)
        }
    }

    private fun enableView(view: View, enable: Boolean) {
        view.isEnabled = enable
        view.alpha = if (enable) 1.0f else 0.5f
    }

    private fun parseParams(params: Map<String, Any>?) {
        if (params == null) return

        params["coHostTemplateId"]?.let { coHostTemplateId ->
            if (coHostTemplateId is Int) {
                anchorManager?.getCoHostManager()?.setCoHostTemplateId(coHostTemplateId)
            }
        }
    }

    private fun destroy() {
        if (isDestroy) return
        isDestroy = true

        DeviceStore.shared().closeLocalCamera()
        DeviceStore.shared().closeLocalMicrophone()
        BeautyUtils.resetBeauty()
        TEBeautyStore.unInit()
        anchorManager?.destroy()
        stopForegroundService()
    }

    private suspend fun onDisableLiveDataChange() {
        AnchorConfig.disableHeaderLiveData.collect {
            layoutHeaderContainer.visibility = if (it) GONE else VISIBLE
        }
    }

    private suspend fun onDisableVisitorCntChange() {
        AnchorConfig.disableHeaderVisitorCnt.collect {
            audienceListView.visibility = if (it) GONE else VISIBLE
        }
    }

    private suspend fun ondDisableCoGuestChange() {
        AnchorConfig.disableFooterCoGuest.collect {
            viewCoGuest.visibility = if (it) GONE else VISIBLE
        }
    }

    private suspend fun onDisableCoHostChange() {
        AnchorConfig.disableFooterCoHost.collect {
            viewCoHost.visibility = if (it) GONE else VISIBLE
        }
    }

    private suspend fun onDisableBattleChange() {
        AnchorConfig.disableFooterBattle.collect {
            viewBattle.visibility = if (it) GONE else VISIBLE
        }
    }

    private fun startForegroundService() {
        val context = ContextProvider.getApplicationContext()
        VideoForegroundService.start(
            context,
            context.getString(context.applicationInfo.labelRes),
            context.getString(R.string.common_app_running),
            0
        )
    }

    private fun stopForegroundService() {
        val context = ContextProvider.getApplicationContext()
        VideoForegroundService.stop(context)
    }

    override fun onRoomExit() {
        endLive()
    }

    override fun onRoomExitEndStatistics() {
        anchorManager?.setExternalState(barrageStreamView.getBarrageCount())
        PictureInPictureStore.sharedInstance().state.isAnchorStreaming = false
    }

    private fun onCoGuestApplicantsChange(applicants: List<LiveUserInfo>) {
        enableView(viewCoHost, applicants.isEmpty())
        anchorCoHostManageDialog?.dismiss()
    }

    private suspend fun onPipModeObserver() {
        mediaState?.isPipModeEnabled?.collect { enable ->
            if (!enable && liveInfo.liveID.isNotEmpty()) {
                onReceivedCoHostRequest(CoHostStore.create(liveInfo.liveID).coHostState.applicant.value)
                onReceivedBattleRequestChange(battleState?.receivedBattleRequest?.value)
                checkCameraStateAndRestore()
            }
        }
    }

    private fun checkCameraStateAndRestore() {
        mediaState?.let {
            if (it.isCameraOccupied && DeviceStore.shared().deviceState.cameraStatus.value == DeviceStatus.ON) {
                DeviceStore.shared().closeLocalCamera()
                postDelayed({
                    DeviceStore.shared().openLocalCamera(DeviceStore.shared().deviceState.isFrontCamera.value, null)
                }, 500)
            }
        }
        anchorManager?.getMediaManager()?.resetCameraOccupied()
    }

    override fun onRoomDismissed() {
        ToastUtil.toastShortMessage(baseContext.getString(R.string.common_live_has_stop))
        endLive()
    }

    override fun onKickedOffLine(message: String) {
        ToastUtil.toastShortMessage(baseContext.getString(R.string.common_kicked_out_of_room_by_owner))
        endLive()
    }

    override fun onKickedOutOfRoom(
        roomId: String, reason: TUIRoomDefine.KickedOutOfRoomReason, message: String
    ) {
        ToastUtil.toastShortMessage(baseContext.getString(R.string.common_kicked_out_of_room_by_owner))
        endLive()
    }

    private fun endLive(isFinish: Boolean = true) {
        PictureInPictureStore.sharedInstance().state.isAnchorStreaming = false
        liveCoreView.setLocalVideoMuteImage(null, null)
        LiveListStore.shared().endLive(null)
        TUICore.notifyEvent(
            TUIConstants.Privacy.EVENT_ROOM_STATE_CHANGED, TUIConstants.Privacy.EVENT_SUB_KEY_ROOM_STATE_STOP, null
        )
        TUICore.notifyEvent(EVENT_KEY_TIME_LIMIT, EVENT_SUB_KEY_COUNTDOWN_END, null)
        if (isFinish) {
            finishActivity()
        }
    }
}