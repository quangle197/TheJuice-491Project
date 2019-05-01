package com.example.quangle.myapplication;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class SearchableActivity extends DefaultActionbar  {
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> conditions = new ArrayList<>();

    private RecyclerView resultList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "RecycleViewAdapter";
    RecyclerView recyclerView;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.search_page, null, false);
        drawer.addView(contentView, 0);

        handleIntent(getIntent());
    }

    @Override
    public void onBackPressed()
    {
        //startActivity(new Intent(this, MainActivity.class));
        //super.onBackPressed();
        this.finish();
    }
    @Override
    protected void onNewIntent(Intent intent)
    {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent)
    {
        if(Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);

            getResults(query);
        }
    }



    private void getResults(final String search)
    {
        db.collection("item")
                //.whereGreaterThanOrEqualTo("name", search)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            names.clear();
                            urls.clear();
                            prices.clear();
                            conditions.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getString("name").contains(search))
                                {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    names.add(document.getString("name"));
                                    conditions.add(document.getString("condition"));
                                    prices.add(document.getDouble("price"));
                                    if(document.getString("image1") != null)
                                    {
                                        urls.add(document.getString("image1"));
                                    }
                                    else
                                    {
                                        urls.add("https://firebasestorage.googleapis.com/v0/b/we-sell-491.appspot.com/o/itemImages%2Fdefault.png?alt=media&token=d4cb0d3c-7888-42d5-940f-d5586a4e0a4a");
                                    }
                                }
                            }
                            firebaseUserSearch();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void firebaseUserSearch()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleVView  );
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterVertical adapter = new RecycleViewAdapterVertical(this, names, urls,prices,conditions);
        recyclerView.setAdapter(adapter);

    }

}

