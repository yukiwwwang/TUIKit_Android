package com.trtc.uikit.livekit.features.anchorboardcast.view.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.common.ui.PopupDialog
import com.trtc.uikit.livekit.component.audioeffect.AudioEffectPanel
import com.trtc.uikit.livekit.component.beauty.BeautyUtils
import com.trtc.uikit.livekit.component.dashboard.StreamDashboardDialog
import com.trtc.uikit.livekit.component.videoquality.LocalMirrorSelectPanel
import com.trtc.uikit.livekit.features.anchorboardcast.manager.AnchorManager
import com.trtc.uikit.livekit.features.anchorboardcast.state.AnchorConfig
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.device.MirrorType
import io.trtc.tuikit.atomicxcore.api.view.LiveCoreView

class SettingsListAdapter(
    private val context: Context,
    private val liveStreamManager: AnchorManager,
    private val liveCoreView: LiveCoreView,
    private val settingsDialog: SettingsPanelDialog
) : RecyclerView.Adapter<SettingsListAdapter.ViewHolder>() {

    companion object {
        private const val ITEM_TYPE_BEAUTY = 0
        private const val ITEM_TYPE_AUDIO_EFFECT = 1
        private const val ITEM_TYPE_FLIP = 2
        private const val ITEM_TYPE_MIRROR = 3
        private const val ITEM_TYPE_DASHBOARD = 4
    }

    private val data = mutableListOf<SettingsItem>()
    private var audioEffectDialog: PopupDialog? = null

    init {
        initData()
    }

    private fun initData() {
        data.add(
            SettingsItem(
                context.getString(R.string.common_video_settings_item_beauty),
                R.drawable.livekit_settings_item_beauty,
                ITEM_TYPE_BEAUTY
            )
        )

        if (!AnchorConfig.disableFooterSoundEffect.value) {
            data.add(
                SettingsItem(
                    context.getString(R.string.common_audio_effect),
                    R.drawable.livekit_settings_audio_effect,
                    ITEM_TYPE_AUDIO_EFFECT
                )
            )
        }

        data.add(
            SettingsItem(
                context.getString(R.string.common_video_settings_item_flip),
                R.drawable.livekit_settings_item_flip,
                ITEM_TYPE_FLIP
            )
        )

        data.add(
            SettingsItem(
                context.getString(R.string.common_video_settings_item_mirror),
                R.drawable.livekit_settings_item_mirror,
                ITEM_TYPE_MIRROR
            )
        )

        data.add(
            SettingsItem(
                context.getString(R.string.common_dashboard_title),
                R.drawable.livekit_settings_dashboard,
                ITEM_TYPE_DASHBOARD
            )
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.livekit_anchor_settings_panel_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.textTitle.text = item.title
        holder.imageIcon.setImageResource(item.icon)
        holder.layoutRoot.tag = item.type

        holder.layoutRoot.setOnClickListener { view ->
            val type = view.tag as Int
            when (type) {
                ITEM_TYPE_BEAUTY -> showBeautyPanel()
                ITEM_TYPE_AUDIO_EFFECT -> showAudioEffectPanel()
                ITEM_TYPE_FLIP -> handleCameraFlip()
                ITEM_TYPE_MIRROR -> switchMirror()
                ITEM_TYPE_DASHBOARD -> showMediaDashboardDialog()
            }
        }
    }

    private fun handleCameraFlip() {
        val isFrontCamera = DeviceStore.shared().deviceState.isFrontCamera.value
        DeviceStore.shared().switchCamera(!isFrontCamera)
    }

    private fun switchMirror() {
        val videoQualityList = listOf(MirrorType.AUTO, MirrorType.ENABLE, MirrorType.DISABLE)
        val mirrorTypePanel = LocalMirrorSelectPanel(context, videoQualityList)
        mirrorTypePanel.setOnMirrorTypeSelectedListener(object :
            LocalMirrorSelectPanel.OnMirrorTypeSelectedListener {
            override fun onVideoQualitySelected(mirrorType: MirrorType) {
                DeviceStore.shared().switchMirror(mirrorType)
            }
        })
        mirrorTypePanel.show()
    }

    private fun showMediaDashboardDialog() {
        settingsDialog.dismiss()
        val streamDashboardDialog = StreamDashboardDialog(context)
        streamDashboardDialog.show()
    }

    private fun showAudioEffectPanel() {
        settingsDialog.dismiss()
        if (audioEffectDialog == null) {
            audioEffectDialog = PopupDialog(context)
            val audioEffectPanel = AudioEffectPanel(context)
            audioEffectPanel.init(liveStreamManager.getState().roomId)
            audioEffectPanel.setOnBackButtonClickListener(object : AudioEffectPanel.OnBackButtonClickListener {
                override fun onClick() {
                    audioEffectDialog?.dismiss()
                }

            })
            audioEffectDialog?.setView(audioEffectPanel)
        }
        audioEffectDialog?.show()
    }

    private fun showBeautyPanel() {
        settingsDialog.dismiss()
        BeautyUtils.showBeautyDialog(context)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutRoot: LinearLayout = itemView.findViewById(R.id.ll_root)
        val textTitle: TextView = itemView.findViewById(R.id.tv_title)
        val imageIcon: ImageView = itemView.findViewById(R.id.iv_icon)
    }

    data class SettingsItem(
        val title: String,
        val icon: Int,
        val type: Int
    )
}