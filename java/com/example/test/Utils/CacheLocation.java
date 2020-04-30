package com.example.test.Utils;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class CacheLocation {
    Context context;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public CacheLocation(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("Location_Preference", Activity.MODE_PRIVATE);
    }

    public boolean saveLocation(double lat, double lng)
    {
        editor = preferences.edit();
        editor.putString("lat",String.valueOf(lat));
        editor.putString("lng",String.valueOf(lng));

        return editor.commit();
    }

    public double getLat()
    {
        return Double.parseDouble(preferences.getString("lat","0"));
    }

    public double getLng()
    {
        return Double.parseDouble(preferences.getString("lng","0"));
    }


    public boolean saveRadius(int radius)
    {
        editor = preferences.edit();
        editor.putInt("radius",radius);
        return editor.commit();
    }


    public int getRadius(){
        return preferences.getInt("radius",10000);
    }
}
