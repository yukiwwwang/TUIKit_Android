package com.trtc.uikit.livekit.component.login

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import com.tencent.cloud.tuikit.engine.room.TUIRoomEngine
import com.tencent.qcloud.tuicore.TUIConfig
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.interfaces.ITUINotification
import com.trtc.tuikit.common.system.ContextProvider
import com.trtc.uikit.livekit.common.LiveKitLogger

class LiveKitInitializer : ContentProvider() {

    companion object {
        const val EVENT_ENGINE_LOGIN_STATE_CHANGED = "eventEngineLoginStateChanged"
        const val EVENT_SUB_KEY_ENGINE_LOGIN_SUCCESS = "eventSubKeyEngineLoginSuccess"
    }

    private val logger = LiveKitLogger.getComponentLogger("LiveKitInitializer")
    private val notification = ITUINotification { key, subKey, param ->
        if (TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED == key &&
            TUIConstants.TUILogin.EVENT_SUB_KEY_USER_LOGIN_SUCCESS == subKey &&
            TUILogin.isUserLogined()
        ) {

            TUIRoomEngine.login(
                TUIConfig.getAppContext(),
                TUILogin.getSdkAppId(),
                TUILogin.getUserId(),
                TUILogin.getUserSig(),
                object : TUIRoomDefine.ActionCallback {
                    override fun onSuccess() {
                        logger.info("serviceInitializer login:[Success]")
                        notifyEngineLoginSuccess()
                    }

                    override fun onError(error: TUICommonDefine.Error, message: String) {
                        logger.error("serviceInitializer login:[Error:$error,message:$message]")
                    }
                }
            )
        }
    }

    override fun onCreate(): Boolean {
        loginRoomEngine()
        registerEvent()
        return false
    }

    private fun loginRoomEngine() {
        if (TUILogin.isUserLogined()) {
            TUIRoomEngine.login(
                TUILogin.getAppContext(),
                TUILogin.getSdkAppId(),
                TUILogin.getUserId(),
                TUILogin.getUserSig(),
                object : TUIRoomDefine.ActionCallback {
                    override fun onSuccess() {
                        logger.info("RoomEngine login:[Success]")
                        notifyEngineLoginSuccess()
                    }

                    override fun onError(error: TUICommonDefine.Error, message: String) {
                        logger.error("RoomEngine login : [onError:[error:$error,message:$message]]")
                    }
                }
            )
        }
    }

    private fun notifyEngineLoginSuccess() {
        TUICore.notifyEvent(EVENT_ENGINE_LOGIN_STATE_CHANGED, EVENT_SUB_KEY_ENGINE_LOGIN_SUCCESS, null)
    }

    private fun registerEvent() {
        TUICore.registerEvent(
            TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED,
            TUIConstants.TUILogin.EVENT_SUB_KEY_USER_LOGIN_SUCCESS,
            notification
        )
        context?.let {
            ContextProvider.setApplicationContext(it.applicationContext)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0
}