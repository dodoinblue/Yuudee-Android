package android.pattern.schedule;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.pattern.BaseApplication;

public class ScheduledService extends Service {
    public static final String LOG_TAG = "MdmService";
    public static final Object mLock = new Object();

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !AlarmExpirationManager.ACTION_SYNC_BROADCAST.equals(intent.getAction())) {
            return super.onStartCommand(intent, flags, startId);
        }

        clearCacheIfNecessary();
        return super.onStartCommand(intent, flags, startId);
    }
    
    private void clearCacheIfNecessary() {
    	
    }
}
