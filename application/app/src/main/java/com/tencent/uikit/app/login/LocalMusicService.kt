package com.tencent.uikit.app.login

import io.trtc.tuikit.atomicx.karaoke.store.ActionCallback
import io.trtc.tuikit.atomicx.karaoke.store.GetSongListCallBack
import io.trtc.tuikit.atomicx.karaoke.store.MusicCatalogService
import io.trtc.tuikit.atomicx.karaoke.store.utils.MusicInfo
import com.tencent.qcloud.tuikit.debug.GenerateTestUserSig

class LocalMusicService : MusicCatalogService() {

    override fun getSongList(callback: GetSongListCallBack) {
        val localList = ArrayList<MusicInfo>()
        callback.onSuccess(localList)
    }

    override fun generateUserSig(
        userId: String,
        callback: ActionCallback,
    ) {
        callback.onSuccess(GenerateTestUserSig.genTestUserSig(userId))
    }
}