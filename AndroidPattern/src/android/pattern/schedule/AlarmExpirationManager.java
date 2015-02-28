package android.pattern.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmExpirationManager {

    public static final String ACTION_ALARM_EXPIRING = "android.pattern.action.ACTION_ALARM_EXPIRING";

    public static final String ACTION_SYNC_BROADCAST = "android.pattern.action.ACTION_SYNC_BROADCAST";

    public static final long MS_PER_MINUTE = 1 * 60 * 1000;

    public static final long MS_PER_HOUR = 1 * 60 * 60 * 1000;

    public static final long MS_PER_DAY = 1 * 24 * 60 * 60 * 1000;
    public static final long SECONDS_PER_DAY = 1 * 24 * 60 * 60;

    public static final long MS_PER_WEEK = 7 * MS_PER_DAY;

    private final Context mContext;

    public static class ExpiredAlarmReceiver extends BroadcastReceiver {

        private Context mContext;

        @Override
        public void onReceive(Context context, Intent intent) {
            mContext = context;
            String action = intent.getAction();
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
//                AlarmExpirationManager.getInstance(context).setAlarmExpirationTimeout(
//                        MS_PER_WEEK);
                AlarmExpirationManager.getInstance(mContext).handleAlarmExpiration();
            }

//            if (Intent.ACTION_BOOT_COMPLETED.equals(action) || ACTION_ALARM_EXPIRING.equals(action)) {
//                AlarmExpirationManager.getInstance(mContext).handleAlarmExpiration();
//            }
        }

    }

    private static AlarmExpirationManager sInstance = null;

    /**
     * Get the security policy instance
     */
    public synchronized static AlarmExpirationManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AlarmExpirationManager(context);
        }
        return sInstance;
    }

    /**
     * Private constructor (one time only)
     */
    private AlarmExpirationManager(Context context) {
        mContext = context;
    }

    /**
     * ACTION_ALARM_EXPIRING If the expiration alarm is set for the account,
     * send command for deleting data and reset the expiration alarm.
     */
    private void handleAlarmExpiration() {
        startAutoPushService(mContext);
//        setExpirationAlarm();
    }

    public static void startAutoPushService(Context context) {
        Intent intent = new Intent(context, ScheduledService.class);
        intent.setAction(ACTION_SYNC_BROADCAST);
        context.startService(intent);
    }

    /**
     * called when alarm changed
     */
    public void updateAlarmExpiration() {
        long expirationTimeout = getAlarmExpirationTimeout();
        if (expirationTimeout > 0) {
            long alarmExpirationDate = 0;
            alarmExpirationDate = System.currentTimeMillis() + expirationTimeout;
            saveAlarmExpiration(expirationTimeout, alarmExpirationDate);
            setExpirationAlarm();
        }
    }

    /**
     * Save the expiration timeout and expiration date, clear and reset a new
     * expiration alarm.
     * 
     * @param timeout
     *            The new expiration timeout.
     */
    public void setAlarmExpirationTimeout(long timeout) {
        long expirationDate = getAlarmExpiration();
        if (timeout > 0) {
            long expirationTimeout = getAlarmExpirationTimeout();
            long currentMillis = System.currentTimeMillis();
            if ((expirationTimeout == 0) || (expirationDate == 0)/* || (expirationDate > currentMillis)*/) {
                expirationDate = currentMillis + timeout;
            }
        } else if (timeout == 0) {
            // timeout is zero, clear this policy
            expirationDate = 0;
        }
        saveAlarmExpiration(timeout, expirationDate);
        setExpirationAlarm();
    }

    /**
     * Save the expiration timeout and date to user preference.
     * 
     * @param alarmExpirationTimeout
     *            Expiration duration.
     * @param alarmExpirationDate
     *            next alarm expire date.
     */
    private void saveAlarmExpiration(long alarmExpirationTimeout, long alarmExpirationDate) {
        AlarmExpirationPreferences.getPreferences(mContext).setPasswordExpirationTimeout(alarmExpirationTimeout);
        AlarmExpirationPreferences.getPreferences(mContext).setPasswordExpirationDate(alarmExpirationDate);
    }

    /**
     * Get the alarm expire date.
     * 
     * @return date
     */
    public long getAlarmExpiration() {
        return AlarmExpirationPreferences.getPreferences(mContext).getPasswordExpirationDate();
    }

    /**
     * Get the expiration duration.
     * 
     * @return timeout
     */
    public long getAlarmExpirationTimeout() {
        return AlarmExpirationPreferences.getPreferences(mContext).getPasswordExpirationTimeout();
    }

    /**
     * Reset the expiration alarm according to the timeout duration.
     */
    protected void setExpirationAlarm() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_ALARM_EXPIRING);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT
                | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        long expirationDate = getAlarmExpiration();
        if (getAlarmExpirationTimeout() > 0 && expirationDate > 0) {
            long currentTimeMillis = System.currentTimeMillis();
            long timeout = expirationDate - currentTimeMillis;
            // if alarm has expired, recalculate a new expiration date
            if (timeout <= 0) {
                expirationDate = currentTimeMillis + timeout % MS_PER_DAY + MS_PER_DAY;
                expirationDate = currentTimeMillis + getAlarmExpirationTimeout();
            }

            alarmManager.set(AlarmManager.RTC, expirationDate, pendingIntent);
        }
    }

}
