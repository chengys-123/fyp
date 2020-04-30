package com.example.test.RestaurantSearch;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.test.Adapters.StaggeredRecyclerViewAdapter;
import com.example.test.Models.ButtonModel;
import com.example.test.Models.PlaceModel;
import com.example.test.R;
import com.example.test.Utils.CacheLocation;
import com.example.test.Utils.Constants;
import com.example.test.Utils.PlaceJSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import mehdi.sakout.fancybuttons.FancyButton;


public class ExploreFrag extends Fragment {

    LinearLayout buttonContainer;
    ArrayList<ButtonModel> buttonsModelList = new ArrayList<>();
    CacheLocation cacheLocation;
    ArrayList<PlaceModel> placeList = new ArrayList<>();
    RecyclerView recyclerView;

    private StaggeredRecyclerViewAdapter adapter;
    private static final String TAG = "Explore";

    public ExploreFrag() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        buttonContainer = getView().findViewById(R.id.exploreHLL);

        cacheLocation = new CacheLocation(getContext());
        recyclerView = getView().findViewById(R.id.exploreRecyclerView);
        initButtons();
        addFancyButtons();

        new LoadPlaces(buttonsModelList.get(0).getPlaceParam()).execute();
        initRecycler();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private void initRecycler() {
        adapter = new StaggeredRecyclerViewAdapter(getContext(),placeList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(25);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    private void initButtons() {
//        Restaurant
        ButtonModel model = new ButtonModel();
        model.setName("Restaurant");
        model.setIconID(R.drawable.restaurant);
        model.setPlaceParam("restaurant");
        model.setBorderHexCode("#F44336");
        model.setFocusHexCode("#EF9A9A");
        buttonsModelList.add(model);

//        Bakery
        model = new ButtonModel();
        model.setName("Bakery");
        model.setIconID(R.drawable.bakery);
        model.setPlaceParam("bakery");
        model.setBorderHexCode("#9C27B0");
        model.setFocusHexCode("#CE93D8");
        buttonsModelList.add(model);

//        Bar
        model = new ButtonModel();
        model.setName("Bar");
        model.setIconID(R.drawable.bar);
        model.setPlaceParam("bar");
        model.setBorderHexCode("#4CAF50");
        model.setFocusHexCode("#A5D6A7");
        buttonsModelList.add(model);

//        Cafe
        model = new ButtonModel();
        model.setName("Cafe");
        model.setIconID(R.drawable.cafe);
        model.setPlaceParam("cafe");
        model.setBorderHexCode("#827717");
        model.setFocusHexCode("#C0CA33");
        buttonsModelList.add(model);
    }

    //function for button list
    private void addFancyButtons() {

        for(final ButtonModel model: buttonsModelList)
        {
            FancyButton button = new FancyButton(getContext());
            button.setText(model.getName());
            button.setBackgroundColor(Color.parseColor("#ffffff"));
            button.setFocusBackgroundColor(Color.parseColor(model.getFocusHexCode()));
            button.setTextSize(14);
            button.setRadius(70);
            button.setIconResource(model.getIconID());
            button.setIconPosition(FancyButton.POSITION_LEFT);
            button.setFontIconSize(10);
            button.setBorderWidth(1);
            button.setBorderColor(Color.parseColor(model.getBorderHexCode()));
            button.setTextColor(Color.parseColor(model.getBorderHexCode()));

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    placeList.clear();
                    new LoadPlaces(model.getPlaceParam()).execute();
                }
            });

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10,10,10,10);
            buttonContainer.addView(button,layoutParams);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    private class LoadPlaces extends AsyncTask<String,String,String>{
        String placeType;

        public LoadPlaces(String placeType) {
            this.placeType = placeType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            adapter.notifyDataSetChanged();
            recyclerView.invalidate();
        }

        @Override
        protected String doInBackground(String... strings) {
            String request = Constants.PLACE_SEARCH_API;
            request += "?key=" + Constants.API_KEY;
            request += "&location=" + cacheLocation.getLat() + ","+cacheLocation.getLng();
            request += "&radius=" + String.valueOf(cacheLocation.getRadius());
            request += "&type=" + placeType;

            Log.i(TAG,request);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, request,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //If we are getting success from server
                            Log.i(TAG,response);
                            try {
                                JSONObject object = new JSONObject(response);
                                JSONArray array = object.getJSONArray("results");
                                if(array.length() == 0)
                                {
                                    Toast.makeText(getContext(),object.getString("error_message"),Toast.LENGTH_LONG).show();
                                    return;
                                }
                                for(int i = 0;i < array.length(); i++)
                                {
                                    placeList.add(new PlaceJSONParser(array.getJSONObject(i),getContext()).getPlaceModel());
                                    adapter.notifyDataSetChanged();
                                }

                                Log.e(TAG,"count : " + placeList.size());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            NetworkResponse networkResponse = error.networkResponse;
                            Log.i(TAG, new String(error.networkResponse.data));
                            if(networkResponse != null && networkResponse.data != null)
                            {

                            }
                        }
                    }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    //Adding parameters to request
                    params.put("location",cacheLocation.getLat()+","+cacheLocation.getLng());
                    params.put("type",placeType);
                    params.put("radius",String.valueOf(cacheLocation.getRadius()));
                    return params;
                }
            };

            //Adding the string request to the queue
            stringRequest.setShouldCache(false);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            requestQueue.getCache().clear();
            requestQueue.add(stringRequest);
            return null;
        }
    }
}