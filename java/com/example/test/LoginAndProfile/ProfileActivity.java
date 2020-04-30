package com.example.test.LoginAndProfile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.test.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frameLayoutProfile, new ProfileFragment(), "fragmentTag")
                .disallowAddToBackStack()
                .commit();
    }
}
