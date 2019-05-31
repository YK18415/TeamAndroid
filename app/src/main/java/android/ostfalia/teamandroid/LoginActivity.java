package android.ostfalia.teamandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText editTextPassword;
    EditText editTextOwnPhonenumber;
    //todo it is possible for the betreuter to save his own number with a 0 or a +49 at the beginning. this influences filenames on the firebase servers and we do not currently handle this.
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final RadioButton radioButtonRoleBetreuer = findViewById(R.id.radioButton_Role_Betreuer);
        final RadioButton radioButtonRoleBetreuter = findViewById(R.id.radioButton_Role_Betreuter);
        final ImageButton imageButtonInfoPassword = findViewById(R.id.imageButtonInfoPassword);
        final ImageButton imageButtonInfoOwnPhonenumber = findViewById(R.id.imageButtonInfoOwnPhonenumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextOwnPhonenumber = findViewById(R.id.editTextOwnPhonenumber);


        editTextPassword.setVisibility(View.GONE);
        imageButtonInfoPassword.setVisibility(View.GONE);
        editTextOwnPhonenumber.setVisibility(View.GONE);
        imageButtonInfoOwnPhonenumber.setVisibility(View.GONE);

        radioButtonRoleBetreuter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPassword.setVisibility(View.VISIBLE);
                imageButtonInfoPassword.setVisibility(View.VISIBLE);
                editTextOwnPhonenumber.setVisibility(View.VISIBLE);
                imageButtonInfoOwnPhonenumber.setVisibility(View.VISIBLE);

                imageButtonInfoPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: add infos about masterpassword
                    }
                });
                imageButtonInfoOwnPhonenumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: add infos about phone number usage
                    }
                });
            }
        });

        radioButtonRoleBetreuer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPassword.setVisibility(View.GONE);
                imageButtonInfoPassword.setVisibility(View.GONE);
                editTextOwnPhonenumber.setVisibility(View.GONE);
                imageButtonInfoOwnPhonenumber.setVisibility(View.GONE);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(radioButtonRoleBetreuer, radioButtonRoleBetreuter);
            }
        });

    }

    /**Login onClickListener - checks which role is chosen and logs in
     *
     * @param radioButtonRoleBetreuer View radioButtonBetreuer
     * @param radioButtonRoleBetreuter View radioButtonBetreuter
     */
    private void login(RadioButton radioButtonRoleBetreuer, RadioButton radioButtonRoleBetreuter) {
        //Storage:
        SharedPreferences.Editor editor = getSharedPreferences("betreuapp", MODE_PRIVATE).edit();
        if(radioButtonRoleBetreuer.isChecked()) {

            editor.putString("role", String.valueOf(radioButtonRoleBetreuer.getText()));
            Intent intent = new Intent(LoginActivity.this, MainActivity.class); // TODO: Refactoren
            startActivity(intent);
        } else if (!TextUtils.isEmpty(editTextPassword.getText()) && !TextUtils.isEmpty(editTextOwnPhonenumber.getText())) {
            editor.putString("role", String.valueOf(radioButtonRoleBetreuter.getText()));
            editor.putString("password", String.valueOf(editTextPassword.getText()));
            editor.putString("PHONE_NUMBER", editTextOwnPhonenumber.getText().toString());

            // Add Betreuer:
            Intent intent = new Intent(LoginActivity.this, NewContact.class);
            startActivityForResult(intent, 1);
        }
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Contact newContact = (Contact) Objects.requireNonNull(data.getExtras()).getSerializable("CONTACT");

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);// TODO: Refactoren
                intent.putExtra("BETREUER_CONTACT", newContact);
                startActivity(intent);
            }
        }
    }
}
