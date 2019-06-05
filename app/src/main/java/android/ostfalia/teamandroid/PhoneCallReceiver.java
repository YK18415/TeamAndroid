package android.ostfalia.teamandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;

import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class PhoneCallReceiver extends BroadcastReceiver {

    private static String savedNumber;
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming;
    private static Date callStartTime;
    public static boolean appCall=false;

    public static String partnerNumber = null;

    /**
     * Formats a given phone number to our conventions
     * @param number The given phone number
     * @return The formatted phone number
     */
    public static String formatPhoneNumber(String number){
        return number.length()==0?number:number.charAt(0)=='0'?"+49" + number.substring(1):number;
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

            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

    }

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up

    /**
     * Is called when the state of the phone call is changed
     * @param context The current context
     * @param state The state of the call
     * @param number The phone number of the partner
     */
    private void onCallStateChanged(Context context, int state, String number) {
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

    /**
     * Is called when the phone is called
     * @param ctx The current context
     * @param number The phone number of the partner
     * @param start When the phone call started
     */
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {

    }

    /**
     * Is called when an incoming call is answered
     * @param ctx The current context
     * @param number The phone number of the partner
     * @param start When the phone call started
     */
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        SharedPreferences settings = ctx.getSharedPreferences(ctx.getString(R.string.SharedPreferencesName), MODE_PRIVATE);
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
                
                appCall = true;
                
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

                Intent intent = new Intent(ctx, CallActivity.class);

                ctx.startActivity(intent);
                break;
            }
        }


    }

    /**
     * Is called when an ongoing call, in which this phone was called, has ended
     * @param ctx The current context
     * @param number The phone number of the partner
     * @param start When the phone call started
     * @param end When the phone call ended
     */
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        betreuerGoBackMainActivity(ctx);
    }

    /**
     * Is called when this phone calls someone
     * @param ctx The current context
     * @param number The phone number of the partner
     * @param start When the phone call started
     */
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {

    }

    /**
     * Is called when an ongoing call, in which this phone has called, has ended
     * @param ctx The current context
     * @param number The phone number of the partner
     * @param start When the phone call started
     */
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        betreuerGoBackMainActivity(ctx);
    }

    /**
     * Is called, when the user misses a call
     * @param ctx The current context
     * @param number The phone number of the partner
     * @param start When the phone call started
     */
    protected void onMissedCall(Context ctx, String number, Date start)
    {

    }

    /**
     * Sends the Betreuer back to the main activity
     * @param ctx The current context
     */
    private void betreuerGoBackMainActivity(Context ctx){
        if(appCall) {
            if (MainActivity.role == Role.BETREUER) {
                Intent goBackIntent = new Intent(ctx, MainActivity.class);
                ctx.startActivity(goBackIntent);
            }
        }
        appCall=false;
    }
}
