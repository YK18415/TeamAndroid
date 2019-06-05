package android.ostfalia.teamandroid;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This activity is used as a main menu from which several different actions can be taken
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {



    private boolean initialState = false;
    public static Role role;

    private Toolbar toolbar;
    private Spinner spinnerContactList;
    private TextView textViewReceiver;

    private LinearLayout contactInfo;
    private TextView street;
    private TextView houseNo;
    private TextView post;
    private TextView city;

    private TextView textViewBetreuerName;

    private List<Contact> contactList = new ArrayList<>();
    private SharedPreferences.Editor editor;
    private SharedPreferences savedData;
    private boolean isActivityActive;
    private boolean isThreadActive;

    private static final int REQUEST_PHONE_CALL = 123;
    private int counter = 0;
    private int secreteCounter = 0;
    private long startTime;
    private int editContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedData = getApplicationContext().getSharedPreferences("betreuapp", MODE_PRIVATE); //lesen
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
                ImageButton btnAddPerson = findViewById(R.id.imageButtonAddPerson);
                ImageButton btnEditPerson = findViewById(R.id.imageButtonEditPerson);
                ImageButton btnDeleteContact = findViewById(R.id.imageButtonDeleteContact);
                ImageButton btnImageInfoMain = findViewById(R.id.imageButtonInfoMain);

                this.spinnerContactList.setOnItemSelectedListener(this);
                btnAddPerson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addNewPerson();
                    }
                });
                btnEditPerson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editPerson();
                    }
                });

                btnDeleteContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteContact();
                    }
                });

                btnImageInfoMain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopupInfo();
                    }
                });
            break;

            case BETREUTER:
                textViewBetreuerName = findViewById(R.id.textViewBetreuerName);
                isActivityActive = true;
                isThreadActive = false;

                textViewBetreuerName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTime = System.currentTimeMillis();
                        isThreadActive = true;
                        counter++;
                        if(counter == 3) {
                            counter=0;
                            changeBetreuerContact();
                        }
                        // thread timer restart:
                        secreteCounter = 0;
                    }
                });
                startTime = System.currentTimeMillis();
                startSecretTimerChangeBetreuer();
            break;
        }
        this.toolbar = findViewById(R.id.toolbar);
        ImageButton btnCall = findViewById(R.id.imageButtonCall);

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call();
            }
        });
    }

    /**
     * Shows important information to the user (Betreuer) in an AlertDialog.
     */
    private void showPopupInfo() {
        android.app.AlertDialog dialog;
        android.app.AlertDialog.Builder imageDialog = new android.app.AlertDialog.Builder(this);
        imageDialog.setTitle("Informationen");
        LayoutInflater inflater = this.getLayoutInflater(); // Takes the xml-file and builds the View-Object from it. It is neccessary, because I have a custom-layout for the image.
        View view = inflater.inflate(R.layout.info_popup, null);

        imageDialog.setView(view);
        imageDialog.setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = imageDialog.create();
        dialog.show();
    }

    /**
     * Starts a timer that resets the amount of button presses the user has made to get into the secret menu
     */
    private void startSecretTimerChangeBetreuer() {

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

    /**
     * Opens a dialog that prompts the user to input the secret password that only the Betreuer should know.
     * If it is put in correctly, the user gets to choose between several options that should be kept out of
     * reach for the Betreuter.
     */
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
                SharedPreferences logindata = getApplicationContext().getSharedPreferences("betreuapp", MODE_PRIVATE); //lesen
                String passwordSaved = logindata.getString("password", "");
                String passwordInput = String.valueOf(input.getText());
                if(passwordInput.equals(passwordSaved)) {
                    dialog.dismiss();
                    openChoicePopup();
                   /* Intent intent = new Intent(MainActivity.this, NewContact.class);
                    intent.putExtra("CONTACT", contactList.get(0));
                    contactList.clear();
                    startActivityForResult(intent, 1);*/
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
     * Opens a dialog that let's the user choose between changing the Betreuer and changing the saved phone number
     * of the device
     */
    private void openChoicePopup() {
        android.app.AlertDialog dialog;
        final android.app.AlertDialog.Builder choiceDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        choiceDialog.setTitle("Wählen Sie aus:");

        choiceDialog.setPositiveButton("Betreuer ändern", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, NewContact.class);
                Contact editContact = contactList.get(0);
                intent.putExtra("firstName", editContact.getFirstname());
                intent.putExtra("lastName", editContact.getLastname());
                intent.putExtra("telephoneNumber", editContact.getTelephonenumber());
                intent.putExtra("street", editContact.getStreet());
                intent.putExtra("streetNumber", editContact.getHousenumber());
                intent.putExtra("postCode", editContact.getPostcode());
                intent.putExtra("city", editContact.getCity());
                intent.putExtra("actionbarText", "Betreuer ändern");
                startActivityForResult(intent, 4);
            }
        }).setNegativeButton("Eigene Nummer ändern", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, ChangeOwnPhonenumberActivity.class);
                intent.putExtra("defaultPhoneNumber", getApplicationContext().getSharedPreferences("betreuapp", MODE_PRIVATE).getString("PHONE_NUMBER", ""));
                startActivityForResult(intent, 2);
            }
        }).setNeutralButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = choiceDialog.create();
        dialog.show();
    }

    /**
     * Validate, that the user has logged in before
     */
    private Boolean validateFirstLogin() {
        String role;
        SharedPreferences loginData = getApplicationContext().getSharedPreferences("betreuapp", MODE_PRIVATE); // For reading.;
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
                    saveContactList();
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
                } else {
                    this.fillSpinnerInitial();
                }
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
        if(!this.initialState) {
            setContactText(contactList.get(position));
        }
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
                    "Ja",
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
                    "Nein",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * Sets some default texts for some of the textViews
     */
    public void setDefaultText(){
        //textViewReceiver.setText(R.string.TextView_Receiver);
        //street, houseNo, post, city
        street.setText("Straße: -");
        houseNo.setText("Hausnummer: -");
        post.setText("Plz: -");
        city.setText("Ort: -");
    }

    /**
     * Sets all textViews texts to show the given  contacts information
     * @param contact The given contact
     */
    public void setContactText(Contact contact){

        if(role==Role.BETREUTER) {
            String buffer = contact.getFirstname() + " " + contact.getLastname();

            textViewBetreuerName.setText(buffer);
        }

        if(!this.initialState) {
            textViewReceiver.setText("Tel: " + contact.getTelephonenumber());
            street.setText(contact.getStreet() != null ? getString(R.string.contactStreet) + " " + contact.getStreet() : getString(R.string.contactNoStreet));
            houseNo.setText(contact.getHousenumber() != null ? getString(R.string.contactHouseNo) + " " + contact.getHousenumber() : getString(R.string.contactNoHouseNo));
            post.setText(contact.getPostcode() != null ? getString(R.string.contactPostal) + " " + contact.getPostcode() : getString(R.string.contactNoPostal));
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

        PhoneCallReceiver.appCall=true;
        final HandlerThread handlerThread = new HandlerThread("CallToBackgroundThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        final Handler handler = new Handler(looper);

        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, CallActivity.class);
                //String role;
                SharedPreferences settings = getApplicationContext().getSharedPreferences("betreuapp", MODE_PRIVATE); // For reading.;
                //role = settings.getString("role","");
                switch(settings.getString("role","")) {
                    case "Betreuer":
                        role = Role.BETREUER;
                        break;
                    case "Betreuter":
                        role = Role.BETREUTER;
                        break;
                }
                startActivity(intent);
                handlerThread.quit();
            }
        }, 1500);

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        switch(this.role) {
            case BETREUER:  new BackgroundTask().execute(contactList.get(spinnerContactList.getSelectedItemPosition()).getTelephonenumber());               break;
            case BETREUTER:  new BackgroundTask().execute(contactList.get(0).getTelephonenumber());               break;
        }
    }

    /**
     * AsyncTask: start Call and printStackTrace in case of failure.
     */
    private class BackgroundTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String ... strings) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + strings[0]));
            PhoneCallReceiver.partnerNumber = PhoneCallReceiver.formatPhoneNumber(strings[0]);
            try {
                startActivity(intent);
            } catch (SecurityException se) {
                se.printStackTrace();
                Toast.makeText(MainActivity.this, "Es gab einen Fehler beim Anrufversuch.", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    /**
     * Open new Activity - NewContact - to create a new contact
     */
    private void addNewPerson() {
        Intent intent = new Intent(MainActivity.this, NewContact.class);
        startActivityForResult(intent, 1);
    }

    /**
     * Open new Activity - NewContact - to edit a contact
     */
    private void editPerson(){
        editContact = spinnerContactList.getSelectedItemPosition();

        Intent intent = new Intent(MainActivity.this, NewContact.class);

        Contact editedContact = contactList.get(editContact);

        intent.putExtra("firstName", editedContact.getFirstname());
        intent.putExtra("lastName", editedContact.getLastname());
        intent.putExtra("telephoneNumber", editedContact.getTelephonenumber());
        intent.putExtra("street", editedContact.getStreet());
        intent.putExtra("streetNumber", editedContact.getHousenumber());
        intent.putExtra("postCode", editedContact.getPostcode());
        intent.putExtra("city", editedContact.getCity());
        intent.putExtra("actionbarText", "Kontakt editieren");

        startActivityForResult(intent, 3);
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

        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                String newPhonenumber = Objects.requireNonNull(data.getExtras()).getString("PHONE_NUMBER", "");
                editor.putString("PHONE_NUMBER", newPhonenumber);
                editor.commit();
            }
        }

        if(requestCode==3){
            if (resultCode == RESULT_OK) {
                Contact newContact = (Contact) Objects.requireNonNull(data.getExtras()).getSerializable("CONTACT");

                // add the newContact, who was created by the user, to the benutzerList:
                if(newContact != null) {
                    contactList.set(editContact, newContact);
                    //contactList.add(newContact);
                }
            }
        }

        if(requestCode==4){
            if (resultCode == RESULT_OK) {
                Contact newContact = (Contact) Objects.requireNonNull(data.getExtras()).getSerializable("CONTACT");

                // add the newContact, who was created by the user, to the benutzerList:
                if(newContact != null) {
                    contactList.clear();
                    contactList.add(newContact);
                }
            }
        }
        saveContactList();
    }

    /**
     * Minimize App when pressed Back
     */
    @Override
    public void onBackPressed() {
        minimizeApp();
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
