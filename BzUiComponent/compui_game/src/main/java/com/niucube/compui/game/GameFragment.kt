package com.niucube.compui.game

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.hapi.base_mvvm.fragment.BaseFrameFragment
import kotlinx.android.synthetic.main.layout_game_game.*


class GameFragment : BaseFrameFragment() {

    fun addGameFragment(container: Int, activity: FragmentActivity) {
        XEGameViewWrap.init(activity.application)

        val fm = activity.supportFragmentManager
        val trans = fm.beginTransaction()
        trans.replace(container, this)
        trans.commit()
    }

    val gameViewWrap by lazy {
        XEGameViewWrap().apply {
            attach(gameView, requireActivity())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameViewWrap.mGameUICall = object : XEGameViewWrap.GameUICall {
            override fun start() {}

            override fun onStop() {
                hide()
            }
        }
    }

    fun startOrHide() {
        if (gameViewWrap.isStart) {
            if (container.visibility == View.VISIBLE) {
                hide()
            } else {
                show()
            }
        } else {
            show()
            gameViewWrap.start()
        }
    }

    fun show() {
        container.visibility = View.VISIBLE
    }

    fun hide() {
        container.visibility = View.GONE
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_game_game
    }

    override fun showLoading(toShow: Boolean) {}
}