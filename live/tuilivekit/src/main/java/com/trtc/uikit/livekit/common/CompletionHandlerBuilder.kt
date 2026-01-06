package com.trtc.uikit.livekit.common

import io.trtc.tuikit.atomicxcore.api.CompletionHandler

class CompletionHandlerBuilder {
    private var _onSuccess: () -> Unit = {}
    private var _onError: (Int, String) -> Unit = { _, _ -> }

    fun onSuccess(block: () -> Unit) {
        _onSuccess = block
    }

    fun onError(block: (Int, String) -> Unit) {
        _onError = block
    }

    fun build(): CompletionHandler {
        return object : CompletionHandler {
            override fun onSuccess() = _onSuccess()
            override fun onFailure(code: Int, desc: String) = _onError(code, desc)
        }
    }
}

fun completionHandler(block: CompletionHandlerBuilder.() -> Unit): CompletionHandler {
    return CompletionHandlerBuilder().apply(block).build()
}