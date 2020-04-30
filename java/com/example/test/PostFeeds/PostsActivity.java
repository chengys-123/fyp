package com.example.test.PostFeeds;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.test.R;

public class PostsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posts_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frameLayoutPost, new PostsFragment(), "fragmentTag")
                .disallowAddToBackStack()
                .commit();
    }
}
