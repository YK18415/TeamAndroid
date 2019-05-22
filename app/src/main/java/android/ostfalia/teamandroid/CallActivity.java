package android.ostfalia.teamandroid;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CallActivity extends AppCompatActivity {

    private static String TAG = "CallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_betreuer);

        // Layout components:
        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnCallEnd = findViewById(R.id.btnCallEnd);

        btnCallEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });
    }

    private void endCall() {
        // TODO: With Receiver
    }



    public static boolean isCallActive(Context context){
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(manager.getMode() == AudioManager.MODE_IN_CALL || manager.getMode() == AudioManager.MODE_IN_COMMUNICATION){
            return true;
        }
        return false;
    }


}
