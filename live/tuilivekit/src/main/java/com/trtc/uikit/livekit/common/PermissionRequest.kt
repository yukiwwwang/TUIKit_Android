package com.trtc.uikit.livekit.common

import android.Manifest
import android.content.Context
import com.trtc.tuikit.common.permission.PermissionCallback
import com.trtc.tuikit.common.permission.PermissionRequester
import com.trtc.uikit.livekit.R

object PermissionRequest {
    fun requestMicrophonePermissions(context: Context, callback: PermissionCallback?) {
        val title = context.getString(R.string.common_permission_microphone)
        val reason = context.getString(R.string.common_permission_mic_reason)

        val permissionCallback = object : PermissionCallback() {
            override fun onGranted() {
                callback?.onGranted()
            }

            override fun onDenied() {
                super.onDenied()
                callback?.onDenied()
            }
        }

        val applicationInfo = context.applicationInfo
        val appName = context.packageManager.getApplicationLabel(applicationInfo).toString()

        PermissionRequester.newInstance(Manifest.permission.RECORD_AUDIO)
            .title(context.getString(R.string.common_permission_title, appName, title))
            .description(reason)
            .settingsTip(context.getString(R.string.common_permission_tips, title) + "\n" + reason)
            .callback(permissionCallback)
            .request()
    }

    fun requestCameraPermissions(context: Context, callback: PermissionCallback?) {
        val title = context.getString(R.string.common_permission_camera)
        val reason = context.getString(R.string.common_permission_camera_reason)

        val permissionCallback = object : PermissionCallback() {
            override fun onGranted() {
                callback?.onGranted()
            }

            override fun onDenied() {
                super.onDenied()
                callback?.onDenied()
            }
        }

        val applicationInfo = context.applicationInfo
        val appName = context.packageManager.getApplicationLabel(applicationInfo).toString()

        PermissionRequester.newInstance(Manifest.permission.CAMERA)
            .title(context.getString(R.string.common_permission_title, appName, title))
            .description(reason)
            .settingsTip(context.getString(R.string.common_permission_tips, title) + "\n" + reason)
            .callback(permissionCallback)
            .request()
    }
}