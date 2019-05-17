package android.ostfalia.teamandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        EditText editTextLastname = findViewById(R.id.editTextLastName);
        final EditText editTexeTelephonenumber = findViewById(R.id.editTextTelephonenumber);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               firstname = editTextFirstname.getText().toString();
               lastname = editTexeTelephonenumber.getText().toString();
               telephonenumber = editTexeTelephonenumber.getText().toString();

                if(firstname.equals("") || lastname.equals("") || telephonenumber.equals("")) {
                    Toast.makeText(NewContact.this, "Sie haben nicht alle Pflichtfelder ausgefüllt.", Toast.LENGTH_LONG).show();
                } else {
                    addNewContact();
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
