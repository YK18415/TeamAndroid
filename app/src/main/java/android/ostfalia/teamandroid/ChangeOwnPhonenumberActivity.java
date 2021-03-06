package android.ostfalia.teamandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class ChangeOwnPhonenumberActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_own_phonenumber);

        EditText ownPhoneNumber = findViewById(R.id.editTextTelephonenumber);

        ownPhoneNumber.setText(getIntent().getStringExtra("defaultPhoneNumber"));

        Intent resultIntent = new Intent();
        resultIntent.putExtra("PHONENUMBER", "");
        setResult(RESULT_CANCELED, resultIntent);

        findViewById(R.id.btnStore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("PHONE_NUMBER", ((EditText)findViewById(R.id.editTextTelephonenumber)).getText().toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
