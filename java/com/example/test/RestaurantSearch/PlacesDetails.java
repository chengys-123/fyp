package com.example.test.RestaurantSearch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.test.PostFeeds.AddPostActivity;
import com.example.test.R;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class PlacesDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collapse_toolbar);

        Toolbar collapseToolbar = (Toolbar) findViewById(R.id.toolbarCollapse);
        setSupportActionBar(collapseToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        init();
    }

    private GeoDataClient geoDataClient;
    private ImageView placeImage, openstats, closestats, price;
    public TextView placeAddress, placeName, openOrNo, textPrice;
    private List<PlacePhotoMetadata> photosDataList;
    private int currentPhotoIndex = 0;
    private Button postButton;

    private void init() {
        final String id = getIntent().getStringExtra("id");
        final String name = getIntent().getStringExtra("name");
        String address = getIntent().getStringExtra("address");
        boolean isOpen = getIntent().getBooleanExtra("isOpen", true);
        int pricing = getIntent().getIntExtra("price_level", 0);
        geoDataClient = Places.getGeoDataClient(this);
        getPhotoMetadata(id);

        //post button
        postButton = findViewById(R.id.detailsPublish);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postIntent = new Intent(PlacesDetails.this, AddPostActivity.class);
                postIntent.putExtra("id", id);
                postIntent.putExtra("name", name);
                startActivity(postIntent);
            }
        });

        //place name
        placeName = (TextView) findViewById(R.id.placeNameText);
        placeName.setText(name);

        //place address
        placeAddress = (TextView) findViewById(R.id.addressText);
        placeAddress.setText(address);

        //place image
        placeImage = findViewById(R.id.scrollableImage);

        //opening now or not
        openstats = findViewById(R.id.openStatus);
        closestats = findViewById(R.id.closedStatus);
        if (!isOpen) {
            openOrNo = findViewById(R.id.statusText);
            openOrNo.setText("Closed Now");
            openstats.setVisibility(View.INVISIBLE);
        }else{
            openOrNo = findViewById(R.id.statusText);
            openOrNo.setText("Open Now");
            closestats.setVisibility(View.INVISIBLE);
        }

        //place price level
        price = findViewById(R.id.priceimage);
        textPrice = findViewById(R.id.pricetext);
        if(pricing == 0){
            textPrice.setText("No pricing available");
        }else  if(pricing == 1){
            textPrice.setText("$ \n Cheap");
        }else if (pricing == 2){
            textPrice.setText("$$ \n Slightly Expensive");
        }else if (pricing == 3){
            textPrice.setText("$$$ \n Mid Expensive");
        }else {
            textPrice.setText("$$$$ \n Very Expensive");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void getPhotoMetadata(String placeId) {

        final Task<PlacePhotoMetadataResponse> photoResponse =
                geoDataClient.getPlacePhotos(placeId);

        photoResponse.addOnCompleteListener
                (new OnCompleteListener<PlacePhotoMetadataResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                        currentPhotoIndex = 0;
                        photosDataList = new ArrayList<>();
                        PlacePhotoMetadataResponse photos = task.getResult();
                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

                        for(PlacePhotoMetadata photoMetadata : photoMetadataBuffer){
                            photosDataList.add(photoMetadataBuffer.get(1).freeze());
                        }

                        photoMetadataBuffer.release();
                        displayPhoto();
                    }
                });
    }

    private void getPhoto(PlacePhotoMetadata photoMetadata){
        Task<PlacePhotoResponse> photoResponse = geoDataClient.getPhoto(photoMetadata);
        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                PlacePhotoResponse photo = task.getResult();
                Bitmap photoBitmap = photo.getBitmap();

                placeImage.invalidate();
                placeImage.setImageBitmap(photoBitmap);
            }
        });
    }

    private void displayPhoto(){
        if(photosDataList.isEmpty() || currentPhotoIndex > photosDataList.size() - 1){
            return;
        }
        getPhoto(photosDataList.get(currentPhotoIndex));
    }
}