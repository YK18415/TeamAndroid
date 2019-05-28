package android.ostfalia.teamandroid;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private enum Role {BETREUER, BETREUTER}

    private boolean initialState = false;
    private Role role;

    private Toolbar toolbar;
    private Spinner spinnerContactList;
    private TextView textViewReceiver;

    LinearLayout contactInfo;
    TextView street;
    TextView houseNo;
    TextView post;
    TextView city;

    private TextView textViewBetreuerName;
    /*private TextView textViewBetreuerPhonenumber;
    private TextView textViewBetreuerStreetNumber;
    private TextView textViewBetreuerPostcode;
    private TextView textViewBetreuerCity;*/

    private List<Contact> contactList = new ArrayList<>();
    SharedPreferences.Editor editor;
    SharedPreferences savedData;
    private boolean isActivityActive;
    private boolean isThreadActive;

    private static final int REQUEST_PHONE_CALL = 123; // TODO: Wofür ist das?
    int counter = 0;
    int secreteCounter = 0;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedData = getApplicationContext().getSharedPreferences("contactList", MODE_PRIVATE); //lesen
        editor = savedData.edit(); //schreiben

        contactInfo=findViewById(R.id.contactInfo);
        street = findViewById(R.id.street);
        houseNo = findViewById(R.id.houseNo);
        post = findViewById(R.id.post);
        city = findViewById(R.id.city);

        // Validate, that user has allowed the Call-Permission before:
        this.validatePhoneCallPermission();
        // Validate, that the user has logged in before:
        if(!this.validateFirstLogin()) {
            // initialize View
            this.initView();
            // set Content View
            this.setContent();
            // initialize Content
            this.initContent();
        }

        setDefaultText();
        // Default-Betreute:

//        contactList.add(new Contact("Max", "Mustermann", "01234567891011"));
//        contactList.add(new Contact("Hallo", "Duda", "12343212121"));
    }

    /**
     * Initialize View content
     */
    private void initView() {
        this.textViewReceiver = findViewById(R.id.textViewReceiver);
        switch(this.role) {
            case BETREUER:
                this.spinnerContactList = findViewById(R.id.spinnerContactList);
                Button btnAddPerson = findViewById(R.id.btnAddPerson);
                ImageButton btnDeleteContact = findViewById(R.id.btnDeleteContact);

                this.spinnerContactList.setOnItemSelectedListener(this);
                btnAddPerson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addNewPerson();
                    }
                });

                btnDeleteContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteContact();
                    }
                });
            break;

            case BETREUTER:
                textViewBetreuerName = findViewById(R.id.textViewBetreuerName);
                /*textViewBetreuerPhonenumber = findViewById(R.id.textViewBetreuerPhonenumber);
                textViewBetreuerStreetNumber = findViewById(R.id.textViewBetreuerStreetNumber);
                textViewBetreuerPostcode = findViewById(R.id.textViewBetreuerPostcode);
                textViewBetreuerCity = findViewById(R.id.textViewBetreuerCity);*/
                isActivityActive = true;
                isThreadActive = false;

                textViewBetreuerName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTime = System.currentTimeMillis();
                        isThreadActive = true;
                        counter++;
                        System.out.println(counter);
                        if(counter == 3) {
                            counter=0;
                            changeBetreuerContact();
                        }
                        // thread timer restart:
                        secreteCounter = 0;
                    }
                });
                startTime = System.currentTimeMillis();
                startSecretTimerChangeBetreuer(isActivityActive);
            break;
        }
        this.toolbar = findViewById(R.id.toolbar);
        Button btnCall = findViewById(R.id.btnCall);

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call();
            }
        });
    }

    private void startSecretTimerChangeBetreuer(final Boolean isActivityActive) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(isActivityActive) {
                    while (isThreadActive) {
                        long millis = System.currentTimeMillis() - startTime;
                        secreteCounter += millis;
                        startTime = System.currentTimeMillis();
                        if(secreteCounter  > 2000){
                            counter = 0;
                            isThreadActive = false;
                        }
                    }
                }
            }
        });
        thread.start();
    }

    private void editContact() {
        // TODO
    }

    private void changeBetreuerContact() {
        android.app.AlertDialog dialog;
        final android.app.AlertDialog.Builder passwordDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        passwordDialog.setTitle("Es muss das Sicherheitspasswort des Betreuers angegeben werden.");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        passwordDialog.setView(input);

        passwordDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences logindata = getApplicationContext().getSharedPreferences("logindata", MODE_PRIVATE); //lesen
                String passwordSaved = logindata.getString("password", "");
                String passwordInput = String.valueOf(input.getText());
                if(passwordInput.equals(passwordSaved)) {
                    Intent intent = new Intent(MainActivity.this, NewContact.class);
                    intent.putExtra("CONTACT", contactList.get(0));
                    contactList.clear();
                    startActivityForResult(intent, 1);
                } else {
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "Sie haben das falsche Password eingegeben.", Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog = passwordDialog.create();
        dialog.show();
    }

    /**
     * Validate, that the user has logged in before
     */
    private Boolean validateFirstLogin() {
        String role;
        SharedPreferences loginData = getApplicationContext().getSharedPreferences("logindata", MODE_PRIVATE); // For reading.;
        role = loginData.getString("role","");

        if(TextUtils.isEmpty(role)) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            //startActivityForResult(intent, 1);
            startActivity(intent);
            return true;
        }else {
            this.setRole(role);
            Bundle bundle = getIntent().getExtras();
            if(bundle != null) {
                Contact contact = (Contact) bundle.get("BETREUER_CONTACT");

                if(this.role == Role.BETREUTER && contact != null) {
                    contactList.add(contact);
                }
            }

            return false;
        }/* else if (role.equals(getResources().getString(R.string.loginActivity_Role_Betreuer))) {
            Intent intent = new Intent(MainActivity.this, MainActivityBetreuer.class);
            startActivity(intent);
        }*/

    }

    /**
     * Set Enum for switching Content View
     * @param role String (Betreuer / Betreuter)
     */
    private void setRole(String role) {
        if(role.equals("Betreuer"))
            this.role = Role.BETREUER;
        else if(role.equals("Betreuter"))
            this.role = Role.BETREUTER;
    }

    /**
     * Set correct Content in Main Activity
     */
    private void setContent() {
        switch(this.role) {
            case BETREUER:
                findViewById(R.id.content_betreuer).setVisibility(View.VISIBLE);
                findViewById(R.id.content_betreuter).setVisibility(View.GONE);
                this.toolbar.setTitle(R.string.label_overview_betreuer);
            break;

            case BETREUTER:
                findViewById(R.id.content_betreuter).setVisibility(View.VISIBLE);
                findViewById(R.id.content_betreuer).setVisibility(View.GONE);
                this.toolbar.setTitle(R.string.label_overview_betreuter);
            break;
        }
    }

    /**
     * Validate, rather user has allowed the Call-Permission before
     */
    private void validatePhoneCallPermission() {
        if(checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
        }
    }

    /**
     * Called by Android OS on Pause
     */
    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
        this.saveContactList();
    }

    /**
     * Called by Android OS on Resume
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(!this.validateFirstLogin()) {
            this.loadContactList();
            this.initContent();
        }

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

    /**
     * Set Data in Content View
     */
    private void initContent() {
        switch (this.role) {
            case BETREUER:
                if(!contactList.isEmpty()) {
                    String[] names = this.convertContactListToNamesArray();
                    this.setSpinnerAdapter(names);
                } else
                    this.fillSpinnerInitial();
            break;
            case BETREUTER:
                if(!contactList.isEmpty()) {
                    Contact contact = contactList.get(0);

                    setContactText(contact);
                }
            break;
        }
    }

    /**
     * Save contactList as json
     */
    private void saveContactList() {
        Gson gson = new Gson();
        String listAsString = gson.toJson(contactList);
        editor.putString("contactList", listAsString);

        editor.commit();
    }

    /**
     * Load contactList from json
     */
    private void loadContactList() {
        if(savedData.contains("contactList")) {
            String contactSpinnerList = savedData.getString("contactList", "");
            Gson gson = new Gson();
            Contact[] contactToArray = gson.fromJson(contactSpinnerList, Contact[].class);

            for (int idx = 0; idx < contactToArray.length; idx++) {
                if (idx >= contactList.size()) {
                    contactList.add(contactToArray[idx]);
                }
            }
        }
    }

    /**
     * Create List for showing by Spinner
     * @return String[] names
     */
    private String[] convertContactListToNamesArray() {
        String[] names = new String[contactList.size()];

        int person_idx = 0;
        for(Contact contact : contactList) {
            names[person_idx++] = contact.getFirstname() + " " + contact.getLastname();
        }
        return names;
    }

    /**
     * Fill Spinner with saved data
     * @param names Names-Array displayed in Spinner
     */
    private void setSpinnerAdapter(String[] names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerContactList.setAdapter(adapter);
        this.initialState = false;
    }

    /**
     * Fill Spinner with initial data
     */
    private void fillSpinnerInitial() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinnerContactInit, android.R.layout.simple_spinner_dropdown_item);
        spinnerContactList.setAdapter(adapter);
        this.initialState = true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(!this.initialState)
            setContactText(contactList.get(position));
        /*
        if(!this.initialState) {
            textViewReceiver.setText(contactList.get(position).getTelephonenumber());
            street.setText(contactList.get(position).getStreet() != null ? getString(R.string.contactStreet) + " " + contactList.get(position).getStreet() : getString(R.string.contactNoStreet));
            houseNo.setText(contactList.get(position).getHousenumber() != 0 ? getString(R.string.contactHouseNo) + " " + contactList.get(position).getHousenumber() : getString(R.string.contactNoHouseNo));
            post.setText(contactList.get(position).getPostcode() != 0 ? getString(R.string.contactPostal) + " " + contactList.get(position).getPostcode() : getString(R.string.contactNoPostal));
            city.setText(contactList.get(position).getCity() != null ? getString(R.string.contactCity) + " " + contactList.get(position).getCity() : getString(R.string.contactNoCity));
        }*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        contactInfo.setVisibility(View.GONE);
    }

    /**
     * Delete onClickListener - Deletes selected contact
     */
    private void deleteContact() {
        if(!this.contactList.isEmpty()) {
            Dialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Ausgewählten Kontakt löschen?");
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            contactList.remove(spinnerContactList.getSelectedItemPosition());
                            setSpinnerAdapter(convertContactListToNamesArray());
                            if (contactList.isEmpty()) {
                                setDefaultText();
                                fillSpinnerInitial();
                            }
                        }
                    });

            builder.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            dialog = builder.create();
            dialog.show();
        }
    }

    public void setDefaultText(){
        //textViewReceiver.setText(R.string.TextView_Receiver);
        //street, houseNo, post, city
        street.setText("Straße: -");
        houseNo.setText("Hausnummer: -");
        post.setText("Plz: -");
        city.setText("Ort: -");
    }

    public void setContactText(Contact contact){

        if(role==Role.BETREUTER) {
            String buffer = contact.getFirstname() + " " + contact.getLastname();

            textViewBetreuerName.setText(buffer);
        }
        /*
        textViewReceiver.setText(contact.getTelephonenumber());
        //textViewBetreuerPhonenumber.setText(contact.getTelephonenumber());

        buffer = contact.getStreet()+" "+contact.getHousenumber();
        street.setText(contact.getStreet());
        houseNo.setText(contact.getHousenumber());
        //textViewBetreuerStreetNumber.setText(buffer);

        //textViewBetreuerPostcode.setText(String.valueOf(contact.getPostcode()));
        //textViewBetreuerCity.setText(contact.getCity());
        post.setText(String.valueOf(contact.getPostcode()));
        city.setText(String.valueOf(contact.getCity()));
        */
        if(!this.initialState) {
            textViewReceiver.setText("Telefonnummer: " + contact.getTelephonenumber());
            street.setText(contact.getStreet() != null ? getString(R.string.contactStreet) + " " + contact.getStreet() : getString(R.string.contactNoStreet));
            houseNo.setText(contact.getHousenumber() != 0 ? getString(R.string.contactHouseNo) + " " + contact.getHousenumber() : getString(R.string.contactNoHouseNo));
            post.setText(contact.getPostcode() != 0 ? getString(R.string.contactPostal) + " " + contact.getPostcode() : getString(R.string.contactNoPostal));
            city.setText(contact.getCity() != null ? getString(R.string.contactCity) + " " + contact.getCity() : getString(R.string.contactNoCity));
        }
    }

    /**
     * Handle outgoing call, start & Default Call Screen to Background
     * Replace actual call() Method
     */
    private void call() {
        if(contactList.size() == 0 ) {
            Toast.makeText(this, "Sie haben keinen Kontakt ausgewählt.", Toast.LENGTH_LONG).show();
            return;
        }

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

        switch(this.role) {
            case BETREUER:  new BackgroundTask().execute(contactList.get(spinnerContactList.getSelectedItemPosition()).getTelephonenumber());               break;
            case BETREUTER:  new BackgroundTask().execute(contactList.get(0).getTelephonenumber());               break;
            //case BETREUTER: new BackgroundTask().execute(textViewBetreuerPhonenumber.getText().toString());    break;
        }
    }

    /**
     * AsyncTask: start Call and printStackTrace in case of failure.
     */
    private class BackgroundTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String ... strings) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + strings[0]));
            PhoneCallReceiver.INCOMING_NUMBER=strings[0];
            try {
                startActivity(intent);
            } catch (SecurityException se) {
                se.printStackTrace();
                Toast.makeText(MainActivity.this, "Es gab einen Fehler beim Anrufversuch.", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

/*    private void chooseReceiver() {
       // final List<String> contactListString = new ArrayList<String>();

        final CharSequence[] contactListString = new CharSequence[contactList.size()];
        for (int i = 0; i < contactList.size(); i++) {
            contactListString[i] = contactList.get(i).getFirstname() + " " + contactList.get(i).getLastname();
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

    /**
     * Open new Activity - NewContact
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
                if(newContact != null && !contactList.contains(newContact)) {
                    contactList.add(newContact);
                }
            }
        }
    }

    /**
     * Minimize App when pressed Back
     */
    @Override
    public void onBackPressed() {
        //Issue #14
        minimizeApp();

        //finish(); //Close App hard, no rocovery!!!
    }


    /**
     * Shows a popup window that explains how to change the "Betreuer" on the device of the "Betreuter"
     */
    public void showChangeBetrInfo(){

        android.app.AlertDialog dialog;
        android.app.AlertDialog.Builder helpDialog = new android.app.AlertDialog.Builder(this);
        helpDialog.setTitle("Betreuer ändern");
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.change_betreuer_help, null);

        helpDialog.setView(view);
        helpDialog.setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = helpDialog.create();
        dialog.show();
    }

    /**
     * Minimize a Activity and go to startscreen
     */
    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
