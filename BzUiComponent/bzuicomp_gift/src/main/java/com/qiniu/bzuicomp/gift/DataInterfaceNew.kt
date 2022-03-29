package com.qiniu.bzuicomp.gift

object DataInterfaceNew {

    val GIFT_ICON_RES = intArrayOf(
        R.drawable.gift_cake, R.drawable.gift_ballon, R.drawable.gift_flower, R.drawable.gift_necklace, R.drawable.gift_ring
    )

    val GIFT_ANIM_RES = intArrayOf(
        R.drawable.shengdan,
        R.drawable.cat,
        R.drawable.lu,
        R.drawable.gift,
        R.drawable.gift
    )

    var giftNames = arrayOf("蛋糕", "气球", "花儿", "项链", "戒指")

    fun getGiftAnimRes(id: Int): Int {
        if(id<GIFT_ANIM_RES.size){
            return GIFT_ANIM_RES[id]
        }
        return -1
    }

    fun getGiftIcon(id: Int): Int {
        return GIFT_ICON_RES[id]
    }

    val gifts = ArrayList<Gift>()

    init {
        for (i in giftNames.indices) {
            val gift = Gift()
            gift.giftId = i.toString() + ""
            gift.giftName = giftNames[i]
            gift.giftRes = GIFT_ICON_RES[i]
            gifts.add(gift)
        }
    }

}