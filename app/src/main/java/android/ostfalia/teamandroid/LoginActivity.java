package android.ostfalia.teamandroid;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final RadioButton radioButtonRoleBetreuer = findViewById(R.id.radioButton_Role_Betreuer);
        final RadioButton radioButtonRoleBetreuter = findViewById(R.id.radioButton_Role_Betreuter);
        final EditText editTextPhonenumber = findViewById(R.id.editText_Phonenumber);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(radioButtonRoleBetreuer, radioButtonRoleBetreuter, editTextPhonenumber);
            }
        });
    }

    private void login(RadioButton radioButtonRoleBetreuer, RadioButton radioButtonRoleBetreuter, EditText editTextPhonenumber) {

    }

}
