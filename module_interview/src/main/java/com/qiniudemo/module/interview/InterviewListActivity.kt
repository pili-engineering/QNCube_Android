package com.qiniudemo.module.interview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.baseframe.adapter.QRecyclerViewBindAdapter
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.hapi.baseframe.smartrecycler.IAdapter
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hapi.baseframe.smartrecycler.SmartRecyclerView
import com.hapi.ut.ViewUtil
import com.hipi.vm.backGround
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.RecyclerActivity
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.ext.toHttpData
import com.qiniudemo.baseapp.manager.SchemaParser
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.qiniudemo.baseapp.widget.SimpleDividerDecoration
import com.qiniudemo.module.interview.been.InterViewInfo
import com.qiniudemo.module.interview.been.InterViewInfo.Option.*
import com.qiniudemo.module.interview.databinding.InterviewItemInterviewBinding
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Response
import java.text.SimpleDateFormat


/**
 * 面试列表
 */
@Route(path = RouterConstant.Interview.InterviewList)
class InterviewListActivity : RecyclerActivity<InterViewInfo>() {

    override val mSmartRecycler: SmartRecyclerView by lazy {
        findViewById<SmartRecyclerView>(R.id.smartRecyclerView).apply {
            this.recyclerView.addItemDecoration(
                SimpleDividerDecoration(
                    this@InterviewListActivity,
                    Color.parseColor("#EAEAEA"), ViewUtil.dip2px(10f)
                )
            )
        }
    }
    override val adapter: IAdapter<InterViewInfo> by lazy { InterviewAdapter() }
    override val layoutManager: RecyclerView.LayoutManager by lazy { LinearLayoutManager(this) }
    override val fetcherFuc: (page: Int) -> Unit = { page ->
        backGround {
            doWork {
                val interviewList =
                    RetrofitManager.create(InterviewService::class.java).interviewList(10, page + 1)
                mSmartRecycler.onFetchDataFinish(interviewList.list, true, interviewList.isEndPage)
            }
            catchError {
                mSmartRecycler.onFetchDataError()
            }
        }
    }

    override fun getInitToolBarTitle(): String {
        return "面试列表"
    }

    override fun isTitleCenter(): Boolean {
        return true
    }

    override fun isToolBarEnable(): Boolean {
        return true
    }

    override fun requestMenuId(): Int {
        return R.menu.interview_op
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

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.interview_add) {
            ARouter.getInstance().build(RouterConstant.Interview.InterviewCreate)
                .navigation(this)
        }
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.interview_activity_interview_list
    }

    inner class InterviewAdapter :
        QSmartViewBindAdapter<InterViewInfo, InterviewItemInterviewBinding>() {

        private fun requst(op: InterViewInfo.Option, item: InterViewInfo) {
            lifecycleScope.launch(Dispatchers.Main) {
                showLoading(true)
                val resp = async(Dispatchers.IO) {
                    var date: Response? = null
                    try {
                        date = if (op.method == "POST") {
                            RetrofitManager.post(
                                op.requestUrl,
                                FormBody.Builder().build()
                            )
                        } else {
                            RetrofitManager.get(op.requestUrl)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        lifecycleScope.launch(Dispatchers.Main) {
                            e.message?.asToast()
                        }
                    }
                    date
                }
                val date = resp.await()
                date?.let {
                    it.toHttpData(Any::class.java).let {
                        CandlerTipHelper.deleteCalendarEvent(
                            mContext,
                            item.title,
                            item.startTime * 1000
                        )
                    }
                }
                showLoading(false)
                mSmartRecycler.startRefresh()
            }
        }

        private var format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        @SuppressLint("SetTextI18n")
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<InterviewItemInterviewBinding>,
            item: InterViewInfo
        ) {

            helper.binding.tvTittle.text = item.title
            helper.binding.tvStatus.text = item.status
            when (item.statusCode) {
                0 -> helper.binding.tvStatus.setTextColor(Color.parseColor("#176AFF"))
                10 -> helper.binding.tvStatus.setTextColor(Color.parseColor("#FABD48"))
                -10 -> helper.binding.tvStatus.setTextColor(Color.parseColor("#999999"))
            }
            helper.binding.tvGovernment.text = item.goverment
            helper.binding.tvCareer.text = item.career
            helper.binding.tvTime.text =
                "${format.format(item.startTime * 1000)} ~ ${format.format(item.endTime * 1000)}"
            if (item.options?.isEmpty() != false) {
                helper.binding.flowlayoutOp.visibility = View.GONE
            } else {
                helper.binding.flowlayoutOp.visibility = View.VISIBLE
            }
            val listOp = ArrayList<InterViewInfo.Option>()
            item.options?.forEach {
                if (it != null) {
                    listOp.add(it)
                }
            }
            helper.binding.flowlayoutOp.adapter =
                object : TagAdapter<InterViewInfo.Option>(listOp) {
                    override fun getView(
                        parent: FlowLayout,
                        position: Int,
                        t: InterViewInfo.Option
                    ): View {
                        val textView: TextView = LayoutInflater.from(mContext).inflate(
                            R.layout.interview_item_interview_op,
                            parent,
                            false
                        ) as TextView
                        textView.text = listOp[position].title
                        return textView
                    }
                }

            helper.binding.flowlayoutOp.setOnTagClickListener { _, position, _ ->
                val op = listOp[position]
                when (op.type) {
                    type_end -> {
                        CommonTipDialog.TipBuild()
                            .setContent("是否结束当前面试？")
                            .setListener(object : FinalDialogFragment.BaseDialogListener() {
                                override fun onDialogPositiveClick(
                                    dialog: DialogFragment,
                                    any: Any
                                ) {
                                    requst(op, item)
                                }

                            }).build()
                            .show(supportFragmentManager, "type_end")
                    }
                    (type_cancel) -> requst(op, item)
                    type_share -> {
                        val sendIntent = Intent()
                        sendIntent.action = Intent.ACTION_SEND;
                        // 指定发送的内容
                        sendIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            item?.shareInfo?.content
                        );
                        // 指定发送内容的类型
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent)
                    }
                    else -> SchemaParser.parseRouter(
                        mContext,
                        supportFragmentManager,
                        op.requestUrl
                    )
                }
                true
            }
        }
    }


}