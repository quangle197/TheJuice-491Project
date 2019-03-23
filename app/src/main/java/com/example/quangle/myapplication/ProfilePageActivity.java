package com.example.quangle.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;

public class ProfilePageActivity extends AppCompatActivity{
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private static final String TAG = "ProfilePageActivity";
        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.profile_page);
            getImage();
        }

        private void getImage()
        {
            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://www.sneakersnstuff.com/images/184924/zoom.jpg");
            names.add("Reigning champ");

            urls.add("https://www.sneakersnstuff.com/images/219550/zoom.jpg");
            names.add("Reigning champ tshirt");

            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://www.sneakersnstuff.com/images/184924/zoom.jpg");
            names.add("Reigning champ");

            urls.add("https://www.sneakersnstuff.com/images/219550/zoom.jpg");
            names.add("Reigning champ tshirt");
            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://www.sneakersnstuff.com/images/184924/zoom.jpg");
            names.add("Reigning champ");

            urls.add("https://www.sneakersnstuff.com/images/219550/zoom.jpg");
            names.add("Reigning champ tshirt");
            initRecyclerView();
        }

    private void initRecyclerView()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleHView  );
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterProfile adapter = new RecycleViewAdapterProfile(this, names, urls);
        recyclerView.setAdapter(adapter);
    }
}
