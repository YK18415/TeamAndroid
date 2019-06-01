package android.ostfalia.teamandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class PhoneCallReceiver extends BroadcastReceiver {

    private static String savedNumber;  //because the passed incoming is only valid in ringing
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming;
    private static Date callStartTime;

    public static String partnerNumber = null;

    public static String formatPhoneNumber(String number){
        return number.charAt(0)=='0'?"+49" + number.substring(1):number;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        }
        else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                state = TelephonyManager.CALL_STATE_IDLE;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
            }


            onCallStateChanged(context, state, number);
        }

        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);


                //Toast.makeText(CallActivity.this, "hjgredtrzguhijztrjiuhzgtfzujikuztgfghzujiuhzgf", Toast.LENGTH_LONG).show();

               System.out.println("äääääääääääääääääääääääääääääääääääääää "+incomingNumber);

            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

    }

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallReceived(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                else
                {
                    isIncoming = true;
                    callStartTime = new Date();
                    onIncomingCallAnswered(context, savedNumber, callStartTime);
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                }
                else if(isIncoming){
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                else{
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }

    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        Toast.makeText(ctx, "Anruf onIncomingCallReceived", Toast.LENGTH_LONG).show();
        System.out.println("onIncomingCallReceived");
    }

    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        SharedPreferences settings = ctx.getSharedPreferences("betreuapp", MODE_PRIVATE);
        String contactListString = settings.getString("contactList", "");
        Gson gson = new Gson();
        if(contactListString.isEmpty()) {
            return;
        }
        Contact[] contactArray = gson.fromJson(contactListString, Contact[].class);

        String formattedIncomingNumber = formatPhoneNumber(number);
        partnerNumber = formattedIncomingNumber;

        for (Contact contact: contactArray) {
            String contactNumber = contact.getTelephonenumber();
            String formattedNumber = formatPhoneNumber(contactNumber);
            if(formattedNumber.equals(formattedIncomingNumber)) {
                PhoneCallReceiver.partnerNumber = formattedNumber;
                String role = settings.getString("role","");

                switch(role) {
                    case "Betreuer":
                        MainActivity.role = Role.BETREUER;
                        break;
                    case "Betreuter":
                        MainActivity.role = Role.BETREUTER;
                        break;
                }
                Toast.makeText(ctx, "Anruf kommt onIncomingCallAnswered", Toast.LENGTH_LONG).show();
                System.out.println("onIncomingCallAnswered ------------------------------------------------------------------------");
                Intent intent = new Intent(ctx, CallActivity.class); // TODO: Change that with Enum.

                ctx.startActivity(intent);
                break;
            }
        }


    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        Toast.makeText(ctx, "Anruf kommt onIncomingCallEnded", Toast.LENGTH_LONG).show();
        System.out.println("onIncomingCallEnded");
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        Toast.makeText(ctx, "Anruf kommt onOutgoingCallStarted", Toast.LENGTH_LONG).show();
        System.out.println("onOutgoingCallStarted");
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        Toast.makeText(ctx, "Anruf kommt onOutgoingCallEnded", Toast.LENGTH_LONG).show();
        System.out.println("onOutgoingCallEnded");
    }

    protected void onMissedCall(Context ctx, String number, Date start)
    {
        Toast.makeText(ctx, "Anruf kommt onMissedCall", Toast.LENGTH_LONG).show();
        System.out.println("onMissedCall");
    }
}
