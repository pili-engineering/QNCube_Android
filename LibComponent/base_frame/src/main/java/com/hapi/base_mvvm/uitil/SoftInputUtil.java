package com.hapi.base_mvvm.uitil;


import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SoftInputUtil {

    /**
     * 隐藏键盘，不知道键盘焦点在哪个view上
     *
     * @param activity
     */
    public static void hideSoftInputView(Activity activity) {
        hideSoftInputView(activity.getCurrentFocus());
    }

    /**
     * 隐藏focusView上的键盘
     *
     * @param focusView 获取键盘焦点的view
     */
    public static void hideSoftInputView(View focusView) {
        if (focusView != null) {
            InputMethodManager manager = ((InputMethodManager) focusView.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE));
            manager.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
    }

    /**
     * 根据focusView弹出键盘
     *
     * @param focusView
     */
    public static void showSoftInputView(View focusView) {
        if (focusView != null) {
            InputMethodManager manager = ((InputMethodManager) focusView.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE));
            manager.showSoftInput(focusView, 0);
        }
    }

    /**
     * 判断软键盘是否弹出
     */
    public static boolean isShowKeyboard(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.hideSoftInputFromWindow(v.getWindowToken(), 0)) {
            imm.showSoftInput(v, 0);
            return true;
        } else {
            return false;
        }
    }
}
