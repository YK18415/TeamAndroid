package android.ostfalia.teamandroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText editTextPassword;
    EditText editTextOwnPhonenumber;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final RadioButton radioButtonRoleBetreuer = findViewById(R.id.radioButton_Role_Betreuer);
        final RadioButton radioButtonRoleBetreuter = findViewById(R.id.radioButton_Role_Betreuter);
        final ImageButton imageButtonInfoLogin = findViewById(R.id.imageButtonInfoLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextOwnPhonenumber = findViewById(R.id.editTextOwnPhonenumber);


        editTextPassword.setVisibility(View.GONE);
        imageButtonInfoLogin.setVisibility(View.GONE);
        editTextOwnPhonenumber.setVisibility(View.GONE);
        imageButtonInfoLogin.setVisibility(View.GONE);

        radioButtonRoleBetreuter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPassword.setVisibility(View.VISIBLE);
                imageButtonInfoLogin.setVisibility(View.VISIBLE);
                editTextOwnPhonenumber.setVisibility(View.VISIBLE);

                imageButtonInfoLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopupInfo();
                    }
                });

            }
        });

        radioButtonRoleBetreuer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPassword.setVisibility(View.GONE);
                imageButtonInfoLogin.setVisibility(View.GONE);
                editTextOwnPhonenumber.setVisibility(View.GONE);
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

    /**
     * Shows information about the masterpassword and the phonenumber in a popup-menu.
     */
    private void showPopupInfo() {
        android.app.AlertDialog dialog;
        android.app.AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);
        imageDialog.setTitle("Informationen");
        LayoutInflater inflater = this.getLayoutInflater(); // Takes the xml-file and builds the View-Object from it. It is neccessary, because I have a custom-layout for the image.
        View view = inflater.inflate(R.layout.info_popup, null);

        imageDialog.setView(view);
        imageDialog.setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = imageDialog.create();
        dialog.show();
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
