package com.nucube.module.lowcodeliving;

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.hapi.ut.SpUtil
import com.qlive.core.QLiveClient
import com.qlive.shoppingservice.QItem
import com.qlive.uikit.component.FloatingModel
import com.qlive.uikit.component.FuncCPTPlayerFloatingHandler
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.dialog.CommonTipDialog
import kotlinx.android.synthetic.main.activity_test_shopping_actvity.*

class TestShoppingActivity : AppCompatActivity() {

    companion object {
        fun start(context: QLiveUIKitContext, item: QItem) {
            val floatCPT =  context.getLiveFuncComponent(FuncCPTPlayerFloatingHandler::class.java)
            if(floatCPT==null){
                val intent = Intent(context.currentActivity, TestShoppingActivity::class.java)
                intent.putExtra("QItem", item)
                context.currentActivity.startActivity(intent)
            }else{
                floatCPT.create(FloatingModel.GO_NEXT_PAGE) { succeed: Boolean, msg: String ->
                    val intent = Intent(context.currentActivity, TestShoppingActivity::class.java)
                    intent.putExtra("QItem", item)
                    context.currentActivity.startActivity(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_shopping_actvity)
        ivGoodsBack.setOnClickListener {
            onBackPressed()
        }
        intent.getSerializableExtra("QItem")?.let {
            (it as QItem).apply {
                Glide.with(this@TestShoppingActivity)
                    .load(thumbnail)
                    .into(ivGoodsImg)
            }
        }
        if (!SpUtil.get("shop").readBoolean("hasTip", false)) {
            SpUtil.get("shop").saveData("hasTip", true)
            CommonTipDialog.TipBuild()
                .setTittle("提示")
                .setContent(
                    "商品页面为您APP内自有页面，\n" +
                            "此处仅展示流程。\n" +
                            "您可以点击直播小窗，返回直播间。"
                )
                .setPositiveText("知道了")
                .isNeedCancelBtn(false)
                .build()
                .show(supportFragmentManager, "")
        }
    }

}