package com.niucube.module.amusement

import androidx.fragment.app.DialogFragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.hapi.base_mvvm.refresh.SmartRecyclerView
import com.hapi.happy_dialog.FinalDialogFragment
import com.hipi.vm.backGround
import com.niucube.module.amusement.mode.AmusementRoomItem
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseRoomListActivity
import com.qiniudemo.baseapp.been.CreateRoomEntity
import com.qiniudemo.baseapp.dialog.CommonCreateRoomDialog
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.RoomService
import com.qiniudemo.baseapp.widget.CommonTipDialog
import kotlinx.android.synthetic.main.activity_amusement_list.*

@Route(path = RouterConstant.Amusement.AmusementList)
class AmusementListActivity : BaseRoomListActivity() {

    override var defaultType: String = "show"
    override fun getLayoutId(): Int = R.layout.activity_amusement_list
    override val mSmartRecycler: SmartRecyclerView
            by lazy { smartRecyclerView }
    override val adapter
            by lazy {
                BaseRoomItemAdapter().apply {
                    setOnItemClickListener { _, _, position ->

                        CommonTipDialog.TipBuild()
                            .setTittle("是否加入rtc房间？")
                            .setContent("拉流模式是指，上麦互动之前采用rtmp流的方式观看,上麦后成功rtc主播,下麦切换拉流")
                            .setNegativeText("拉流播放")
                            .setPositiveText("加入订阅")
                            .setListener(object : FinalDialogFragment.BaseDialogListener() {
                                override fun onDialogNegativeClick(
                                    dialog: DialogFragment,
                                    any: Any
                                ) {
                                    ARouter.getInstance()
                                        .build(RouterConstant.Amusement.AmusementRoom)
                                        .withString("solutionType", solutionType)
                                        .withString("roomId", data[position].roomId)
                                        .withBoolean("isUserJoinRTC", false)
                                        .navigation(this@AmusementListActivity)
                                }

                                override fun onDialogPositiveClick(
                                    dialog: DialogFragment,
                                    any: Any
                                ) {
                                    ARouter.getInstance()
                                        .build(RouterConstant.Amusement.AmusementRoom)
                                        .withString("solutionType", solutionType)
                                        .withString("roomId", data[position].roomId)
                                        .withBoolean("isUserJoinRTC", true)
                                        .navigation(this@AmusementListActivity)

                                }
                            }).build().show(supportFragmentManager, "")


                    }
                }
            }

    override fun initViewData() {
        super.initViewData()
        tvCreate.setOnClickListener {
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
                                ARouter.getInstance().build(RouterConstant.Amusement.AmusementRoom)
                                    .withString("solutionType", solutionType)
                                    .withString("roomId", room.roomInfo!!.roomId)
                                    .withBoolean("isUserJoinRTC", true)
                                    .navigation(this@AmusementListActivity)
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