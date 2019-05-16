package android.ostfalia.teamandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
        } else {
            editor.putString("role", String.valueOf(radioButtonRoleBetreuter.getText()));
        }
        editor.commit();
    }

}
