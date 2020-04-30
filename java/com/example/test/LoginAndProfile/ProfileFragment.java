package com.example.test.LoginAndProfile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileFragment extends Fragment {
    String user;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private ProgressDialog loadingBar;

    ImageView profilePic, coverPhoto;
    TextView nameTv, countryTv;
    FloatingActionButton fab;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        nameTv = view.findViewById(R.id.nameTv);
        countryTv = view.findViewById(R.id.countryTv);
        profilePic = view.findViewById(R.id.profilePic);
        coverPhoto = view.findViewById(R.id.coverPhoto);
        fab = view.findViewById(R.id.fab);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        usersRef.child(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    {
                        if(dataSnapshot.hasChild("FullName")){
                            nameTv.setText(dataSnapshot.child("FullName").getValue().toString());
                        }
                        if (dataSnapshot.hasChild("profileimage")) {
                            String image = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.get().load(image).into(profilePic);
                        }
                        if(dataSnapshot.hasChild("Country")){
                            countryTv.setText(dataSnapshot.child("Country").getValue().toString());
                        }
                    }
                }else {
                    Intent create = new Intent(getContext(), SetupActivity.class);
                    create.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(create);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] options = {"Yes", "No"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Proceed to Edit Profile?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            Intent i = new Intent(getContext(), EditProfileActivity.class);
                            startActivity(i);
                        }
                    }
                });
                builder.create().show();
            }
        });
        return view;
    }

}


