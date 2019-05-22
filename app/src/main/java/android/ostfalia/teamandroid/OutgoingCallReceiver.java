package android.ostfalia.teamandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class OutgoingCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        //check the flag
        //open your activity immediately after a call
        Intent intent1 = new Intent(context, CallActivity.class);
        intent1.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);

    }
}
