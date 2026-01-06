package com.trtc.uikit.livekit.voiceroom.interaction.battle

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.trtc.uikit.livekit.R
import io.trtc.tuikit.atomicxcore.api.live.BattleInfo
import io.trtc.tuikit.atomicxcore.api.live.BattleStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

class BattleInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private enum class BattleResultType {
        DRAW, VICTORY, DEFEAT
    }

    fun interface OnBattleEndListener {
        fun onBattleEnd(view: BattleInfoView)
    }

    private var battleStore: BattleStore? = null
    private var onBattleEndListener: OnBattleEndListener? = null

    private lateinit var singleBattleScoreView: SingleBattleScoreView
    private lateinit var timeLabel: TextView
    private lateinit var battleStartView: ImageView
    private lateinit var battleResultView: ImageView

    private var countDownTimer: CountDownTimer? = null
    private var currentBattleId: String? = null

    private var viewScope: CoroutineScope? = null

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        inflate(context, R.layout.livekit_voiceroom_battle_info_view, this)
        singleBattleScoreView = findViewById(R.id.single_battle_score_view)
        timeLabel = findViewById(R.id.tv_battle_time)
        battleStartView = findViewById(R.id.iv_battle_start)
        battleResultView = findViewById(R.id.iv_battle_result)
        visibility = GONE
    }

    fun init(liveId: String) {
        battleStore = BattleStore.create(liveId)
    }

    private fun addObservers() {
        val scope = viewScope ?: return
        battleStore ?: return

        battleStore?.battleState?.currentBattleInfo
            ?.onEach { battleInfo -> handleBattleInfoChanged(battleInfo) }
            ?.launchIn(scope)
    }

    private fun removeObservers() {
        viewScope?.cancel()
        viewScope = null
    }

    private fun handleBattleInfoChanged(newBattleInfo: BattleInfo?) {
        val newBattleId = newBattleInfo?.battleID
        if (currentBattleId != newBattleId) {
            if (!newBattleId.isNullOrEmpty()) {
                onBattleStart(newBattleInfo)
            } else {
                if (!currentBattleId.isNullOrEmpty()) {
                    onBattleEnd()
                }
            }
        }
        currentBattleId = newBattleId
    }

    fun setBattleEndListener(listener: OnBattleEndListener) {
        onBattleEndListener = listener
    }

    private fun onBattleStart(battleInfo: BattleInfo) {
        post {
            visibility = VISIBLE
            battleResultView.visibility = GONE
            battleStartView.visibility = VISIBLE
            postDelayed({ battleStartView.visibility = GONE }, 2000)
            singleBattleScoreView.post {
                singleBattleScoreView.updateScores(0, 0)
            }
            startTimer(battleInfo)
        }
    }

    private fun onBattleEnd() {
        post {
            cleanupTimer()
            showBattleResult(BattleResultType.DRAW)
            postDelayed({
                visibility = GONE
                onBattleEndListener?.onBattleEnd(this)
            }, 2000)
        }
    }

    private fun startTimer(battleInfo: BattleInfo) {
        cleanupTimer()

        val startTimeMillis = TimeUnit.SECONDS.toMillis(battleInfo.startTime)
        val durationMillis = TimeUnit.SECONDS.toMillis(battleInfo.config.duration.toLong())
        val elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis

        if (elapsedTimeMillis >= durationMillis) {
            updateTimeText(0)
            return
        }

        val remainingTimeMillis = durationMillis - elapsedTimeMillis
        updateTimeText(TimeUnit.MILLISECONDS.toSeconds(remainingTimeMillis))

        countDownTimer = object : CountDownTimer(remainingTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimeText(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))
            }

            override fun onFinish() {
                updateTimeText(0)
            }
        }.start()
    }

    private fun updateTimeText(seconds: Long) {
        if (seconds <= 0) {
            timeLabel.text = context.getString(R.string.common_battle_pk_end)
        } else {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            timeLabel.text = "${context.getString(R.string.seat_in_pk)} ${
                "%d:%02d".format(
                    minutes,
                    remainingSeconds
                )
            }"
        }
    }

    private fun showBattleResult(type: BattleResultType) {
        battleResultView.visibility = VISIBLE
        val resId = when (type) {
            BattleResultType.VICTORY -> R.drawable.livekit_battle_result_victory
            BattleResultType.DEFEAT -> R.drawable.livekit_battle_result_defeat
            BattleResultType.DRAW -> R.drawable.livekit_battle_result_draw
        }
        battleResultView.setImageResource(resId)
    }

    private fun cleanupTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewScope = CoroutineScope(Dispatchers.Main)
        addObservers()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObservers()
        cleanupTimer()
        removeCallbacks(null)
    }
}