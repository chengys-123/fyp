package com.example.test.PostFeeds;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.DialogPreference;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.RestaurantSearch.ExploreFrag;
import com.example.test.RestaurantSearch.MainActivity;
import com.example.test.RestaurantSearch.PlacesDetails;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    EditText postTitleText, postDescText;
    ImageView postImageView;
    Button uploadPostBtn;
    ProgressDialog progressDialog;
    TextView placeText;

    Uri image_uri = null;

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    String mUid, fullname, profileimage, placeid, currentplace;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermissions;
    String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    fullname = "" + ds.child("FullName").getValue();
                    profileimage = "" + ds.child("profileimage").getValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //init permission arrays
        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        postTitleText = findViewById(R.id.postTitle);
        postDescText = findViewById(R.id.postDesc);
        placeText = findViewById(R.id.currentPlace);
        postImageView = findViewById(R.id.postImage);
        uploadPostBtn = findViewById(R.id.uploadBtn);
        progressDialog = new ProgressDialog(this);

        placeid = getIntent().getStringExtra("id");
        currentplace = getIntent().getStringExtra("name");
        placeText =  findViewById(R.id.currentPlace);
        placeText.setText("At: "  + currentplace);

        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker();
            }
        });

        uploadPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = postTitleText.getText().toString().trim();
                String description = postDescText.getText().toString().trim();
                String places = placeid;
                String currentplacename = placeText.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter post title.", Toast.LENGTH_SHORT).show();
                    return;
                }if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter a status.", Toast.LENGTH_SHORT).show();
                }

                if(image_uri == null){
                    uploadData(title, description, "noImage", currentplacename, places);
                }else{
                    uploadData(title, description, String.valueOf(image_uri), currentplacename, places);
                }
            }
        });

    }


    private void uploadData(final String title, final String description, String uri, final String currentplace, final String placeId) {
        progressDialog.setMessage("Uploading Post...");
        progressDialog.show();

        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filepath = "Posts/" + "post_" + timeStamp;

        if(!uri.equals("noImage")){
            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filepath);
            ref.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //image uploaded to firebase storage, now get its url
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());

                    String downloadUri = uriTask.getResult().toString();

                    if(uriTask.isSuccessful()){
                        HashMap<Object, String> hashMap = new HashMap<>();
                        //put post info
                        hashMap.put("uid", mUid);
                        hashMap.put("pid", timeStamp);
                        hashMap.put("placeid", placeId);
                        hashMap.put("currentPlaceName", currentplace);
                        hashMap.put("postname", fullname);
                        hashMap.put("image", profileimage);
                        hashMap.put("posttitle", title);
                        hashMap.put("postdescription", description);
                        hashMap.put("postimage", downloadUri);
                        hashMap.put("posttime", timeStamp);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

                        ref.child(timeStamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddPostActivity.this, "Post Uploaded Successfully.", Toast.LENGTH_SHORT).show();

                                        String[] option = {"Yes, publish another post.", "No, back to previous page."};
                                        AlertDialog.Builder builder = new AlertDialog.Builder(AddPostActivity.this);
                                        builder.setTitle("Publish Another Post?");
                                        builder.setItems(option, new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int what){
                                                if(what == 0){
                                                    //reset views
                                                    postTitleText.setText("");
                                                    postDescText.setText("");
                                                    postImageView.setImageURI(null);
                                                    image_uri = null;
                                                }else if(what == 1){
                                                    Intent backIntent = new Intent(AddPostActivity.this, MainActivity.class);
                                                    backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(backIntent);
                                                }
                                            }
                                        });
                                        builder.create().show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed to upload image
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", mUid);
            hashMap.put("pid", timeStamp);
            hashMap.put("placeid", placeId);
            hashMap.put("currentPlaceName", currentplace);
            hashMap.put("postname", fullname);
            hashMap.put("image", profileimage);
            hashMap.put("posttitle", title);
            hashMap.put("postdescription", description);
            hashMap.put("postimage", "noImage");
            hashMap.put("posttime", timeStamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post Uploaded Successfully.", Toast.LENGTH_SHORT).show();

                            String[] option = {"Yes, publish another post.", "No, back to restaurant page."};
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddPostActivity.this);
                            builder.setTitle("Publish Another Post?");
                            builder.setItems(option, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int what){
                                    if(what == 0){
                                        //reset views
                                        postTitleText.setText("");
                                        postDescText.setText("");
                                        postImageView.setImageURI(null);
                                        image_uri = null;
                                    }else if(what == 1){
                                        Intent backIntent = new Intent(AddPostActivity.this, MainActivity.class);
                                        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(backIntent);
                                    }
                                }
                            });
                            builder.create().show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void imagePicker() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               if(which == 0){
                   if(!checkCameraPermission()){
                       requestCameraPermission();
                   }else{
                       pickFromCamera();
                   }
               }if(which == 1){
                   //gallery
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp desc");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean resultstorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return resultstorage;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean resultcamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean resultcamera1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return resultcamera && resultcamera1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else{
                        Toast.makeText(this, "Camera and Storage Permission Required.", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if(grantResults.length > 0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }else{
                        Toast.makeText(this, "Storage Permission Required.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                postImageView.setImageURI(image_uri);

            }else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                postImageView.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
