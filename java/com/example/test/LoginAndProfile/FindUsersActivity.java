package com.example.test.LoginAndProfile;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.test.Models.FindUsers;
import com.example.test.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton searchBtn;
    private EditText searchInputText;

    private RecyclerView searchResultsList;

    private DatabaseReference allUsersDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        allUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Users");

        searchResultsList = (RecyclerView) findViewById(R.id.search_results);
        searchResultsList.setHasFixedSize(true);
        searchResultsList.setLayoutManager(new LinearLayoutManager(this));

        searchBtn = (ImageButton) findViewById(R.id.search_users_btn);
        searchInputText = (EditText) findViewById(R.id.search_users_box);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBox = searchInputText.getText().toString();
                searchUsers(searchBox);
            }
        });
    }


    private void searchUsers(String searchBox)
    {
        Toast.makeText(this, "Searching user...", Toast.LENGTH_SHORT).show();

        FirebaseRecyclerOptions<FindUsers> options = new FirebaseRecyclerOptions.Builder<FindUsers>().
                setQuery(allUsersDatabase.orderByChild("FullName").startAt(searchBox)
                        .endAt(searchBox + "\uf88f"), FindUsers.class).build();
        FirebaseRecyclerAdapter<FindUsers, FindUsersActivity.FindUsersViewHolder> adapter
                = new FirebaseRecyclerAdapter<FindUsers, FindUsersActivity.FindUsersViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder( FindUsersViewHolder holder, int position,
                                             FindUsers model) {
                final String PostKey = getRef(position).getKey();
                holder.username.setText("Username: " + model.getUserName());
                holder.fullname.setText("Full name: " + model.getFullName());
                holder.country.setText("Country: " + model.getCountry());
                Picasso.get().load(model.getProfileImage()).into(holder.profileimage);

                //change the intent here, to view the person profile and then allows add friends etc
                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent findUsersIntent = new Intent(FindUsersActivity.this, UsersProfileActivity.class);
                        findUsersIntent.putExtra("PostKey", PostKey);
                        startActivity(findUsersIntent);
                    }
                });
            }
            @NonNull
            @Override
            public FindUsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).
                        inflate(R.layout.all_users_display_layout, viewGroup, false);

                FindUsersViewHolder viewHolder = new FindUsersViewHolder(view);
                return viewHolder;
            }
        };
        searchResultsList.setAdapter(adapter);
        adapter.startListening();
    }


    //the holder to display search results
    public static class FindUsersViewHolder extends RecyclerView.ViewHolder {
        TextView username, fullname, country;
        ImageView profileimage;

        public FindUsersViewHolder(@NonNull View ItemView){
            super(ItemView);
            username = ItemView.findViewById(R.id.profile_username);
            fullname = ItemView.findViewById(R.id.profile_fullname);
            country = ItemView.findViewById(R.id.profile_country);
            profileimage = ItemView.findViewById(R.id.all_users_profile_image);
        }
    }
}
