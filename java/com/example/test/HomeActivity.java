package com.example.test;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.test.LoginAndProfile.FindUsersActivity;
import com.example.test.LoginAndProfile.LoginActivity;
import com.example.test.LoginAndProfile.ProfileActivity;
import com.example.test.PostFeeds.PostsActivity;
import com.example.test.RestaurantSearch.MainActivity;
import com.example.test.RestaurantSearch.SearchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.hitomi.cmlibrary.CircleMenu;
import com.hitomi.cmlibrary.OnMenuSelectedListener;
import com.hitomi.cmlibrary.OnMenuStatusChangeListener;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private CircleMenu circleMenu;
    private int indexItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        circleMenu = (CircleMenu) findViewById(R.id.circle_menu);
        circleMenu.setMainMenu(Color.parseColor("#CDCDCD"), R.drawable.menu_icon, R.drawable.menu_close)
                .addSubMenu(Color.parseColor("#30A400"), R.drawable.ic_profile)
                .addSubMenu(Color.parseColor("#FF4B32"), R.drawable.ic_place)
                .addSubMenu(Color.parseColor("#21A400"), R.drawable.ic_search)
                .addSubMenu(Color.parseColor("#FFCBA4"), R.drawable.ic_menu_map)
                .addSubMenu(Color.parseColor("#123456"), R.drawable.ic_home)
                .setOnMenuSelectedListener(new OnMenuSelectedListener() {
                    @Override
                    public void onMenuSelected(int index) {
                            indexItem = index;
                    }

                }).setOnMenuStatusChangeListener(new OnMenuStatusChangeListener() {
            @Override
            public void onMenuOpened() {}

            @Override
            public void onMenuClosed() {
                switch (indexItem) {
                    case 0:
                        indexItem = -1;
                        Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
                        break;
                    case 1:
                        indexItem = -1;
                        Intent exploreIntent = new Intent(HomeActivity.this, MainActivity.class);
                        startActivity(exploreIntent);
                        break;
                    case 2:
                        indexItem = -1;
                        Intent searchIntent = new Intent(HomeActivity.this, PostsActivity.class);
                        startActivity(searchIntent);
                        break;
                    case 3:
                        indexItem = -1;
                        Intent findIntent = new Intent(HomeActivity.this, SearchActivity.class);
                        startActivity(findIntent);
                        break;
                    case 4:
                        indexItem = -1;
                        mAuth.signOut();
                        Intent logoutIntent = new Intent(HomeActivity.this, LoginActivity.class);
                        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(logoutIntent);
                        finish();
                }
            }
        });

    }
}
