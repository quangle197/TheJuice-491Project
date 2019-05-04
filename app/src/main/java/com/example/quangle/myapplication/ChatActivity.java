package com.example.quangle.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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

public class ChatActivity extends DefaultActionbar {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mChat;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<String> ids = new ArrayList<>();

    private static final String TAG = "ChatActivity";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.chat_room, null, false);
        drawer.addView(contentView, 0);
        getRooms();

    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
    public void getRooms()
    {
        final String cartRef = "chatRoom";
        mChat= database.getReference(cartRef);
        mChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    if(ds.child("sender1").getValue().equals(uid))
                    {
                        ids.add(ds.getKey());
                        getResults(ds.child("sender2").getValue(String.class));
                        return;
                    }
                    else if(ds.child("sender2").getValue().equals(uid))
                    {
                        ids.add(ds.getKey());
                        getResults(ds.child("sender1").getValue(String.class));
                        return;
                    }
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

    private void getResults( String search)
    {
        DocumentReference docRef = db.collection("users").document(search);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        names.add(document.getString("username"));
                        //ids.add(document.getId());
                        if(document.getString("profilePicture") != null)
                        {
                            urls.add(document.getString("profilePicture"));
                        }
                        else
                        {
                            urls.add("");
                        }

                        showUsers();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void showUsers()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleChatView  );
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        ChatProfileAdapter adapter = new ChatProfileAdapter(this, names, urls, ids);
        recyclerView.setAdapter(adapter);
    }



}
