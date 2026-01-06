package com.trtc.uikit.roomkit.view.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.trtc.tuikit.common.util.ToastUtil
import com.trtc.uikit.roomkit.R
import com.trtc.uikit.roomkit.base.extension.getDisplayName
import com.trtc.uikit.roomkit.base.ui.BaseView
import io.trtc.tuikit.atomicxcore.api.room.RoomInfo
import io.trtc.tuikit.atomicxcore.api.room.RoomStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Room information view displaying room details with copy-to-clipboard functionality.
 */
class RoomInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseView(context, attrs, defStyleAttr) {

    private val tvRoomName: TextView by lazy { findViewById(R.id.tv_room_name) }
    private val tvRoomOwner: TextView by lazy { findViewById(R.id.tv_room_owner) }
    private val tvRoomID: TextView by lazy { findViewById(R.id.tv_room_ID) }
    private val btnCopyRoomID: LinearLayout by lazy { findViewById(R.id.btn_copy_room_ID) }
    private val btnCopyInvitationLink: AppCompatButton by lazy { findViewById(R.id.btn_copy_invitation_link) }

    private var roomStore = RoomStore.shared()
    private var subscribeJob: Job? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.roomkit_view_room_info, this)
        setupListeners()
    }

    override fun initStore(roomID: String) {
        roomStore = RoomStore.shared()
    }

    override fun addObserver() {
        subscribeJob = CoroutineScope(Dispatchers.Main).launch {
            roomStore.state.currentRoom.collect { roomInfo ->
                roomInfo?.let {
                    updateRoomInfo(roomInfo)
                }
            }
        }
    }

    override fun removeObserver() {
        subscribeJob?.cancel()
    }

    private fun updateRoomInfo(roomInfo: RoomInfo) {
        tvRoomName.text = roomInfo.getDisplayName()
        tvRoomOwner.text = roomInfo.roomOwner.getDisplayName()
        tvRoomID.text = roomInfo.roomID
    }

    private fun setupListeners() {
        btnCopyRoomID.setOnClickListener {
            copyToClipboard(tvRoomID.text.toString())
            ToastUtil.toastShortMessage(context.getString(R.string.roomkit_toast_room_id_copied))
        }

        btnCopyInvitationLink.setOnClickListener {
            val invitationText = generateInvitationText()
            copyToClipboard(invitationText)
            ToastUtil.toastShortMessage(context.getString(R.string.roomkit_toast_room_info_copied))
        }
    }

    private fun generateInvitationText(): String {
        return """
            ${context.getString(R.string.roomkit_room_name)}: ${tvRoomName.text}
            ${context.getString(R.string.roomkit_room_id)}: ${tvRoomID.text}
        """.trimIndent()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Label", text)
        clipboard.setPrimaryClip(clip)
    }
}