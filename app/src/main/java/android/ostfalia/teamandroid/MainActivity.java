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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinnerContactList;
    Button btnAddPerson;
    Button btnCall;
    TextView textViewReceiver;

    List<Contact> betreuterList = new ArrayList<Contact>();
    SharedPreferences.Editor editor;
    SharedPreferences savedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedData = getApplicationContext().getSharedPreferences("PEOPLE", MODE_PRIVATE); //lesen
        editor = savedData.edit(); //schreiben

        // Validate, rather user has done login before:
        validateFirstLogin();

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








//        this.fillSpinner();

/*        btnContactList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseReceiver();
            }
        });
*/  }



    private void validateFirstLogin() {
        String role;
        SharedPreferences settings = getApplicationContext().getSharedPreferences("logindata", MODE_PRIVATE); // For reading.;
        role = settings.getString("role","");
        if(TextUtils.isEmpty(role)) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
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
        super.onResume();

        this.loadContactList();
        String[] names = this.convertPersonListToNamesArray();
        this.setSpinnerAdapter(names);
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
/*            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinnerContactInit, android.R.layout.simple_spinner_dropdown_item);
            int numberElements = adapter.getCount();
            for(int position = 0; position < numberElements; position++) {
                personList.add(new Person(adapter.getItem(position).toString()));

            }
*/        }
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

    private void setSpinnerAdapter(String[] names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerContactList.setAdapter(adapter);
    }

    private void fillSpinnerInitial() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinnerContactInit, android.R.layout.simple_spinner_dropdown_item);
        spinnerContactList.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //TODO Listener vom Spinner
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    private void addNewPerson() {
        Intent intent = new Intent(MainActivity.this, NewContact.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // TODO: Handle erstellter Contact.
            }
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Sie sind bereits eingeloggt", Toast.LENGTH_LONG).show();
    }

}
