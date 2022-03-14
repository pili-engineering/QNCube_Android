package com.qiniu.bzuicomp.gift

object DataInterfaceNew {

    val GIFT_ICON_RES = intArrayOf(
        R.drawable.gift_01_bell,
        R.drawable.gift_02_icecream,
        R.drawable.gift_03_wine,
        R.drawable.gift_04_cake,
        R.drawable.gift_05_ring,
        R.drawable.gift_06_watch,
        R.drawable.gift_07_diamond,
        R.drawable.gift_08_rocket
    )

    val GIFT_ANIM_RES = intArrayOf(
        R.drawable.gift_anim_bell,
        R.drawable.gift_anim_icecream,
        R.drawable.gift_anim_wine,
        R.drawable.gift_anim_cake,
        R.drawable.gift_anim_ring,
        R.drawable.gift_anim_watch,
        R.drawable.gift_anim_diamond,
        R.drawable.gift_anim_rocket
    )

    var giftNames = arrayOf("铃铛", "冰淇凌", "红酒", "蛋糕", "戒指", "手表", "砖石", "火箭")


    fun getGiftAnimRes(id: Int): Int {
        return GIFT_ANIM_RES[id]
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