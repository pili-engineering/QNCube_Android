package com.niucube.compui.beauty.model;

import android.graphics.Bitmap;

public class StickerOptionsItem {
    public String name;
    public Bitmap unselectedtIcon;
    public Bitmap selectedtIcon;

    public StickerOptionsItem(String name, Bitmap unselectedtIcon, Bitmap selectedtIcon) {
        this.name = name;
        this.unselectedtIcon = unselectedtIcon;
        this.selectedtIcon = selectedtIcon;
    }
}
