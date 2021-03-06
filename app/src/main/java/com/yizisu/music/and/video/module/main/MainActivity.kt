package com.yizisu.music.and.video.module.main


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.billy.android.swipe.SmartSwipe
import com.billy.android.swipe.SmartSwipeRefresh
import com.billy.android.swipe.consumer.StretchConsumer
import com.billy.android.swipe.consumer.TranslucentSlidingConsumer
import com.yizisu.basemvvm.app
import com.yizisu.basemvvm.utils.goHome
import com.yizisu.basemvvm.utils.gone
import com.yizisu.basemvvm.utils.transparentStatusBar
import com.yizisu.basemvvm.view.simpleFragmentPagerAdapter
import com.yizisu.basemvvm.widget.BaseImageView
import com.yizisu.music.and.video.R
import com.yizisu.music.and.video.baselib.BaseUiActivity
import com.yizisu.music.and.video.module.search.SearchMusicActivity
import com.yizisu.music.and.video.service.music.MusicService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseUiActivity() {
    override fun getContentResOrView(inflater: LayoutInflater): Any? {
        return R.layout.activity_main
    }

    private val windowCoverIv by lazy {
        BaseImageView(app).apply {
            scaleType = ImageView.ScaleType.FIT_XY
            setImageResource(R.drawable.bg_launcher_window)
        }
    }

    override fun initUi(savedInstanceState: Bundle?) {
        super.initUi(savedInstanceState)
        window
            .decorView
            .findViewById<FrameLayout>(android.R.id.content)
            .addView(windowCoverIv)
        MusicService.bindService(this)
        transparentStatusBar()
        homeVp.adapter = simpleFragmentPagerAdapter(
            mutableListOf(MainFragment.create())
        )
        windowCoverIv.post {
            windowCoverIv.animate().alpha(0f)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    windowCoverIv.gone()
                }
                .setDuration(1000)
                .start()
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        goHome()
    }

    override fun onDestroy() {
        MusicService.unBindService(this)
        super.onDestroy()
    }

    override fun getClickView(): List<View?>? {
        return listOf(searchTv, windowCoverIv)
    }

    override fun onSingleClick(view: View) {
        super.onSingleClick(view)
        when (view) {
            searchTv -> {
                SearchMusicActivity.start(this)
            }
            else -> {
            }
        }
    }
}
