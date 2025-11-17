package com.trtc.uikit.livekit.common.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import java.util.Objects

class StandardDialog(context: Context) : Dialog(context) {
    private var avatarUrl: String? = null
    private var content: String? = null
    private var positiveText: String? = null
    private var negativeText: String? = null
    private var positiveTextColor: Int
    private var negativeTextColor: Int
    private var positiveClickListener: View.OnClickListener? = null
    private var negativeClickListener: View.OnClickListener? = null
    private var options: MutableList<Option> = ArrayList()
    private var useMultiOptionMode = false
    private var isDarkTheme = false
    private var negativeButtonCountdownTimer: CountDownTimer? = null
    private var isCountdownEnabled = false
    private var negativeCountdownBaseText: String? = null
    private var countdownDurationMillis: Long = 0

    data class Option(
        val text: String,
        @ColorInt val textColor: Int,
        val listener: View.OnClickListener?
    )

    init {
        Objects.requireNonNull(window)?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        positiveTextColor = context.resources.getColor(R.color.common_color_white_e5)
        negativeTextColor = context.resources.getColor(R.color.common_text_color_secondary)
    }

    fun setContent(content: String?): StandardDialog {
        this.content = content
        return this
    }

    fun setAvatar(avatarUrl: String?): StandardDialog {
        this.avatarUrl = avatarUrl
        return this
    }

    fun setPositiveText(positiveText: String?, listener: View.OnClickListener): StandardDialog {
        this.positiveText = positiveText
        this.positiveClickListener = listener
        return this
    }

    fun setNegativeText(negativeText: String?, listener: View.OnClickListener): StandardDialog {
        this.negativeText = negativeText
        this.negativeClickListener = listener
        this.isCountdownEnabled = false
        return this
    }

    fun setPositiveText(positiveText: String?, @ColorInt color: Int, listener: View.OnClickListener): StandardDialog {
        this.positiveText = positiveText
        this.positiveClickListener = listener
        this.positiveTextColor = color
        return this
    }

    fun setNegativeText(negativeText: String?, @ColorInt color: Int, listener: View.OnClickListener): StandardDialog {
        this.negativeText = negativeText
        this.negativeClickListener = listener
        this.negativeTextColor = color
        this.isCountdownEnabled = false
        return this
    }

    fun setPositiveTextColor(@ColorInt color: Int): StandardDialog {
        this.positiveTextColor = color
        return this
    }

    fun setNegativeTextColor(@ColorInt color: Int): StandardDialog {
        this.negativeTextColor = color
        return this
    }

    fun setNegativeTextWithCountdown(baseText: String?, durationMillis: Long, listener: View.OnClickListener): StandardDialog {
        this.isCountdownEnabled = true
        this.negativeCountdownBaseText = baseText
        this.countdownDurationMillis = durationMillis
        setNegativeText(baseText, listener)
        this.isCountdownEnabled = true
        return this
    }

    fun setOptions(options: List<Option>?): StandardDialog {
        this.options.clear()
        if (options != null) {
            this.options.addAll(options)
        }
        useMultiOptionMode = this.options.isNotEmpty()
        if (useMultiOptionMode) {
            setDarkTheme()
        }
        return this
    }

    fun setDarkTheme(): StandardDialog {
        isDarkTheme = true
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livekit_standard_dialog)
        setCancelable(false)
    }

    override fun onStart() {
        super.onStart()
        setupViews()
    }

    override fun onStop() {
        super.onStop()
        cancelCountdown()
    }

    private fun cancelCountdown() {
        negativeButtonCountdownTimer?.cancel()
        negativeButtonCountdownTimer = null
    }

    private fun setupViews() {
        applyTheme()
        initCommonViews()

        val classicButtonsContainer = findViewById<LinearLayout>(R.id.ll_classic_buttons)
        val optionsContainer = findViewById<LinearLayout>(R.id.ll_options_container)

        if (useMultiOptionMode) {
            classicButtonsContainer.visibility = View.GONE
            optionsContainer.visibility = View.VISIBLE
            initOptionsList(optionsContainer)
        } else {
            optionsContainer.visibility = View.GONE
            classicButtonsContainer.visibility = View.VISIBLE
            initClassicButtons()
        }
    }

    private fun applyTheme() {
        if (isDarkTheme) {
            findViewById<ConstraintLayout>(R.id.dialog_root_layout).setBackgroundResource(R.drawable.livekit_standard_dialog_bg)
            findViewById<TextView>(R.id.tv_content).setTextColor(context.resources.getColor(R.color.common_color_white_e5))
            findViewById<View>(R.id.horizontal_divider).setBackgroundColor(context.resources.getColor(R.color.common_not_standard_blue_30_transparency))
        }
    }

    private fun initCommonViews() {
        findViewById<TextView>(R.id.tv_content).apply {
            visibility = if (TextUtils.isEmpty(content)) View.GONE else View.VISIBLE
            text = content
        }
        findViewById<ImageView>(R.id.iv_picture).apply {
            visibility = if (TextUtils.isEmpty(avatarUrl)) View.GONE else View.VISIBLE
            avatarUrl?.let { ImageLoader.load(context, this, it, R.drawable.livekit_ic_avatar) }
        }
    }

    private fun initClassicButtons() {
        val buttonPositive = findViewById<Button>(R.id.btn_positive)
        if (positiveClickListener != null) {
            buttonPositive.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(positiveText)) {
                buttonPositive.text = positiveText
            }
            buttonPositive.setTextColor(positiveTextColor)
            buttonPositive.setOnClickListener { v ->
                cancelCountdown()
                positiveClickListener?.onClick(v)
                dismiss()
            }
        } else {
            buttonPositive.visibility = View.GONE
        }

        val buttonNegative = findViewById<Button>(R.id.btn_negative)
        buttonNegative.setTextColor(negativeTextColor)
        buttonNegative.typeface = Typeface.DEFAULT
        if (negativeClickListener != null) {
            buttonNegative.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(negativeText)) {
                buttonNegative.text = negativeText
            }
            buttonNegative.setOnClickListener { v ->
                cancelCountdown()
                negativeClickListener?.onClick(v)
                dismiss()
            }
            cancelCountdown()
            if (isCountdownEnabled) {
                negativeButtonCountdownTimer = object : CountDownTimer(countdownDurationMillis, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsLeft = Math.ceil(millisUntilFinished / 1000.0).toLong()
                        buttonNegative.text = "$negativeCountdownBaseText($secondsLeft)"
                    }

                    override fun onFinish() {
                        buttonNegative.text = negativeCountdownBaseText
                        dismiss()
                    }
                }.start()
            }
        } else {
            buttonNegative.visibility = View.GONE
        }
        findViewById<View>(R.id.vertical_divider).visibility =
            if (buttonPositive.visibility == View.GONE || buttonNegative.visibility == View.GONE) View.GONE else View.VISIBLE
    }

    private fun initOptionsList(container: LinearLayout) {
        container.removeAllViews()
        options.forEachIndexed { index, option ->
            val optionView = TextView(context).apply {
                text = option.text
                setTextColor(option.textColor)
                textSize = 16f
                gravity = Gravity.CENTER
                minHeight = dp2px(50f)
                setPadding(dp2px(16f), 0, dp2px(16f), 0)
                setOnClickListener { v ->
                    option.listener?.onClick(v)
                    dismiss()
                }
            }
            container.addView(optionView, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
            if (index < options.size - 1) {
                val divider = View(context).apply {
                    setBackgroundColor(context.resources.getColor(R.color.common_not_standard_blue_30_transparency))
                }
                val dividerParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp2px(0.5f)
                )
                container.addView(divider, dividerParams)
            }
        }
    }

    private fun dp2px(dp: Float): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }
}