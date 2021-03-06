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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CartActivity extends DefaultActionbar {

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> conditions = new ArrayList<>();
    private static final String TAG = "RecycleViewAdapter";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private DatabaseReference mCartDatabase;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        //set up the screen
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.cart, null, false);
        drawer.addView(contentView, 0);

        getIDs();
    }

    @Override
    public void onBackPressed() {
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
                startActivity(new Intent(this, ChatActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //get the id of the items
    public void getIDs()
    {
        //get database path
        final String cartRef = "cart/" + uid;
        mCartDatabase= database.getReference(cartRef);
        mCartDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                names.clear();
                urls.clear();
                prices.clear();
                conditions.clear();
                id.clear();

                //get each item's info and display them
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String id = ds.getKey();
                    getResults(id);
                }

                Log.d(TAG, "Value is: " + cartRef);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    //function to get item's info
    private void getResults( String search)
    {
        DocumentReference docRef = db.collection("item").document(search);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        names.add(document.getString("name"));
                        conditions.add(document.getString("condition"));
                        prices.add(document.getDouble("price"));
                        id.add(document.getId());
                        if(document.getString("image1") != null)
                        {
                            urls.add(document.getString("image1"));
                        }
                        else
                        {
                            urls.add("https://firebasestorage.googleapis.com/v0/b/we-sell-491.appspot.com/o/itemImages%2Fdefault.png?alt=media&token=d4cb0d3c-7888-42d5-940f-d5586a4e0a4a");
                        }

                        showItems();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //function to remove item in cart
    public void removeItemAt(String id)
    {
        final String cartRef = "cart/" + uid + "/" + id;
        mCartDatabase= database.getReference(cartRef);
        mCartDatabase.removeValue();

    }

    //function to create recycler adapter to hold items
    private void showItems()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");
        final TextView noShow = (TextView) findViewById(R.id.noResult);
        noShow.setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleVView  );
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterVertical adapter = new RecycleViewAdapterVertical(this, names, urls, prices, conditions, 0,id, new RecycleViewAdapterVertical.AdapterListener() {
            @Override
            public void removeButtonOnClick(View v, int position)
            {
                removeItemAt(id.get(position));
            }

            @Override
            public void soldButtonOnClick(View v, int position)
            {

            }
        });

        recyclerView.setAdapter(adapter);

    }

}
