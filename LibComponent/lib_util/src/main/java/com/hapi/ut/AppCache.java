package com.hapi.ut;

import android.app.Application;

/**
 * Created by xx on 2017/7/10.
 * 
 */
public class AppCache {
    private static Application context;

    public static Application getContext() {
        return context;
    }

    public static void setContext(Application app) {
        if (app == null) {
            AppCache.context = null;
            return;
        }
        AppCache.context = app;
    }
}
