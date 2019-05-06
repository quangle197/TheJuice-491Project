package com.example.quangle.myapplication;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SoldItemActivity extends DefaultActionbar {

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> sellerID = new ArrayList<>();
    private ArrayList<String> conditions = new ArrayList<>();
    private static final String TAG = "RecycleViewAdapter";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private DatabaseReference mCartDatabase;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private RecycleViewAdapterRating adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.buy_history, null, false);
        drawer.addView(contentView, 0);
        getSupportActionBar().setTitle("Sold Items");
        getResults();
    }

    @Override
    public void onBackPressed() {
        //startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.message:
                startActivity(new Intent(this, MessageActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //get item's information
    private void getResults()
    {
        db.collection("item")
                .whereEqualTo("sellerID", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.get("buyerID") != null)
                                {
                                    if(document.getString("image1") != null)
                                    {
                                        urls.add(document.getString("image1"));
                                    }
                                    else
                                    {
                                        urls.add("https://firebasestorage.googleapis.com/v0/b/we-sell-491.appspot.com/o/itemImages%2Fdefault.png?alt=media&token=d4cb0d3c-7888-42d5-940f-d5586a4e0a4a");
                                    }
                                    names.add(document.getString("name"));
                                    prices.add(document.getDouble("price"));
                                    id.add(document.getId());
                                    conditions.add(document.getString("condition"));
                                    sellerID.add(document.getString("sellerID"));
                                }
                                showItems();
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //display items
    private void showItems()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");
        final TextView noShow = (TextView) findViewById(R.id.noResultHistory);
        noShow.setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleRView  );
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterVertical adapter = new RecycleViewAdapterVertical(this, names, urls, prices, conditions, -1,id, new RecycleViewAdapterVertical.AdapterListener() {
            @Override
            public void removeButtonOnClick(View v, int position){
            }

            @Override
            public void soldButtonOnClick(View v, int position)
            {

            }
        });

        recyclerView.setAdapter(adapter);
    }
}
