package com.qiniudemo.baseapp.manager.swith;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SwitchEnvHelper {

    private static SwitchEnvHelper unique = null;

    private boolean switchEnable = false;
    private SharedPreferences sp = null;
    private EnvType curEnvType = EnvType.Release;
    private static List<EnvChangeListener> listeners = null;

    private SwitchEnvHelper() {
        listeners = new CopyOnWriteArrayList<>();
    }


    public boolean isSwitchEnable() {
        return switchEnable;
    }


    public static SwitchEnvHelper get() {
        synchronized (SwitchEnvHelper.class) {
            if (unique == null) {
                unique = new SwitchEnvHelper();
            }
            return unique;
        }
    }

    public void init(Application application, boolean switchEnable) {
        this.switchEnable = switchEnable;
        if (switchEnable) {
            sp = application.getSharedPreferences("env_config", Context.MODE_PRIVATE);
            int type = sp.getInt("envType", EnvType.Release.getType());
            curEnvType = EnvType.valueOf(type);
        }else {
            curEnvType = EnvType.valueOf(0);
        }
    }

    public EnvType getEnvType() {
        return curEnvType;
    }

    public void switchEnvType(EnvType envType) {
        if (switchEnable) {
            curEnvType = envType;
            sp.edit().putInt("envType", envType.getType()).apply();

            for (EnvChangeListener listener : listeners) {
                listener.onChange(envType);
            }
        }
    }

    public static void regist( boolean regist,EnvChangeListener listener) {
        if (regist) {
            listeners.add(listener);
        } else {
            listeners.remove(listener);
        }
    }

    public interface EnvChangeListener {
        void onChange(EnvType envType);
    }
}
