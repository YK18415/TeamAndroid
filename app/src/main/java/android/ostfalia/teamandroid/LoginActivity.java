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
        editTextPassword = findViewById(R.id.editTextPassword);

        editTextPassword.setVisibility(View.GONE);
        imageButtonInfoPassword.setVisibility(View.GONE);

        radioButtonRoleBetreuter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPassword.setVisibility(View.VISIBLE);
                imageButtonInfoPassword.setVisibility(View.VISIBLE);
                imageButtonInfoPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: add infos about masterpassword
                    }
                });
            }
        });

        radioButtonRoleBetreuer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPassword.setVisibility(View.GONE);
                imageButtonInfoPassword.setVisibility(View.GONE);
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
        SharedPreferences.Editor editor = getSharedPreferences("logindata", MODE_PRIVATE).edit();
        if(radioButtonRoleBetreuer.isChecked()) {

            editor.putString("role", String.valueOf(radioButtonRoleBetreuer.getText()));
            Intent intent = new Intent(LoginActivity.this, MainActivity.class); // TODO: Refactoren
            startActivity(intent);
        } else {
            if(!TextUtils.isEmpty(editTextPassword.getText())) {
                editor.putString("role", String.valueOf(radioButtonRoleBetreuter.getText()));
                editor.putString("password", String.valueOf(editTextPassword.getText()));

                // Add Betreuer:
                Intent intent = new Intent(LoginActivity.this, NewContact.class);
                startActivityForResult(intent, 1);
            }
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
