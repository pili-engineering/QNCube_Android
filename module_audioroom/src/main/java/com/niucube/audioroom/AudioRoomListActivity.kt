package com.niucube.audioroom

import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.hapi.baseframe.smartrecycler.SmartRecyclerView
import com.hipi.vm.backGround
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseRoomListActivity
import com.qiniudemo.baseapp.been.CreateRoomEntity
import com.qiniudemo.baseapp.dialog.CommonCreateRoomDialog
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.RoomService

@Route(path = RouterConstant.VoiceChatRoom.voiceChatRoomList)
class AudioRoomListActivity : BaseRoomListActivity() {

    override var defaultType: String = "voiceChatRoom"
    override fun getLayoutId(): Int = R.layout.activity_audio_room_list
    override val mSmartRecycler: SmartRecyclerView
            by lazy { findViewById(R.id.smartRecyclerView) }

    override val adapter
            by lazy {
                BaseRoomItemAdapter().apply {
                    setOnItemClickListener { _, _, position ->
                        ARouter.getInstance().build(RouterConstant.VoiceChatRoom.voiceChatRoom)
                            .withString("solutionType", solutionType)
                            .withString("roomId", data[position].roomId)
                            .navigation(this@AudioRoomListActivity)
                    }
                }
            }

    override fun init() {
        super.init()
        findViewById<TextView>(R.id.tvCreate).setOnClickListener {
            CommonCreateRoomDialog().apply {
                setDefaultListener(object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        backGround {
                            showLoading(true)
                            doWork {
                                val room = RetrofitManager.create(RoomService::class.java)
                                    .createRoom(CreateRoomEntity().apply {
                                        title = any.toString()
                                        type = solutionType
                                    })
                                ARouter.getInstance()
                                    .build(RouterConstant.VoiceChatRoom.voiceChatRoom)
                                    .withString("solutionType", solutionType)
                                    .withString("roomId", room.roomInfo!!.roomId)
                                    .navigation(this@AudioRoomListActivity)
                            }
                            catchError {
                                it.printStackTrace()
                                it.message?.asToast()
                            }
                            onFinally {
                                showLoading(false)
                            }
                        }
                    }
                }).show(supportFragmentManager, "")
            }
        }
    }
}