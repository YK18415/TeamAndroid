package android.ostfalia.teamandroid;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.Date;

public class CallReceiver extends PhoneCallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        Toast.makeText(ctx, "Anruf kommt", Toast.LENGTH_LONG).show();
        System.out.println("onIncomingCallReceived");
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        Toast.makeText(ctx, "Anruf kommt", Toast.LENGTH_LONG).show();
        System.out.println("onIncomingCallAnswered ------------------------------------------------------------------------");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        Toast.makeText(ctx, "Anruf kommt", Toast.LENGTH_LONG).show();
        System.out.println("onIncomingCallEnded");
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        Toast.makeText(ctx, "Anruf kommt", Toast.LENGTH_LONG).show();
        System.out.println("onOutgoingCallStarted");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        Toast.makeText(ctx, "Anruf kommt", Toast.LENGTH_LONG).show();
        System.out.println("onOutgoingCallEnded");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start)
    {
        Toast.makeText(ctx, "Anruf kommt", Toast.LENGTH_LONG).show();
        System.out.println("onMissedCall");
    }

}