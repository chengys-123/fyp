package com.example.test.Utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.test.Models.PhotoModel;
import com.example.test.Models.PlaceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class PlaceJSONParser {
    JSONObject result;
    PlaceModel model = new PlaceModel();
    Context context;
    private static String address;

    public PlaceJSONParser(JSONObject result, Context context) {
        this.result = result;
        this.context = context;
    }

    public PlaceModel getPlaceModel()
    {
        try {
            JSONObject location = result.getJSONObject("geometry").getJSONObject("location");

            model.setLatitude(location.getDouble("lat"));
            model.setLongitude(location.getDouble("lng"));

            model.setId(result.getString("place_id"));
            model.setName(result.getString("name"));

            if(result.has("opening_hours") && result.getJSONObject("opening_hours").has("open_now"))
                model.setOpen(result.getJSONObject("opening_hours").getBoolean("open_now"));
            else
                model.setOpen(false);

            ArrayList<PhotoModel> photoList = new ArrayList<>();
            if(result.has("photos")) {
                JSONArray photoArray = result.getJSONArray("photos");
                for (int i = 0; i < photoArray.length(); i++) {
                    JSONObject photoObj = photoArray.getJSONObject(i);
                    PhotoModel m = new PhotoModel();
                    m.setPhotoReference(photoObj.getString("photo_reference"));
                    m.setHeight(photoObj.getInt("height") / 2);
                    m.setWidth(photoObj.getInt("width") / 2);
                    photoList.add(m);
                }
            }
            model.setPhotoList(photoList);

            //phone number
            if(result.has("formatted_phone_number"))
                model.setPhoneNum(result.getString("formatted_phone_number"));
            else
                model.setPhoneNum("No contact available");

            //price level
            if(result.has("price_level"))
                model.setPricelevel(result.getInt("price_level"));
            else
                model.setPricelevel(0);

            //rating
            if(result.has("rating"))
                model.setRating(result.getDouble("rating"));
            else
                model.setRating(0.0);

            //address
            if(result.has("vicinity"))
                model.setVicinity(result.getString("vicinity"));
            else
                model.setVicinity("Could not fetch address");
            model.setAddress(model.getVicinity());

            //types/categories
            if(result.has("types")) {
                List<String> pTags = new ArrayList<>();
                JSONArray tagArray = result.getJSONArray("types");
                for(int x = 0; x < tagArray.length();x++)
                    pTags.add(tagArray.getString(x));
                model.setCategoryTags(pTags);
            }
            else
                model.setCategoryTags(new ArrayList<String>(0));
        } catch (JSONException e) {
            Log.e("error",e.getMessage());
        }
        return model;
    }

    //thread communication
    public static class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    address = bundle.getString("address");
                    break;
                default:
                    address = "Could not fetch address";
            }
        }
    }
}
