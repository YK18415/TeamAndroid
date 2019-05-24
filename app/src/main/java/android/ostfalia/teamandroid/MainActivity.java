package android.ostfalia.teamandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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

    private TextView textViewBetreuerName;
    private TextView textViewBetreuerPhonenumber;
    private TextView textViewBetreuerStreetNumber;
    private TextView textViewBetreuerPostcode;
    private TextView textViewBetreuerCity;

    private List<Contact> contactList = new ArrayList<>();
    SharedPreferences.Editor editor;
    SharedPreferences savedData;

    private static final int REQUEST_PHONE_CALL = 123; // TODO: Wofür ist das?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedData = getApplicationContext().getSharedPreferences("contactList", MODE_PRIVATE); //lesen
        editor = savedData.edit(); //schreiben

        // Validate, that user has allowed the Call-Permission before:
        this.validatePhoneCallPermission();
        // Validate, that the user has logged in before:
        this.validateFirstLogin();
        // initialize View
        this.initView();
        // set Content View
        this.setContent();
        // initialize Content
        this.initContent();
        // Default-Betreute:

//        contactList.add(new Contact("Max", "Mustermann", "01234567891011"));
//        contactList.add(new Contact("Hallo", "Duda", "12343212121"));


    }

    /**
     * Initialize View content
     */
    private void initView() {
        switch(this.role) {
            case BETREUER:
                this.spinnerContactList = findViewById(R.id.spinnerContactList);
                Button btnAddPerson = findViewById(R.id.btnAddPerson);
                ImageButton btnDeleteContact = findViewById(R.id.btnDeleteContact);
                this.textViewReceiver = findViewById(R.id.textViewReceiver);

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
                textViewBetreuerPhonenumber = findViewById(R.id.textViewBetreuerPhonenumber);
                textViewBetreuerStreetNumber = findViewById(R.id.textViewBetreuerStreetNumber);
                textViewBetreuerPostcode = findViewById(R.id.textViewBetreuerPostcode);
                textViewBetreuerCity = findViewById(R.id.textViewBetreuerCity);
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

    /**
     * Validate, that the user has logged in before
     */
    private void validateFirstLogin() {
        String role;
        SharedPreferences loginData = getApplicationContext().getSharedPreferences("logindata", MODE_PRIVATE); // For reading.;
        role = loginData.getString("role","");

        if(TextUtils.isEmpty(role)) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        if(role != null)
            this.setRole(role);

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
        this.saveContactList();
    }

    /**
     * Called by Android OS on Resume
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.loadContactList();
        this.initContent();
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

                    String buffer = contact.getFirstname()+" "+contact.getLastname();
                    textViewBetreuerName.setText(buffer);
                    textViewBetreuerPhonenumber.setText(contact.getTelephonenumber());

                    buffer = contact.getStreet()+" "+contact.getHousenumber();
                    textViewBetreuerStreetNumber.setText(buffer);

                    textViewBetreuerPostcode.setText(String.valueOf(contact.getPostcode()));
                    textViewBetreuerCity.setText(contact.getCity());
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
            textViewReceiver.setText(contactList.get(position).getTelephonenumber());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
                                textViewReceiver.setText(R.string.TextView_Receiver);
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

    private void call() {
        Intent intent = null;
        switch(this.role) {
            case BETREUER:  intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + textViewReceiver.getText()));                break;
            case BETREUTER: intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + textViewBetreuerPhonenumber.getText()));     break;
        }

        try {
            startActivity(intent);
        } catch(SecurityException se) {
            se.printStackTrace();
            Toast.makeText(MainActivity.this, "Es gab einen Fehler", Toast.LENGTH_SHORT).show();
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
     * Locks relogin possibility
     */
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Sie sind bereits eingeloggt", Toast.LENGTH_LONG).show();
    }

}
