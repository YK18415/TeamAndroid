package android.ostfalia.teamandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NewContact extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.activity_new_contact);
    }
}