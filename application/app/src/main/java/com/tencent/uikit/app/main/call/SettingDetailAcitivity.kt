package com.tencent.uikit.app.main.call

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuikit.tuicallkit.TUICallKit.Companion.createInstance
import com.tencent.uikit.app.R
import com.tencent.uikit.app.main.BaseActivity
import io.trtc.tuikit.atomicx.widget.basicwidget.toast.AtomicToast
import io.trtc.tuikit.atomicxcore.api.CompletionHandler

class SettingDetailActivity : BaseActivity() {
    private var editContent: EditText? = null
    private var itemType = ITEM_USER_DATA

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_settings_detail)
        initBundleData()
        initView()
    }

    private fun initBundleData() {
        itemType = getIntent().getStringExtra(ITEM_KEY).toString()
    }

    private fun initView() {
        editContent = findViewById(R.id.et_content)
        when (itemType) {
            ITEM_USER_DATA -> {
                editContent?.setText(SettingsConfig.userData)
                editContent?.setHint(getString(R.string.app_invite_cmd_extra_info))
            }

            ITEM_OFFLINE_MESSAGE -> {
                editContent?.setText(SettingsConfig.offlineParams)
                editContent?.setHint(getString(R.string.app_offline_message_json_string))
            }

            ITEM_AVATAR -> {
                editContent?.setText(TUILogin.getFaceUrl())
                editContent?.setHint(getString(R.string.app_avatar))
            }

            ITEM_RING_PATH -> {
                editContent?.setText(SettingsConfig.ringPath)
                editContent?.setHint(getString(R.string.app_set_ring_path))
            }

            else -> {}
        }
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_confirm).setOnClickListener { clickConfirm() }
        editContent?.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (EditorInfo.IME_ACTION_DONE == actionId) {
                clickConfirm()
            }
            true
        }
    }

    private fun clickConfirm() {
        when (itemType) {
            ITEM_USER_DATA -> SettingsConfig.userData = editContent?.getText().toString().trim { it <= ' ' }

            ITEM_OFFLINE_MESSAGE -> SettingsConfig.offlineParams = editContent?.getText().toString().trim { it <= ' ' }

            ITEM_AVATAR -> {
                val avatar = editContent?.getText().toString().trim { it <= ' ' }
                setUserAvatar(avatar)
            }

            ITEM_RING_PATH -> {
                val ringPath = editContent?.getText().toString()
                SettingsConfig.ringPath = ringPath
                createInstance(getApplicationContext()).setCallingBell(ringPath)
            }

            else -> {}
        }
        AtomicToast.show(this, getString(R.string.app_set_success), AtomicToast.Style.SUCCESS)
        finish()
    }

    private fun setUserAvatar(avatar: String) {
        createInstance(getApplicationContext()).setSelfInfo(
            TUILogin.getNickName(), avatar,
            object : CompletionHandler {
                override fun onSuccess() {
                    AtomicToast.show(this@SettingDetailActivity, getString(R.string.app_set_success), AtomicToast.Style.SUCCESS)
                    finish()
                }

                override fun onFailure(code: Int, desc: String) {
                    AtomicToast.show(this@SettingDetailActivity, getString(R.string.app_set_fail), AtomicToast.Style.ERROR)
                }
            })
    }

    companion object {
        const val ITEM_KEY: String = "settingsItem"
        const val ITEM_AVATAR: String = "avatar"
        const val ITEM_RING_PATH: String = "ringPath"
        const val ITEM_USER_DATA: String = "userData"
        const val ITEM_OFFLINE_MESSAGE: String = "offlineMessage"
    }
}