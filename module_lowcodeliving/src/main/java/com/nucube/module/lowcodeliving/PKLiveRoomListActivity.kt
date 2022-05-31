package com.nucube.module.lowcodeliving

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.asToast
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveuikit.QNLiveRoomUIKit
import kotlinx.android.synthetic.main.activity_pklive_room_list.*

@Route(path = RouterConstant.LowCodePKLive.LiveRoomList)
class PKLiveRoomListActivity : BaseActivity() {

    override fun initViewData() {
        title = "直播列表"

        roomListView.attach(this)
        tvCreateRoom.setOnClickListener {
            QNLiveRoomUIKit.createAndJoinRoom(this, object : QNLiveCallBack<QNLiveRoomInfo> {
                override fun onError(code: Int, msg: String?) {
                    msg?.asToast()
                }

                override fun onSuccess(data: QNLiveRoomInfo?) {}
            })
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_pklive_room_list
    }

    override fun isToolBarEnable(): Boolean {
        return true
    }

    override fun isTitleCenter(): Boolean {
        return true
    }

}