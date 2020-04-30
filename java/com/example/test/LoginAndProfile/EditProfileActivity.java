package com.example.test.LoginAndProfile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.test.HomeActivity;
import com.example.test.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private Toolbar eToolBar;
    private EditText editusername, editfullname, editcountry;
    private Button updateProfileButton;
    private ImageView updateProfileImage, uploadCoverPhoto;

    private DatabaseReference updateUsersRefs;
    private StorageReference UserProfileImageRefs;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private ProgressDialog progressDialog;

    final static int gallerypick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        updateUsersRefs = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRefs = FirebaseStorage.getInstance().getReference().child("Profile Images");

        editusername = findViewById(R.id.editusername);
        editfullname =  findViewById(R.id.editfullname);
        editcountry =  findViewById(R.id.editcountry);
        updateProfileButton =  findViewById(R.id.update_profile_button);
        updateProfileImage =  findViewById(R.id.edit_profile_image);
        uploadCoverPhoto = findViewById(R.id.coverPhoto);
        progressDialog = new ProgressDialog(this);

        eToolBar = (Toolbar) findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(eToolBar);
        getSupportActionBar().setTitle("Edit Profile");

        updateUsersRefs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myprofileimage = dataSnapshot.child("profileimage").getValue().toString();
                    String myusername = dataSnapshot.child("UserName").getValue().toString();
                    String myfullname = dataSnapshot.child("FullName").getValue().toString();
                    String mycountry = dataSnapshot.child("Country").getValue().toString();
                    Picasso.get().load(myprofileimage).placeholder(R.drawable.profile).into(updateProfileImage);

                    editusername.setText(myusername);
                    editfullname.setText(myfullname);
                    editcountry.setText(mycountry);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editusername.getText().toString();
                String fullname = editfullname.getText().toString();
                String country = editcountry.getText().toString();

                if(TextUtils.isEmpty(username)){
                    Toast.makeText(EditProfileActivity.this, "Empty username", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(fullname)){
                    Toast.makeText(EditProfileActivity.this, "Empty Full name", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(country)){
                    Toast.makeText(EditProfileActivity.this, "Empty country", Toast.LENGTH_SHORT).show();
                }else{
                    UpdateAccountInfo(username, fullname, country);
                }
            }
        });

        updateProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallerypick );
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == gallerypick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                progressDialog.setTitle("Profile Image Loading");
                progressDialog.setMessage("Updating profile image...");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRefs.child(currentUserID + ".jpg"); //put inside firebase image file

                filePath.putFile(resultUri)
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(!task.isSuccessful()){
                                    throw task.getException();
                                }
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downUri = task.getResult();
                            Toast.makeText(EditProfileActivity.this, "Successfully stored to firebase", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = downUri.toString();
                            updateUsersRefs.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent selfIntent = new Intent(EditProfileActivity.this, EditProfileActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(EditProfileActivity.this, "Successfully stored to firebase", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }else{
                                                String message = task.getException().getMessage();
                                                Toast.makeText(EditProfileActivity.this, "Error occured" + message, Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
        }
    }

    private void UpdateAccountInfo(String username, String fullname, String country) {
        HashMap userMap = new HashMap();
        userMap.put("UserName", username);
        userMap.put("FullName", fullname);
        userMap.put("Country", country);
        updateUsersRefs.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    sendUserToProfileActivity();
                    Toast.makeText(EditProfileActivity.this, "Update successfully!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(EditProfileActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToProfileActivity() {
        Intent mainIntent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
