package android.pattern.schedule;

import android.content.Context;
import android.content.SharedPreferences;

public class AlarmExpirationPreferences {
    private static final String PREFERENCES_FILE = "alarm-expiration";

    private static final String POLICY_ALARM_EXPIRATION_TIMEOUT = "alarm-expiration-timeout";

    private static final String POLICY_ALARM_EXPIRATION_DATE = "alarm-expiration-date";

    private static AlarmExpirationPreferences sPreferences;

    final SharedPreferences mSharedPreferences;

    private AlarmExpirationPreferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    /**
     * TODO need to think about what happens if this gets GCed along with the
     * Activity that initialized it. Do we lose ability to read Preferences in
     * further Activities? Maybe this should be stored in the Application
     * context.
     */
    public static synchronized AlarmExpirationPreferences getPreferences(Context context) {
        if (sPreferences == null) {
            sPreferences = new AlarmExpirationPreferences(context);
        }
        return sPreferences;
    }

    public long getPasswordExpirationTimeout() {
        return mSharedPreferences.getLong(POLICY_ALARM_EXPIRATION_TIMEOUT, 0);
    }

    public void setPasswordExpirationTimeout(long alarmExpirationTimeout) {
        mSharedPreferences.edit().putLong(POLICY_ALARM_EXPIRATION_TIMEOUT, alarmExpirationTimeout).commit();
    }

    public long getPasswordExpirationDate() {
        return mSharedPreferences.getLong(POLICY_ALARM_EXPIRATION_DATE, 0);
    }

    public void setPasswordExpirationDate(long alarmExpirationDate) {
        mSharedPreferences.edit().putLong(POLICY_ALARM_EXPIRATION_DATE, alarmExpirationDate).commit();
    }

    public void clear() {
        mSharedPreferences.edit().clear().commit();
    }

}
