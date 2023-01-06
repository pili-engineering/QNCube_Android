package com.qiniudemo.module.interview

import android.Manifest
import android.annotation.SuppressLint
import android.text.InputType
import android.text.TextUtils
import androidx.core.widget.addTextChangedListener
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.hipi.vm.lifecycleBg
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.module.interview.been.InterViewDetails
import com.qiniudemo.module.interview.databinding.InterviewActivityCreateBinding
import com.tbruyelle.rxpermissions2.RxPermissions
import java.text.SimpleDateFormat
import java.util.*


/**
 * 创建修改面试
 */
@Route(path = RouterConstant.Interview.InterviewCreate)
class CreateInterViewActivity : BaseActivity<InterviewActivityCreateBinding>() {

    @Autowired
    @JvmField
    var interviewId = ""

    //需要删除的面试提醒
    private var calendarEventToDelete: String? = null
    private var calendarEventTimeToDelete: Long = 0
    override fun init() {
        if (interviewId == null) {
            interviewId = ""
        }
        if (interviewId.isNotEmpty()) {
            lifecycleBg {
                showLoading(true)
                doWork {
                    val details = RetrofitManager.create(InterviewService::class.java)
                        .interviewDetails(interviewId)
                    calendarEventToDelete = details.title
                    calendarEventTimeToDelete = details.startTime
                    details.authCode = (Math.random() * 10000).toInt().toString()
                    details.startTime = details.startTime * 1000
                    details.endTime = details.endTime * 1000
                    initView(details)
                }
                catchError {
                    finish()
                }
                onFinally {
                    showLoading(false)
                }
            }
            binding.tvTittle.text = "修改面试"
        } else {
            val interViewDetails = InterViewDetails().apply {
                val currentData = Calendar.getInstance()
                var min = currentData.get(Calendar.MINUTE)
                var hours = currentData.get(Calendar.HOUR_OF_DAY)
                when (min) {
                    in 0..30 -> min = 30
                    in 31..59 -> {
                        min = 0
                        hours += 1
                    }
                }
                currentData.set(Calendar.HOUR_OF_DAY, hours)
                currentData.set(Calendar.MINUTE, min)
                currentData.set(Calendar.SECOND, 0)
                startTime = currentData.time.time
                title = ""
                endTime = (currentData.time.time + 30 * 1000 * 60)
                setAuth(false)
                authCode = (Math.random() * 10000).toInt().toString()

            }
            initView(interViewDetails)
            binding.tvTittle.text = "创建面试"
        }
        binding.tvBack.setOnClickListener {
            finish()
        }
    }

    private val dateDialogFragment by lazy {
        val dateTimeDialogFragment =
            SwitchDateTimeDialogFragment.newInstance(
                "",
                "确定",
                "取消"
            )
        dateTimeDialogFragment.startAtCalendarView()
        dateTimeDialogFragment.set24HoursMode(true)
        dateTimeDialogFragment.minimumDateTime = Date()
        dateTimeDialogFragment.maximumDateTime = GregorianCalendar(
            2025,
            Calendar.DECEMBER,
            31
        ).time
        dateTimeDialogFragment.setDefaultDateTime(
            Date()
        )
        dateTimeDialogFragment
    }


    var format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private var isAutoTittle = true
    private fun initView(interViewDetails: InterViewDetails) {
        binding.etTittle.setText(interViewDetails.title)
        binding.tvStartTime.text = format.format(interViewDetails.startTime)
        binding.tvEndTime.text = format.format(interViewDetails.endTime)
        binding.etGovernment.setText(interViewDetails.goverment)
        binding.etCareer.setText(interViewDetails.career)
        binding.etInterviewerName.setText(interViewDetails.candidateName)
        binding.etInterviewerPhone.setText(interViewDetails.candidatePhone)
        binding.etInterviewerName.addTextChangedListener {
            if (isAutoTittle) {
                isAutoTittle = true
                if (it.toString().isEmpty()) {
                    binding.etTittle.setText("")
                } else {
                    binding.etTittle.setText("${it.toString()}的面试")
                }
            }
        }
        binding.etTittle.addTextChangedListener {
            val etInterviewerNameStr = binding.etInterviewerName.text.toString()
            isAutoTittle = it.toString().isEmpty() || it.toString() == "${etInterviewerNameStr}的面试"
        }

        binding.swPasswd.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.tvInterviewerPwd.setText(interViewDetails.authCode)
            } else {
                binding.tvInterviewerPwd.setText("")
            }
        }
        binding.cbIsPwdVisbility.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.tvInterviewerPwd.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            } else {
                binding.tvInterviewerPwd.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
        binding.tvStartTime.setOnClickListener {
            dateDialogFragment.minimumDateTime = Date(interViewDetails.startTime.toLong())
            dateDialogFragment.setDefaultDateTime(
                Date(interViewDetails.startTime.toLong())
            )
            dateDialogFragment.setOnButtonClickListener(object :
                SwitchDateTimeDialogFragment.OnButtonClickListener {
                override fun onPositiveButtonClick(date: Date) {
                    interViewDetails.startTime = date.time
                    binding.tvStartTime.text = format.format(interViewDetails.startTime)

                    if (interViewDetails.endTime.toLong() < interViewDetails.startTime.toLong()) {
                        interViewDetails.endTime = (interViewDetails.startTime + 30 * 1000 * 60)
                        binding.tvEndTime.text = format.format(interViewDetails.endTime)
                    }
                }

                override fun onNegativeButtonClick(date: Date?) {}
            })
            dateDialogFragment.show(supportFragmentManager, "dateDialogFragment")
        }

        binding.tvEndTime.setOnClickListener {
            dateDialogFragment.minimumDateTime = Date(interViewDetails.endTime.toLong())
            dateDialogFragment.setDefaultDateTime(
                Date(interViewDetails.endTime.toLong())
            )
            dateDialogFragment.setOnButtonClickListener(object :
                SwitchDateTimeDialogFragment.OnButtonClickListener {
                override fun onPositiveButtonClick(date: Date) {
                    interViewDetails.endTime = date.time
                    binding.tvEndTime.text = format.format(interViewDetails.endTime)
                }

                override fun onNegativeButtonClick(date: Date?) {}
            })
            dateDialogFragment.show(supportFragmentManager, "dateDialogFragment")
        }

        binding.swPasswd.isChecked = interViewDetails.auth
        binding.tvFinish.setOnClickListener {

            val title = binding.etTittle.text.toString()
            val startTime = interViewDetails.startTime / 1000
            val endTime = interViewDetails.endTime / 1000
            val goverment = binding.etGovernment.text.toString()
            val career = binding.etCareer.text.toString()
            val isAuth = if (binding.swPasswd.isChecked) "true" else "false"
            val authCode = interViewDetails.authCode
            val isRecorded = if (binding.swRecord.isChecked) "true" else "false"
            val candidateName = binding.etInterviewerName.text.toString()
            val candidatePhone = binding.etInterviewerPhone.text.toString()

            if (title.isEmpty()) {
                "请输入面试标题".asToast()
                return@setOnClickListener
            }
            if (goverment.isEmpty()) {
                "请输入面试公司/部门".asToast()
                return@setOnClickListener
            }
            if (career.isEmpty()) {
                "请输入面试职位".asToast()
                return@setOnClickListener
            }
            if (candidateName.isEmpty()) {
                "请输入面试者姓名".asToast()
                return@setOnClickListener
            }
            if (candidatePhone.isEmpty()) {
                "请输入面试者电话".asToast()
                return@setOnClickListener
            }


            lifecycleBg {
                showLoading(true)
                doWork {
                    if (!TextUtils.isEmpty(interviewId)) {
                        RetrofitManager.create(InterviewService::class.java).modifyInterview(
                            interviewId,
                            title,
                            startTime.toString(),
                            endTime.toString(),
                            goverment,
                            career,
                            isAuth,
                            authCode,
                            isRecorded,
                            candidateName,
                            candidatePhone
                        )
                        "修改成功".asToast()
                        calendarEventToDelete?.let {
                            CandlerTipHelper.deleteCalendarEvent(
                                applicationContext,
                                it, calendarEventTimeToDelete
                            )
                        }
                        addCalendarEvent(title, startTime)
                    } else {
                        RetrofitManager.create(InterviewService::class.java).createInterview(
                            title,
                            startTime.toString(),
                            endTime.toString(),
                            goverment,
                            career,
                            isAuth,
                            authCode,
                            isRecorded,
                            candidateName,
                            candidatePhone
                        )
                        "创建成功".asToast()
                    }
                    addCalendarEvent(title, startTime)
                    finish()
                }
                onFinally {
                    showLoading(false)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun addCalendarEvent(title: String, startTime: Long) {
        RxPermissions(this).request(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )
            .subscribe({
                if (it) {
                    CandlerTipHelper.addCalendarEvent(
                        applicationContext,
                        title,
                        "您有一条面试需要处理",
                        startTime * 1000,
                        30
                    )
                }
            }, {
                it.printStackTrace()
            })
    }

    override fun isTranslucentBar(): Boolean {
        return false
    }

}