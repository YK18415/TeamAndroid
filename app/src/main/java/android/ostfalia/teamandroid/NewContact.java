package android.ostfalia.teamandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.Serializable;

public class NewContact extends AppCompatActivity {

    Contact newContact;

    String firstname;
    String lastname;
    String telephonenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);

        Button btnSave = findViewById(R.id.btnStore);
        final EditText editTextFirstname = findViewById(R.id.editTextFirstName);
        final EditText editTextLastname = findViewById(R.id.editTextLastName);
        final EditText editTexeTelephonenumber = findViewById(R.id.editTextTelephonenumber);
        final EditText editTextStreet = findViewById(R.id.editTextStreet);
        final EditText editTextStreetnumber = findViewById(R.id.editTextStreetnumber);
        final EditText editTextPostcode = findViewById(R.id.editTextPostcode);
        final EditText editTextCity = findViewById(R.id.editTextCity);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               firstname = editTextFirstname.getText().toString();
               lastname = editTextLastname.getText().toString();
               telephonenumber = editTexeTelephonenumber.getText().toString();

                if(firstname.equals("") || lastname.equals("") || telephonenumber.equals("")) {
                    Toast.makeText(NewContact.this, "Sie haben nicht alle Pflichtfelder ausgefüllt.", Toast.LENGTH_LONG).show();
                } else {
                    addNewContact();

                    // TODO: Refactoren.
                    if(!TextUtils.isEmpty(editTextStreet.getText())) {
                        newContact.setStreet(editTextStreet.getText().toString());
                    }
                    if(!TextUtils.isEmpty(editTextStreetnumber.getText())) {
                        newContact.setHousenumber(Integer.parseInt(editTextStreetnumber.getText().toString()));
                    }
                    if(!TextUtils.isEmpty(editTextPostcode.getText())) {
                        newContact.setPostcode(Integer.parseInt(editTextPostcode.getText().toString()));
                    }
                    if(!TextUtils.isEmpty(editTextCity.getText())) {
                        newContact.setCity(editTextCity.getText().toString());
                    }

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("CONTACT", (Serializable) newContact);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // ends current activity
                }
            }
        });
    }

    private void addNewContact() {
        newContact = new Contact(firstname,lastname, telephonenumber);
    }
}
