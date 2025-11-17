package com.trtc.uikit.livekit.features.audiencecontainer.view.battle.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.LiveKitLogger
import com.trtc.uikit.livekit.features.audiencecontainer.manager.AudienceManager
import com.trtc.uikit.livekit.features.audiencecontainer.view.BasicView
import io.trtc.tuikit.atomicxcore.api.live.BattleEndedReason
import io.trtc.tuikit.atomicxcore.api.live.BattleInfo
import io.trtc.tuikit.atomicxcore.api.live.BattleListener
import io.trtc.tuikit.atomicxcore.api.live.SeatUserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * mode: multi member Battle
 * for member
 */
@SuppressLint("ViewConstructor")
class BattleMemberInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BasicView(context, attrs) {
    private var userId: String? = null
    private lateinit var rankingView: ImageView
    private lateinit var scoreView: TextView
    private lateinit var connectionStatusView: TextView

    private val battleListener = object : BattleListener() {
        override fun onBattleEnded(battleInfo: BattleInfo, reason: BattleEndedReason?) {
            updateUserInfo()
        }
    }

    fun init(audienceManager: AudienceManager, userId: String) {
        this.userId = userId
        super.init(audienceManager)
    }

    override fun initView() {
        inflate(context, R.layout.livekit_battle_member_info_view, this)
        rankingView = findViewById(R.id.iv_ranking)
        scoreView = findViewById(R.id.tv_score)
        connectionStatusView = findViewById(R.id.tv_connection_status)
        reset()
    }

    override fun addObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                audienceManager.getBattleStore().battleState.battleUsers.collect {
                    logger.info("battleUsers changed: $it")
                    if (it.isNotEmpty()) {
                        updateUserInfo()
                    }
                }
            }
            launch {
                audienceManager.getBattleStore().battleState.battleScore.collect {
                    logger.info("battleScore changed: $it")
                    if (it.isNotEmpty()) {
                        updateUserInfo()
                    }
                }
            }
            launch {
                audienceManager.getViewState().isOnDisplayResult.collect {
                    onBattleResultDisplay(it)
                }
            }
            launch {
                mediaState.isPictureInPictureMode.collect {
                    onPictureInPictureObserver(it)
                }
            }
        }
        audienceManager.getBattleStore().addBattleListener(battleListener)
    }

    override fun removeObserver() {
        subscribeStateJob?.cancel()
        audienceManager.getBattleStore().removeBattleListener(battleListener)
    }

    private fun updateUserInfo() {
        if (audienceManager.getCoHostState().connected.value.size <= 1) {
            return
        }

        var userInfo: SeatUserInfo? = null
        for (user in audienceManager.getCoHostState().connected.value) {
            if (user.userID == userId) {
                userInfo = user
            }
        }
        val is1V1Battle = audienceManager.getCoHostState().connected.value.size <= 2
        val userScore = audienceManager.getBattleState().battleScore.value[userId] ?: 0
        val currentUserIsBattle =
            audienceManager.getBattleState().battleUsers.value.find({ it.userID == userId }) != null
        logger.info("updateUserInfo is1V1Battle: $is1V1Battle, userScore:$userScore, currentUserIsBattle:$currentUserIsBattle, userInfo: $userInfo")
        if (is1V1Battle || userInfo == null) {
            reset()
        } else {
            if (currentUserIsBattle) {
                showBattleView(true)
                scoreView.text = "$userScore"
                val ranking =
                    getRankingFromMap(
                        userInfo.userID,
                        audienceManager.getBattleState().battleScore.value
                    )
                if (ranking > 0 && ranking <= RANKING_IMAGE.size) {
                    rankingView.setImageResource(RANKING_IMAGE[ranking - 1])
                }
            } else {
                showBattleView(false)
            }
        }
    }

    private fun getRankingFromMap(userID: String, scoreMap: Map<String, Int>): Int {
        data class TmpUser(val userId: String, val score: Int)

        val list = scoreMap.map { TmpUser(it.key, it.value) }
            .sortedByDescending { it.score }

        val rankMap = mutableMapOf<String, Int>()
        for ((index, tmpUser) in list.withIndex()) {
            val rank = if (index > 0 && tmpUser.score == list[index - 1].score) {
                rankMap[list[index - 1].userId] ?: index
            } else {
                index + 1
            }
            rankMap[tmpUser.userId] = rank
        }

        return rankMap[userID] ?: 1
    }

    private fun reset() {
        visibility = GONE
        rankingView.visibility = GONE
        scoreView.visibility = GONE
        connectionStatusView.visibility = GONE
    }

    private fun showBattleView(show: Boolean) {
        visibility = if (mediaState.isPictureInPictureMode.value) GONE else VISIBLE
        rankingView.visibility = if (show) VISIBLE else GONE
        scoreView.visibility = if (show) VISIBLE else GONE
        connectionStatusView.visibility = if (show) GONE else VISIBLE
    }

    private fun onBattleResultDisplay(display: Boolean?) {
        if (false == display) {
            reset()
        }
    }

    private fun onPictureInPictureObserver(isPipMode: Boolean?) {
        if (true == isPipMode) {
            visibility = GONE
        } else {
            if (!audienceManager.getBattleState().currentBattleInfo.value?.battleID.isNullOrBlank()) {
                visibility = VISIBLE
            }
        }
    }

    companion object {
        private val logger = LiveKitLogger.getLiveStreamLogger("BattleMemberInfoView")
        private val RANKING_IMAGE = intArrayOf(
            R.drawable.livekit_battle_ranking_1,
            R.drawable.livekit_battle_ranking_2,
            R.drawable.livekit_battle_ranking_3,
            R.drawable.livekit_battle_ranking_4,
            R.drawable.livekit_battle_ranking_5,
            R.drawable.livekit_battle_ranking_6,
            R.drawable.livekit_battle_ranking_7,
            R.drawable.livekit_battle_ranking_8,
            R.drawable.livekit_battle_ranking_9
        )
    }
}
