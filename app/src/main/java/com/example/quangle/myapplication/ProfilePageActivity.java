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
            urls.add("https://cdn.shopify.com/s/files/1/1499/3122/products/RC_3205_M_Black_Zip_Hoodie_Front_1553_2_d8dfd745-0683-42de-9ab0-daa07894d1de_1024x1024.JPG?v=1549500312");
            names.add("Reigning champ");

            urls.add("https://www.sneakersnstuff.com/images/219550/large.jpg");
            names.add("Reigning champ tshirt");

            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://cdn.shopify.com/s/files/1/1499/3122/products/RC_3206_M_Black_Hoodie_Front_copy_1024x1024.jpg?v=1549562065");
            names.add("Reigning champ");

            urls.add("https://www.sneakersnstuff.com/images/219550/large.jpg");
            names.add("Reigning champ tshirt");
            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://www.sneakersnstuff.com/images/184924/large.jpg");
            names.add("Reigning champ");

            urls.add("https://www.sneakersnstuff.com/images/219550/large.jpg");
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
