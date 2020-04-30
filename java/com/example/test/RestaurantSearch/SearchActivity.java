package com.example.test.RestaurantSearch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.test.R;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frameLayout, new SearchFrag(), "fragmentTag")
                .disallowAddToBackStack()
                .commit();
    }
}
