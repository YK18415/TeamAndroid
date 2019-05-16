package android.ostfalia.teamandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Validate, rather user has done login before:
        SharedPreferences settings = getApplicationContext().getSharedPreferences("logindata", MODE_PRIVATE); // For reading.;
        role = settings.getString("role","");
        if(TextUtils.isEmpty(role)) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

    /*    Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });*/
    }
}
