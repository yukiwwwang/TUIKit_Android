package com.trtc.uikit.roomkit.base.operator

import android.Manifest
import android.content.Context
import com.trtc.tuikit.common.permission.PermissionCallback
import com.trtc.tuikit.common.permission.PermissionRequester
import com.trtc.uikit.roomkit.base.error.ErrorLocalized
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.device.DeviceStatus
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.room.RoomParticipantStore
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Device operator for managing camera and microphone operations with permission handling.
 */
class DeviceOperator(context: Context) {

    private val contextRef = WeakReference(context)

    private val deviceStore = DeviceStore.shared()

    suspend fun unmuteMicrophone(participantStore: RoomParticipantStore?) {
        val hasPermission = requestPermission(Manifest.permission.RECORD_AUDIO)
        if (!hasPermission) return

        if (deviceStore.deviceState.microphoneStatus.value == DeviceStatus.OFF) {
            awaitCompletion { deviceStore.openLocalMicrophone(it) }
        }

        participantStore?.let { store ->
            awaitCompletion { store.unmuteMicrophone(it) }
        }
    }

    fun muteMicrophone(participantStore: RoomParticipantStore?) {
        participantStore?.muteMicrophone()
    }

    suspend fun openCamera() {
        val hasPermission = requestPermission(Manifest.permission.CAMERA)
        if (!hasPermission) return

        awaitCompletion {
            val isFrontCamera = deviceStore.deviceState.isFrontCamera.value
            deviceStore.openLocalCamera(isFrontCamera, it)
        }
    }

    fun closeCamera() {
        deviceStore.closeLocalCamera()
    }

    suspend fun requestPermission(permission: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            var isCompleted = false
            PermissionRequester.newInstance(permission)
                .callback(object : PermissionCallback() {
                    override fun onGranted() {
                        if (!isCompleted) {
                            isCompleted = true
                            continuation.resume(true)
                        }
                    }

                    override fun onDenied() {
                        if (!isCompleted) {
                            isCompleted = true
                            continuation.resume(false)
                        }
                    }
                })
                .request()
            
            continuation.invokeOnCancellation {
                isCompleted = true
            }
        }
    }

    private suspend fun awaitCompletion(block: (CompletionHandler) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            var isCompleted = false
            block(object : CompletionHandler {
                override fun onSuccess() {
                    if (!isCompleted) {
                        isCompleted = true
                        continuation.resume(Unit)
                    }
                }

                override fun onFailure(code: Int, desc: String) {
                    if (!isCompleted) {
                        isCompleted = true
                        contextRef.get()?.let { ErrorLocalized.showError(it, code) }
                        continuation.resumeWithException(Exception(desc))
                    }
                }
            })
            
            continuation.invokeOnCancellation {
                isCompleted = true
            }
        }
    }
}
