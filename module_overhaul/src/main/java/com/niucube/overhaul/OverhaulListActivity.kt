package com.niucube.overhaul

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.hapi.baseframe.smartrecycler.IAdapter
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hapi.baseframe.smartrecycler.SmartRecyclerView
import com.hapi.ut.ClickUtil
import com.hapi.ut.ViewUtil
import com.hipi.vm.backGround
import com.niucube.comproom.RoomManager
import com.niucube.overhaul.databinding.ItemOverhaulroomBinding
import com.niucube.overhaul.mode.OverhaulRoomItem
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.RecyclerActivity
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.qiniudemo.baseapp.widget.SimpleDividerDecoration
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout

@Route(path = RouterConstant.Overhaul.OverhaulList)
class OverhaulListActivity : RecyclerActivity<OverhaulRoomItem>() {

    companion object {
        var deviceMode_common = 0
        var deviceMode_Glasses = 1
        var deviceMode_Glasses_test = 2
    }

    @Autowired
    @JvmField
    var deviceMode: Int = deviceMode_common

    private val reqCode = 11
    override fun getLayoutId(): Int {
        return R.layout.activity_overhaul_list
    }

    override val mSmartRecycler: SmartRecyclerView by lazy {
        findViewById<SmartRecyclerView>(R.id.smartRecyclerView).apply {
            this.recyclerView.addItemDecoration(
                SimpleDividerDecoration(
                    this@OverhaulListActivity,
                    Color.parseColor("#EAEAEA"), ViewUtil.dip2px(10f)
                )
            )
        }
    }

    override val adapter: IAdapter<OverhaulRoomItem>
            by lazy { OverhaulRoomAdapter() }

    override val layoutManager: RecyclerView.LayoutManager by lazy { LinearLayoutManager(this) }

    override val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            doWork {
                val interviewList =
                    RetrofitManager.create(OverhaulService::class.java).overhaulList(10, it + 1)
                mSmartRecycler.onFetchDataFinish(interviewList.list, true, interviewList.isEndPage)
            }
            catchError {
                mSmartRecycler.onFetchDataError()
            }
        }
    }

    override fun init() {
        super.init()
        if (deviceMode == deviceMode_Glasses) {
            findViewById<View>(R.id.cardCreateRoom).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.cardCreateRoom).visibility = View.GONE
        }

        findViewById<View>(R.id.cardCreateRoom).setOnClickListener {
            if (RoomManager.mCurrentRoom != null) {
                return@setOnClickListener
            }
            if (ClickUtil.isClickAvalible()) {
                createRoom(UserInfoManager.getUserInfo()?.nickname + "的房间", OverhaulRole.STAFF.role)
            }
        }
    }

    override fun getInitToolBarTitle(): String {
        return "检修房间列表"
    }

    override fun isTitleCenter(): Boolean {
        return true
    }

    override fun isToolBarEnable(): Boolean {
        return true
    }

    override fun requestMenuId(): Int {
        return R.menu.overhaul_op
    }

    override fun isRefreshAtOnStart(): Boolean {
        return false
    }

    override fun loadMoreNeed(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()

        mSmartRecycler.startRefresh()
    }

    private fun createRoom(tittle: String, role: String) {
        backGround {
            showLoading(true)
            findViewById<View>(R.id.cardCreateRoom).isClickable = false
            doWork {
                val roomCreated = RetrofitManager.create(OverhaulService::class.java)
                    .createInterview(
                        tittle, role
                    )
                val room = RetrofitManager.create(OverhaulService::class.java)
                    .joinRoom(roomCreated.provideRoomId(), role)
                room.role = role
                ARouter.getInstance().build(RouterConstant.Overhaul.OverhaulRoom)
                    .withParcelable("overhaulRoomEntity", room)
                    .withInt("deviceMode", deviceMode)
                    .navigation(this@OverhaulListActivity)
            }
            catchError {
                it.printStackTrace()
                it.message?.asToast()
            }
            onFinally {
                showLoading(false)
                findViewById<View>(R.id.cardCreateRoom)?.postDelayed({
                    findViewById<View>(R.id.cardCreateRoom).isClickable = true
                }, 1000)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (reqCode == requestCode && resultCode == RESULT_OK) {
            val etTittle = data?.extras?.getString("etTittle") ?: ""
            val roloId = data?.extras?.getString("roleId") ?: ""
            findViewById<View>(R.id.smartRecyclerView).postDelayed({
                createRoom(etTittle, roloId)
            }, 1000)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {

        if (deviceMode == deviceMode_Glasses) {
            createRoom(UserInfoManager.getUserInfo()?.nickname + "的房间", OverhaulRole.STAFF.role)
        } else {
            if (item.itemId == R.id.interview_add) {
                startActivityForResult(Intent(this, CreateRoomActivity::class.java), reqCode)
            }
        }
        return true
    }

    inner class OverhaulRoomAdapter :
        QSmartViewBindAdapter<OverhaulRoomItem, ItemOverhaulroomBinding>() {
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemOverhaulroomBinding>,
            item: OverhaulRoomItem
        ) {
            helper.binding.tvTittle.text = item.title
            helper.binding.flowlayoutOp.adapter =
                object : TagAdapter<OverhaulRoomItem.Options>(item.options) {
                    override fun getView(
                        parent: FlowLayout,
                        position: Int,
                        t: OverhaulRoomItem.Options
                    ): View {
                        val textView: TextView = LayoutInflater.from(mContext).inflate(
                            R.layout.overhaul_item_overhaul_op,
                            parent,
                            false
                        ) as TextView
                        textView.text = item.options[position].title
                        return textView
                    }
                }
            helper.binding.flowlayoutOp.setOnTagClickListener(TagFlowLayout.OnTagClickListener { view, position, parent ->
                val role = item.options[position].role
                showLoading(true)
                backGround {
                    doWork {
                        val room = RetrofitManager.create(OverhaulService::class.java)
                            .joinRoom(item.roomId, role)
                        room.role = role

                        if (room.role != OverhaulRole.STUDENT.role) {
                            ARouter.getInstance().build(RouterConstant.Overhaul.OverhaulRoom)
                                .withInt("deviceMode", deviceMode)
                                .withParcelable("overhaulRoomEntity", room).navigation(mContext)
                        } else {
                            CommonTipDialog.TipBuild()
                                .setContent("是否加入rtc房间？")
                                .setNegativeText("拉流播放")
                                .setPositiveText("加入订阅")
                                .setListener(object : FinalDialogFragment.BaseDialogListener() {
                                    override fun onDialogNegativeClick(
                                        dialog: DialogFragment,
                                        any: Any
                                    ) {
                                        ARouter.getInstance()
                                            .build(RouterConstant.Overhaul.OverhaulRoom)
                                            .withParcelable("overhaulRoomEntity", room)
                                            .withInt("deviceMode", deviceMode)
                                            .navigation(mContext)
                                    }

                                    override fun onDialogPositiveClick(
                                        dialog: DialogFragment,
                                        any: Any
                                    ) {
                                        room.isStudentJoinRtc = true
                                        ARouter.getInstance()
                                            .build(RouterConstant.Overhaul.OverhaulRoom)
                                            .withParcelable("overhaulRoomEntity", room)
                                            .withInt("deviceMode", deviceMode)
                                            .navigation(mContext)

                                    }
                                }).build().show(supportFragmentManager, "")
                        }
                    }

                    catchError {
                        it.printStackTrace()
                        it.message?.asToast()
                    }
                    onFinally {
                        showLoading(false)
                    }
                }
                true
            })
        }
    }
}