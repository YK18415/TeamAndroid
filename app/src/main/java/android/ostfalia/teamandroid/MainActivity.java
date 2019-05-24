package android.ostfalia.teamandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinnerContactList;
    Button btnAddPerson;
    Button btnCall;
    TextView textViewReceiver;

    List<Contact> betreuterList = new ArrayList<>();
    SharedPreferences.Editor editor;
    SharedPreferences savedData;

    private static final int REQUEST_PHONE_CALL = 123; // TODO: Wofür ist das?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedData = getApplicationContext().getSharedPreferences("PEOPLE", MODE_PRIVATE); //lesen
        editor = savedData.edit(); //schreiben

        // Validate, that the user has logged in before:
        validateFirstLogin();
        // Validate, that user has allowed the Call-Permission before:
        validatePhoneCallPermission();

        spinnerContactList = findViewById(R.id.spinnerContactList);
        spinnerContactList.setOnItemSelectedListener(this);
        btnAddPerson = findViewById(R.id.btnAddPerson);
        btnCall = findViewById(R.id.btnCall);
        textViewReceiver = findViewById(R.id.textViewReceiver);

        // Default-Betreute:
        betreuterList.add(new Contact("Max", "Mustermann", "01234567891011"));
        betreuterList.add(new Contact("Hallo", "Duda", "12343212121"));

        btnAddPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewPerson();
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call();
            }
        });
    }

    /**Validate, that the user has logged in before
     *
     */
    private void validateFirstLogin() {
        String role;
        SharedPreferences settings = getApplicationContext().getSharedPreferences("logindata", MODE_PRIVATE); // For reading.;
        role = settings.getString("role","");
        if(TextUtils.isEmpty(role)) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (role.equals(getResources().getString(R.string.loginActivity_Role_Betreuer))) {
            Intent intent = new Intent(MainActivity.this, MainActivityBetreuer.class);
            startActivity(intent);
        }
    }

    /**Validate, rather user has allowed the Call-Permission before
     *
     */
    private void validatePhoneCallPermission() {
        if(checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
        }
    }

        /**Called by Android OS on Pause
         *
         */
        @Override
        protected void onPause() {
            super.onPause();
            this.saveContactList();
        }

        /**Called by Android OS on Resume
         *
         */
        @Override
        protected void onResume() {
            //SharedPreferences settings = getApplicationContext().getSharedPreferences("emailmessagedetails", MODE_PRIVATE); // For reading.
            super.onResume();

        this.loadContactList();
        String[] names = this.convertPersonListToNamesArray();
        this.setSpinnerAdapter(names);

       /* Boolean b = settings.getBoolean("imagePreferance", false);
        if(b) {
            Intent intent = new Intent(MainActivity.this, CallActivity.class); // TODO: Change that with Enum.
            String role;
            savedData = getApplicationContext().getSharedPreferences("logindata", MODE_PRIVATE); // For reading.;
            role = savedData.getString("role","");
            intent.putExtra("role", role);
            intent.putExtra("IS_PAUSED", true);
            startActivity(intent);
        }*/
    }

    /**Saving personList as json
     *
     */
    private void saveContactList() {
        Gson gson = new Gson();
        String listAsString = gson.toJson(betreuterList);
        editor.putString("contactList", listAsString);

        editor.commit();
    }

    /**Load personList from json if possible
     * Otherwise Load personList from xml
     *
     */
    private void loadContactList() {
        if(savedData.contains("contactList")) {
            String personSpinnerList = savedData.getString("contactList", "");
            Gson gson = new Gson();
            Contact contactToArray[] = gson.fromJson(personSpinnerList, Contact[].class);

            for (int idx = 0; idx < contactToArray.length; idx++) {
                if (idx >= betreuterList.size()) {
                    betreuterList.add(contactToArray[idx]);
                }
            }
        } else {
            this.fillSpinnerInitial();
        }
    }

    /**Create List for showing by Spinner
     *
     * @return String[] names
     */
    private String[] convertPersonListToNamesArray() {
        String[] names = new String[betreuterList.size()];

        int person_idx = 0;
        for(Contact contact : betreuterList) {
            names[person_idx++] = contact.getFirstname() + " " + contact.getLastname();
        }
        return names;
    }

    /**Fill Spinner with saved data
     *
     * @param names Names-Array for chosing in Spinner
     */
    private void setSpinnerAdapter(String[] names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerContactList.setAdapter(adapter);
    }

    /**Fill Spinner with initial data if nothing's saved
     *
     */
    private void fillSpinnerInitial() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinnerContactInit, android.R.layout.simple_spinner_dropdown_item);
        spinnerContactList.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        textViewReceiver.setText(betreuterList.get(position).getTelephonenumber());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Handle outgoing call, start & Default Call Screen to Background
     * Replace actual call() Method
     */
    private void call() {
        final HandlerThread handlerThread = new HandlerThread("CallToBackgroundThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        final Handler handler = new Handler(looper);

        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, CallActivity.class); // TODO: Change that with Enum.
                String role;
                SharedPreferences settings = getApplicationContext().getSharedPreferences("logindata", MODE_PRIVATE); // For reading.;
                role = settings.getString("role","");
                intent.putExtra("role", role);
                startActivity(intent);
                handlerThread.quit();
            }
        }, 1000);

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        new BackgroundTask().execute(textViewReceiver.getText().toString());
    }

    /**
     * AsyncTask: start Call and printStackTrace in case of failure.
     */
    private class BackgroundTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String ... strings) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + strings[0]));
            try {
                startActivity(intent);
            } catch(SecurityException se) {
                se.printStackTrace();
                Toast.makeText(MainActivity.this, "Es gab einen Fehler beim Anrufversuch.", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

    }

/*    private void chooseReceiver() {
       // final List<String> contactListString = new ArrayList<String>();

        final CharSequence[] contactListString = new CharSequence[betreuterList.size()];
        for (int i = 0; i < betreuterList.size(); i++) {
            contactListString[i] = betreuterList.get(i).getFirstname() + " " + betreuterList.get(i).getLastname();
        }

        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Wählen Sie eine Person.");
        // TODO: Dismissal sofort!
        builder.setSingleChoiceItems(contactListString, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textViewReceiver.setText(contactListString[which]);
            }
        });
        dialog = builder.create();
        dialog.show();
    }
*/

    /**Open new Activity - NewContact
     *
     */
    private void addNewPerson() {
        Intent intent = new Intent(MainActivity.this, NewContact.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Contact newContact = (Contact) Objects.requireNonNull(data.getExtras()).getSerializable("CONTACT");

                // add the newContact, who was created by the user, to the benutzerList:
                if(newContact != null && !betreuterList.contains(newContact)) {
                    betreuterList.add(newContact);
                }
            }
        }
    }

    /**Locks relogin possibility
     *
     */
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Sie sind bereits eingeloggt", Toast.LENGTH_LONG).show();
    }

}
