package com.example.test.LoginAndProfile;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.test.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UsersProfileActivity extends AppCompatActivity {

    private TextView userName, userFullName, userCountry;
    private ImageView otherProfileImage;
    private Button friendBtn, unfriendBtn;

    private DatabaseReference UsersRef, FriendRequestRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserID, receiverUserID, CURRENT_STATE, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        userFullName = (TextView) findViewById(R.id.other_user_fullname);
        userName = (TextView) findViewById(R.id.other_user_username);
        userCountry = (TextView) findViewById(R.id.other_user_country);
        otherProfileImage = (ImageView) findViewById(R.id.other_user_profile_image);

        friendBtn = (Button) findViewById(R.id.friend_button);
        unfriendBtn = (Button) findViewById(R.id.unfriend_button);

        CURRENT_STATE = "not_friends";

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();

        receiverUserID = getIntent().getExtras().get("PostKey").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("friendrequest");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("friends");

        UsersRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String username = dataSnapshot.child("UserName").getValue().toString();
                    String fullname = dataSnapshot.child("FullName").getValue().toString();
                    String country = dataSnapshot.child("Country").getValue().toString();

                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(otherProfileImage);

                    userName.setText(username);
                    userFullName.setText(fullname);
                    userCountry.setText(country);

                    ButtonMaintenance();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        unfriendBtn.setVisibility(View.INVISIBLE);
        unfriendBtn.setEnabled(false);

        if(!senderUserID.equals(receiverUserID)){
            friendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendBtn.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }
                }
            });

        }else{
            unfriendBtn.setVisibility(View.INVISIBLE);
            friendBtn.setVisibility(View.VISIBLE);
        }
    }



    private void ButtonMaintenance() {
        FriendRequestRef.child(senderUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserID)){
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type")
                                    .getValue().toString();

                            if(request_type.equals("sent")){
                                CURRENT_STATE = "request_sent";
                                friendBtn.setTag("Cancel Friend Request");

                                unfriendBtn.setVisibility(View.INVISIBLE);
                                unfriendBtn.setEnabled(false);
                            }else if(request_type.equals("received")){
                                CURRENT_STATE = "request_received";
                                friendBtn.setText("Accept Friend Request");

                                unfriendBtn.setVisibility(View.VISIBLE);
                                unfriendBtn.setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        FriendsRef.child(senderUserID).child(receiverUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestRef.child(senderUserID).child(receiverUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                FriendRequestRef.child(receiverUserID).child(senderUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    friendBtn.setEnabled(true);
                                                                    CURRENT_STATE = "friends";
                                                                    friendBtn.setText("Unfriend User");

                                                                    unfriendBtn.setVisibility(View.INVISIBLE);
                                                                    unfriendBtn.setEnabled(false);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void SendFriendRequest() {
        FriendRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                friendBtn.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                friendBtn.setText("Cancel Friend Request");

                                                unfriendBtn.setVisibility(View.INVISIBLE);
                                                unfriendBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void CancelFriendRequest() {
        FriendRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                friendBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                friendBtn.setText("Add Friend");

                                                unfriendBtn.setVisibility(View.INVISIBLE);
                                                unfriendBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
