package android.ostfalia.teamandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String role;
    Button btnContactList;
    Button btnAddPerson;
    Button btnCall;
    TextView textViewReceiver;

    List<Betreuter> betreuterList = new ArrayList<Betreuter>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Validate, rather user has done login before:
        validateFirstLogin();

        btnContactList = findViewById(R.id.btnContactList);
        btnAddPerson = findViewById(R.id.btnAddPerson);
        btnCall = findViewById(R.id.btnCall);
        textViewReceiver = findViewById(R.id.textViewReceiver);

        // Default-Betreute:
        betreuterList.add(new Betreuter("Max", "Mustermann", "01234567891011"));
        betreuterList.add(new Betreuter("Hallo", "Duda", "12343212121"));

        btnContactList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseReceiver();
            }
        });
    }

    private void validateFirstLogin() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("logindata", MODE_PRIVATE); // For reading.;
        role = settings.getString("role","");
        if(TextUtils.isEmpty(role)) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void chooseReceiver() {
       // final List<String> contactListString = new ArrayList<String>();

        final CharSequence[] contactListString = new CharSequence[betreuterList.size()];
        for (int i = 0; i < betreuterList.size(); i++) {
            contactListString[i] = betreuterList.get(i).getFirstname() + " " + betreuterList.get(i).getLastname();
        }

        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Wählen Sie eine Person.");
        builder.setSingleChoiceItems(contactListString, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textViewReceiver.setText(contactListString[which]);
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Sie sind bereits eingeloggt", Toast.LENGTH_LONG).show();
    }
}
