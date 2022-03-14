package com.qiniu.bzuicomp.gift;

import java.util.ArrayList;

public class DataInterface {

    /*获取礼物列表*/
    public static ArrayList<Gift> getGiftList() {
        ArrayList<Gift> gifts = new ArrayList<>();
        String[] giftNames = new String[]{"蛋糕", "气球", "花儿", "项链", "戒指"};
        int[] giftRes = new int[]{R.drawable.gift_cake, R.drawable.gift_ballon, R.drawable.gift_flower, R.drawable.gift_necklace, R.drawable.gift_ring};

        for (int i = 0; i < giftNames.length; i++) {
            Gift gift = new Gift();
            gift.setGiftId("GiftId_" + (i + 1));
            gift.setGiftName(giftNames[i]);
            gift.setGiftRes(giftRes[i]);
            gifts.add(gift);
        }
        return gifts;
    }

    /*获取礼物名*/
    public static String getGiftNameById(String giftId) {
        switch (giftId) {
            case "GiftId_1":
                return "蛋糕";
            case "GiftId_2":
                return "气球";
            case "GiftId_3":
                return "花儿";
            case "GiftId_4":
                return "项链";
            case "GiftId_5":
                return "戒指";
        }
        return null;
    }
    /*获取礼物名*/
    public static int getGiftIconNameById(String giftId) {
        switch (giftId) {
            case "GiftId_1":
                return R.drawable.gift_cake;
            case "GiftId_2":
                return R.drawable.gift_ballon;
            case "GiftId_3":
                return R.drawable.gift_flower;
            case "GiftId_4":
                return R.drawable.gift_necklace;
            case "GiftId_5":
                return R.drawable.gift_ring;
        }
        return 0;
    }

}
