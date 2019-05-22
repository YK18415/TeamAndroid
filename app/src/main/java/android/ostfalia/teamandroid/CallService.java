package android.ostfalia.teamandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class CallService extends Service {
    public CallService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Call", "Juhuuuuu. yannick ist doof");
        startService(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
