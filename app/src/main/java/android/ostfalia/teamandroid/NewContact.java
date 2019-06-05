package android.ostfalia.teamandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewContact extends AppCompatActivity {

    Contact newContact;

    String firstname;
    String lastname;
    String telephonenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnSave = findViewById(R.id.btnStore);
        final EditText editTextFirstname = findViewById(R.id.editTextFirstName);
        final EditText editTextLastname = findViewById(R.id.editTextLastName);
        final EditText editTextTelephonenumber = findViewById(R.id.editTextTelephonenumber);
        final EditText editTextStreet = findViewById(R.id.editTextStreet);
        final EditText editTextStreetnumber = findViewById(R.id.editTextStreetnumber);
        final EditText editTextPostcode = findViewById(R.id.editTextPostcode);
        final EditText editTextCity = findViewById(R.id.editTextCity);

        Intent intent = getIntent();

        editTextFirstname.setText(intent.getStringExtra("firstName"));
        editTextLastname.setText(intent.getStringExtra("lastName"));
        editTextTelephonenumber.setText(intent.getStringExtra("telephoneNumber"));
        editTextStreet.setText(intent.getStringExtra("street"));
        editTextStreetnumber.setText(intent.getStringExtra("streetNumber"));
        editTextPostcode.setText(intent.getStringExtra("postCode"));
        editTextCity.setText(intent.getStringExtra("city"));

        String actionbarText = intent.getStringExtra("actionbarText");
        if(actionbarText!=null){
            setTitle(actionbarText);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               firstname = editTextFirstname.getText().toString();
               lastname = editTextLastname.getText().toString();
               telephonenumber = editTextTelephonenumber.getText().toString();

                if(firstname.equals("") || lastname.equals("") || telephonenumber.equals("")) {
                    Toast.makeText(NewContact.this, "Sie haben nicht alle Pflichtfelder ausgefüllt.", Toast.LENGTH_LONG).show();
                } else {
                    addNewContact();

                    if(!TextUtils.isEmpty(editTextStreet.getText())) {
                        newContact.setStreet(editTextStreet.getText().toString());
                    }
                    if(!TextUtils.isEmpty(editTextStreetnumber.getText())) {
                        newContact.setHousenumber(editTextStreetnumber.getText().toString());
                    }
                    if(!TextUtils.isEmpty(editTextPostcode.getText())) {
                        newContact.setPostcode(editTextPostcode.getText().toString());
                    }
                    if(!TextUtils.isEmpty(editTextCity.getText())) {
                        newContact.setCity(editTextCity.getText().toString());
                    }

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("CONTACT", newContact);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // ends current activity
                }
            }
        });
    }

    /**
     * Creates contact with the current values of firstname, lastname, telephonenumber and saves it in newContact
     */
    private void addNewContact() {
        newContact = new Contact(firstname,lastname, telephonenumber);
    }
}
