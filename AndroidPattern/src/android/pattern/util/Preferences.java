package android.pattern.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.text.TextUtils;

public class Preferences {
    private SharedPreferences mSharedPreferences;
    private String mPreferenceName;
    private static HashMap<String, Preferences> mPreferenceMap = new HashMap<String, Preferences>();

    private Preferences(Context context, String preferenceName) {
        mSharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        mPreferenceName = preferenceName;
    }

    public static synchronized Preferences getInstance(Context context) {
        return getInstance(context, null);
    }

    public static synchronized Preferences getInstance(Context context, String preferenceName) {
        if (TextUtils.isEmpty(preferenceName)) {
            preferenceName = "App_" + context.getPackageName();
        }
        Preferences preference = mPreferenceMap.get(preferenceName);
        if (preference == null) {
            preference = new Preferences(context.getApplicationContext(), preferenceName);
            mPreferenceMap.put(preferenceName, preference);
        }
        return preference;
    }
    
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void clearPreference() {
        mSharedPreferences.edit().clear().commit();
    }

    public void putString(String key, String value) {
        mSharedPreferences.edit().putString(key, value).commit();
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        mSharedPreferences.edit().putInt(key, value).commit();
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    public void putFloat(String key, float value) {
        mSharedPreferences.edit().putFloat(key, value).commit();
    }

    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    public float getFloat(String key, float defaultValue) {
        return mSharedPreferences.getFloat(key, defaultValue);
    }

    public void putLong(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).commit();
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }

    public void remove(String key) {
        mSharedPreferences.edit().remove(key).commit();
    }
}
