package com.hapi.ut;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import androidx.collection.SimpleArrayMap;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * SharedPreferences工具类，支持更具不同name获取不同的sp
 */
public class SpUtil {
    private SharedPreferences shref = null;
    private String shrefName = null;
    private String shrefDir = null;

    private static SimpleArrayMap<String, SpUtil> SP_MAP = new SimpleArrayMap<>();

    public static SpUtil get(String spName) {
        SpUtil sharefUtil = SP_MAP.get(spName);
        if (sharefUtil == null) {
            synchronized (SpUtil.class) {
                sharefUtil = new SpUtil(spName);
                SP_MAP.put(spName, sharefUtil);
            }
        }
        return sharefUtil;
    }

    public static SpUtil get(String spName, int mode) {
        SpUtil sharefUtil = SP_MAP.get(spName);
        if (sharefUtil == null) {
            synchronized (SpUtil.class) {
                sharefUtil = new SpUtil(spName, mode);
                SP_MAP.put(spName, sharefUtil);
            }
        }
        return sharefUtil;
    }

    private SpUtil() {
        this("default_sharedpreferences");
    }

    private SpUtil(String shrefName) {
        this(shrefName, Context.MODE_PRIVATE);
    }

    private SpUtil(String shrefName, int mode) {
        this.shrefName = shrefName;
        this.shref = AppCache.getContext().getSharedPreferences(shrefName, mode);
        shrefDir = getShrefDir(AppCache.getContext());
    }

    /**
     * 获取SharedPreferences文件的目录路径
     *
     * @param context
     * @return
     */
    private String getShrefDir(Context context) {
        return File.separator + "data" + File.separator + "data" + File.separator + context.getPackageName()
                + File.separator + "shared_prefs";
    }

    /**
     * 判断sp文件是否存在¬
     *
     * @return
     */
    public boolean isShrefFileExists() {
        File file = new File(shrefDir, shrefName + ".xml");
        if (file != null) {
            return file.exists();
        }
        return false;
    }

    /**
     * 获取配置文件上次修改时间
     *
     * @return
     */
    public final long lastModified() {
        File file = new File(shrefDir, shrefName + ".xml");
        if (file != null && file.exists()) {
            return file.lastModified();
        }
        return 0;
    }

    /**
     * SP中写入string
     *
     * @param key
     * @param value
     */
    public final void saveData(String key, String value) {
        saveData(key, value, false);
    }

    /**
     * SP中写入string
     *
     * @param key
     * @param value
     * @param commit
     */
    public final void saveData(String key, String value, boolean commit) {
        Editor editor = shref.edit();
        editor.putString(key, value);
        if (commit) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * SP中写入int
     *
     * @param key
     * @param value
     */
    public final void saveData(String key, int value) {
        saveData(key, value, false);
    }

    /**
     * SP中写入int
     *
     * @param key
     * @param value
     * @param commit
     */
    public final void saveData(String key, int value, boolean commit) {
        Editor editor = shref.edit();
        editor.putInt(key, value);
        if (commit) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * SP中写入boolean
     *
     * @param key
     * @param value
     */
    public final void saveData(String key, boolean value) {
        saveData(key, value, false);
    }

    /**
     * SP中写入boolean
     *
     * @param key
     * @param value
     * @param commit
     */
    public final void saveData(String key, boolean value, boolean commit) {
        Editor editor = shref.edit();
        editor.putBoolean(key, value);
        if (commit) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * SP中写入long
     *
     * @param key
     * @param value
     */
    public final void saveData(String key, long value) {
        saveData(key, value, false);
    }

    /**
     * SP中写入long
     *
     * @param key
     * @param value
     * @param commit
     */
    public final void saveData(String key, long value, boolean commit) {
        Editor editor = shref.edit();
        editor.putLong(key, value);
        if (commit) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * SP中写入float
     *
     * @param key
     * @param value
     */
    public final void saveData(String key, float value) {
        saveData(key, value, false);
    }

    /**
     * SP中写入float
     *
     * @param key
     * @param value
     * @param commit
     */
    public final void saveData(String key, float value, boolean commit) {
        Editor editor = shref.edit();
        editor.putFloat(key, value);
        if (commit) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * SP中写入set<string>
     *
     * @param key
     * @param value
     */
    public final void saveData(String key, Set<String> value) {
        saveData(key, value, false);
    }

    /**
     * SP中写入set<string>
     *
     * @param key
     * @param value
     */
    public final void saveData(String key, Set<String> value, boolean commit) {
        Editor editor = shref.edit();
        editor.putStringSet(key, value);
        if (commit) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * SP中读取string
     *
     * @param key
     * @return
     */
    public final String readString(String key) {
        return readString(key, null);
    }

    /**
     * SP中读取string
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public final String readString(String key, String defaultValue) {
        return shref.getString(key, defaultValue);
    }

    /**
     * SP中读取int
     *
     * @param key
     * @return
     */
    public final int readInt(String key) {
        return readInt(key, 0);
    }

    /**
     * SP中读取int
     *
     * @param key
     * @param defalValue
     * @return
     */
    public final int readInt(String key, int defalValue) {
        return shref.getInt(key, defalValue);
    }

    /**
     * SP中读取boolean
     *
     * @param key
     * @return
     */
    public final boolean readBoolean(String key) {
        return readBoolean(key, false);
    }

    /**
     * SP中读取boolean
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public final boolean readBoolean(String key, boolean defaultValue) {
        return shref.getBoolean(key, defaultValue);
    }

    /**
     * SP中读取long
     *
     * @param key
     * @return
     */
    public final long readLong(String key) {
        return readLong(key, 0);
    }

    /**
     * SP中读取long
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public final long readLong(String key, long defaultValue) {
        return shref.getLong(key, defaultValue);
    }

    /**
     * SP中读取float
     *
     * @param key
     * @return
     */
    public final float readFloat(String key) {
        return readFloat(key, 0);
    }

    /**
     * SP中读取float
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public final float readFloat(String key, float defaultValue) {
        return shref.getFloat(key, defaultValue);
    }

    /**
     * SP中读取set<string>
     *
     * @param key
     * @return
     */
    public final HashSet<String> readSetString(String key) {
        return new HashSet<String>(shref.getStringSet(key, new HashSet<String>()));
    }

    /**
     * SP中读取set<string>
     *
     * @param key
     * @return
     */
    public final HashSet<String> readSetString(String key, Set<String> defaultValue) {
        return new HashSet<String>(shref.getStringSet(key, defaultValue));
    }

    /**
     * SP中是否包含指定的key
     *
     * @param key
     * @return
     */
    public final boolean contains(String key) {
        return shref.contains(key);
    }

    /**
     * SP中移除指定的key
     *
     * @param key
     */
    public final void remove(String key) {
        remove(key, false);
    }

    /**
     * SP中移除指定的key
     *
     * @param key
     * @param commit
     */
    public final void remove(String key, boolean commit) {
        Editor editor = shref.edit();
        editor.remove(key);
        if (commit) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * 清除sp中的数据
     */
    public final void clear() {
        Editor editor = shref.edit();
        editor.clear();
        editor.apply();
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener, boolean regist) {
        if (regist) {
            shref.registerOnSharedPreferenceChangeListener(listener);
        } else {
            shref.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }
}
