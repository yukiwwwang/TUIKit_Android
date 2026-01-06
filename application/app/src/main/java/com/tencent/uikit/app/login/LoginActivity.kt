package com.tencent.uikit.app.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.interfaces.TUIServiceCallback
import com.tencent.qcloud.tuicore.util.SPUtils
import com.tencent.qcloud.tuikit.debug.GenerateTestUserSig
import com.tencent.qcloud.tuikit.tuicallkit.TUICallKit.Companion.createInstance
import com.tencent.uikit.app.R
import com.tencent.uikit.app.main.BaseActivity
import com.tencent.uikit.app.main.MainActivity
import io.trtc.tuikit.atomicx.widget.basicwidget.toast.AtomicToast
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var editUserId: EditText
    private lateinit var scEnableTestEnv: SwitchCompat
    private lateinit var scDisableFeature: SwitchCompat
    private lateinit var scEnableHighPerformance: SwitchCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action.equals(Intent.ACTION_MAIN)
        ) {
            finish();
            return;
        }
        setContentView(R.layout.app_activity_login)
        initView()
    }

    private fun initView() {
        editUserId = findViewById(R.id.et_userId)
        scEnableTestEnv = findViewById(R.id.sc_enable_test_env)
        scDisableFeature = findViewById(R.id.sc_disable_feature)
        scEnableHighPerformance = findViewById<SwitchCompat>(R.id.sc_enable_high_performance)
        editUserId.setText(SPUtils.getInstance("app_uikit").getString("userId"))
        findViewById<View>(R.id.btn_login).setOnClickListener {
            val userId = editUserId.text.toString().trim()
            SPUtils.getInstance("app_uikit").put("userId", userId)
            login(userId)
        }
        scEnableTestEnv.setOnCheckedChangeListener { buttonView, isChecked ->
            switchTestEnv(isChecked)
        }
        scDisableFeature.setOnCheckedChangeListener { buttonView, isChecked ->
            val params = mapOf(
                "isEnableHighPerformance" to scEnableHighPerformance.isChecked,
                "isEnableHighPerformanceFeature" to isChecked,
            )
            TUICore.callService("TEBeautyExtension", "enableHighPerformance", params, object : TUIServiceCallback() {
                override fun onServiceCallback(errorCode: Int, errorMessage: String?, bundle: Bundle?) {
                }
            })

        }

        scEnableHighPerformance.setOnCheckedChangeListener { buttonView, isChecked ->
            val params = mapOf(
                "isEnableHighPerformance" to isChecked,
                "isEnableHighPerformanceFeature" to scDisableFeature.isChecked,
            )
            TUICore.callService("TEBeautyExtension", "enableHighPerformance", params, object : TUIServiceCallback() {
                override fun onServiceCallback(errorCode: Int, errorMessage: String?, bundle: Bundle?) {
                }
            })
        }
    }

    private fun login(userId: String) {
        if (userId.isEmpty()) {
            AtomicToast.show(
                this,
                getString(R.string.app_user_id_is_empty),
                AtomicToast.Style.ERROR
            )
            return
        }
        val userSig = GenerateTestUserSig.genTestUserSig(userId)
        LoginStore.shared.login(this, GenerateTestUserSig.SDKAPPID, userId, userSig, object : CompletionHandler {
            override fun onSuccess() {
                Log.i(TAG, "login onSuccess")
                val instance = createInstance(application)
                instance.enableFloatWindow(true)
                instance.enableVirtualBackground(true)
                instance.enableIncomingBanner(true)
                getUserInfo()
            }

            override fun onFailure(code: Int, desc: String) {
                AtomicToast.show(
                    this@LoginActivity,
                    getString(R.string.app_toast_login_fail, code, desc),
                    AtomicToast.Style.ERROR
                )
                Log.e(TAG, "login fail errorCode: $code errorMessage:$desc")
            }
        })
        TUILogin.login(this, GenerateTestUserSig.SDKAPPID, userId, userSig, null)
    }

    private fun getUserInfo() {
        lifecycleScope.launch {
            LoginStore.shared.loginState.loginUserInfo.collect { loginUserInfo ->
                loginUserInfo?.let {
                    if (it.userID.isEmpty()) {
                        return@collect
                    }
                    if (it.nickname.isNullOrEmpty() || it.avatarURL.isNullOrEmpty()) {
                        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun switchTestEnv(enableTestEnv: Boolean) {
        V2TIMManager.getInstance().callExperimentalAPI(
            "setTestEnvironment", enableTestEnv, object : V2TIMValueCallback<Any?> {
                override fun onSuccess(o: Any?) {
                    Log.i(TAG, "V2TIMManager setNetEnv success. enableTestEnv：$enableTestEnv")
                }

                override fun onError(code: Int, desc: String?) {
                    Log.i(TAG, "V2TIMManager setNetEnv fail. enableTestEnv：$enableTestEnv")
                }
            })
    }
}