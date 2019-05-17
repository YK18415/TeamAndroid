package android.ostfalia.teamandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final RadioButton radioButtonRoleBetreuer = findViewById(R.id.radioButton_Role_Betreuer);
        final RadioButton radioButtonRoleBetreuter = findViewById(R.id.radioButton_Role_Betreuter);
        editTextPassword = findViewById(R.id.editTextPassword);
        final TextView textViewPassword =  findViewById(R.id.textViewPassword);

        editTextPassword.setVisibility(View.GONE);
        textViewPassword.setVisibility(View.GONE);

        radioButtonRoleBetreuter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPassword.setVisibility(View.VISIBLE);
                textViewPassword.setVisibility(View.VISIBLE);
            }
        });

        radioButtonRoleBetreuer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPassword.setVisibility(View.GONE);
                textViewPassword.setVisibility(View.GONE);
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

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);// TODO: Refactoren
                startActivity(intent);
            }
        }
        editor.commit();
    }

}
