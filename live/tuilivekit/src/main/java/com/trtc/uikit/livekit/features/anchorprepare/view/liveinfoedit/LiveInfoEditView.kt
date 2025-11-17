package com.trtc.uikit.livekit.features.anchorprepare.view.liveinfoedit

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.features.anchorprepare.LiveStreamPrivacyStatus
import com.trtc.uikit.livekit.features.anchorprepare.manager.AnchorPrepareManager
import com.trtc.uikit.livekit.features.anchorprepare.state.AnchorPrepareState
import com.trtc.uikit.livekit.features.anchorprepare.state.AnchorPrepareState.Companion.COVER_URL_LIST
import com.trtc.uikit.livekit.features.anchorprepare.state.AnchorPrepareState.Companion.MAX_INPUT_BYTE_LENGTH
import com.trtc.uikit.livekit.features.anchorprepare.view.liveinfoedit.livecoverpicker.LiveCoverPicker
import java.nio.charset.Charset

class LiveInfoEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var manager: AnchorPrepareManager? = null
    private var state: AnchorPrepareState? = null
    private lateinit var editRoomName: EditText
    private lateinit var textStreamPrivacyStatus: TextView
    private lateinit var imageStreamCover: ImageView
    private var liveCoverPicker: LiveCoverPicker? = null
    
    private val liveCoverObserver = Observer<String> { onLiveCoverChange(it) }
    private val livePrivacyStatusObserver = Observer<LiveStreamPrivacyStatus> { onLivePrivacyStatusChange(it) }

    init {
        LayoutInflater.from(context).inflate(R.layout.anchor_prepare_layout_live_info_edit_view, this, true)
    }

    fun init(manager: AnchorPrepareManager) {
        initManager(manager)
        initView()
    }

    private fun initManager(manager: AnchorPrepareManager) {
        this.manager = manager
        this.state = manager.getState()
    }

    private fun initView() {
        bindViewId()
        initLiveNameEditText()
        initLiveCoverPicker()
        initLivePrivacyStatusPicker()
    }

    @Synchronized
    private fun addObserver() {
        state?.let { s ->
            s.coverURL.observeForever(liveCoverObserver)
            s.liveMode.observeForever(livePrivacyStatusObserver)
        }
    }

    @Synchronized
    fun removeObserver() {
        state?.let { s ->
            s.coverURL.removeObserver(liveCoverObserver)
            s.liveMode.removeObserver(livePrivacyStatusObserver)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeObserver()
    }

    private fun bindViewId() {
        imageStreamCover = findViewById(R.id.iv_cover)
        editRoomName = findViewById(R.id.et_stream_name)
        textStreamPrivacyStatus = findViewById(R.id.tv_stream_privacy_status)
    }

    private fun initLiveCoverPicker() {
        val coverSettingsLayout = findViewById<View>(R.id.fl_cover_edit)
        ImageLoader.load(
            context, 
            imageStreamCover, 
            state?.coverURL?.value,
            R.drawable.anchor_prepare_live_stream_default_cover
        )
        
        coverSettingsLayout.setOnClickListener {
            if (liveCoverPicker == null) {
                val config = LiveCoverPicker.Config().apply {
                    title = context.getString(R.string.common_cover)
                    confirmButtonText = context.getString(R.string.common_set_as_cover)
                    data = COVER_URL_LIST.toList()
                    currentImageUrl = state?.coverURL?.value ?: ""
                }
                liveCoverPicker = LiveCoverPicker(context, config)
                liveCoverPicker?.setOnItemClickListener { imageUrl ->
                    manager?.setCoverURL(imageUrl)
                }
            }
            liveCoverPicker?.show()
        }
    }

    private fun initLiveNameEditText() {
        val roomName = manager?.getDefaultRoomName() ?: ""
        editRoomName.setText(roomName)
        manager?.setRoomName(roomName)
        
        editRoomName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                if (TextUtils.isEmpty(editable)) {
                    return
                }
                
                var newString = editable.toString()
                if (!checkLength(editable.toString())) {
                    for (i in editable!!.length downTo 1) {
                        val s = editable.subSequence(0, i).toString()
                        if (checkLength(s)) {
                            newString = s
                            editRoomName.setText(s)
                            editRoomName.setSelection(s.length)
                            break
                        }
                    }
                }
                manager?.setRoomName(newString)
            }

            private fun checkLength(s: String): Boolean {
                return s.toByteArray(Charset.defaultCharset()).size <= MAX_INPUT_BYTE_LENGTH
            }
        })
    }

    private fun initLivePrivacyStatusPicker() {
        findViewById<View>(R.id.ll_stream_privacy_status).setOnClickListener {
            manager?.let { mgr ->
                val picker = LivePrivacyStatusPicker(context, mgr)
                picker.show()
            }
        }
    }

    private fun onLiveCoverChange(coverURL: String?) {
        ImageLoader.load(
            context, 
            imageStreamCover, 
            coverURL,
            R.drawable.anchor_prepare_live_stream_default_cover
        )
    }

    private fun onLivePrivacyStatusChange(status: LiveStreamPrivacyStatus?) {
        status?.let {
            textStreamPrivacyStatus.setText(it.resId)
        }
    }
}