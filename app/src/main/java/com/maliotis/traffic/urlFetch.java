package com.maliotis.traffic;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class urlFetch {
    Context mContext;
    private DirectionsHandler mHandler;

    public urlFetch(Context context){
        mContext = context;
    }


    public DirectionsHandler getDirections(double lat,double lon, double latDest, double lonDest) throws IOException{
        String key = mContext.getResources().getString(R.string.google_maps_key);
        String urlDestination = "https://maps.googleapis.com/maps/api/directions/json?origin= "
                + lat + "," + lon + "&destination= " + latDest + "," + lonDest + "&key="+key;

        Request request = new Request.Builder().url(urlDestination).build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Error
                // TODO : Show an alert dialog
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String JSONData = response.body().string();
                Log.v("JsonData",JSONData);
                if (response.isSuccessful()){
                    //Do stuff
                    //TODO: Create class to handle the data!
                    //TODO: Best Solution pass the data to an Object to handle the data

                    mHandler = new DirectionsHandler(JSONData);
                }
            }
        });
        return mHandler;
    }


}
