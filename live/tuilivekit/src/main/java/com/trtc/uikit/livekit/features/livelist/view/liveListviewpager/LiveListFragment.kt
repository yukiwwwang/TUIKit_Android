package com.trtc.uikit.livekit.features.livelist.view.liveListviewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.trtc.tuikit.atomicxcore.api.live.LiveInfo

class LiveListFragment(
    private var liveInfo: LiveInfo,
    private val liveListViewAdapter: LiveListViewPagerAdapter
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return liveListViewAdapter.createLiveInfoView(liveInfo)
    }

    fun updateLiveInfo(liveInfo: LiveInfo) {
        this.liveInfo = liveInfo
        view?.let { liveListViewAdapter.updateLiveInfoView(it, liveInfo) }
    }

    fun onFragmentWillSlideIn() {
        view?.let { liveListViewAdapter.onViewWillSlideIn(it) }
    }

    fun onFragmentDidSlideIn() {
        view?.let { liveListViewAdapter.onViewDidSlideIn(it) }
    }

    fun onFragmentSlideInCancelled() {
        view?.let { liveListViewAdapter.onViewSlideInCancelled(it) }
    }

    fun onFragmentWillSlideOut() {
        view?.let { liveListViewAdapter.onViewWillSlideOut(it) }
    }

    fun onFragmentDidSlideOut() {
        view?.let { liveListViewAdapter.onViewDidSlideOut(it) }
    }

    fun onFragmentSlideOutCancelled() {
        view?.let { liveListViewAdapter.onViewSlideOutCancelled(it) }
    }
}