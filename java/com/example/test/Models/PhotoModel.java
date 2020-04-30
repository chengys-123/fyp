package com.example.test.Models;

import com.example.test.Utils.Constants;

public class PhotoModel {
    String photoReference;
    int width,height;

    public String getPhotoReference() {
        String api = "https://maps.googleapis.com/maps/api/place/photo?key="+
                Constants.API_KEY+"&maxheight="+getHeight()+"&maxwidth="+getWidth()+"&photoreference="+photoReference;
        return api;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public int getWidth() {
        return (width<400?width:400);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return (height<600?height:600);
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
