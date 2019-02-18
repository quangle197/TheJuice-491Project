package com.example.quangle.myapplication;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ListItem {


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String name, sold, condition, description;
    int price, distance, quantity;


    public void listItem(String name, String sold, String condition, String description, int price, int distance, int quantity)
    {

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("price", price);
        user.put("distance", distance);
        user.put("sold", sold);
        user.put("condition", condition);
        user.put("quantity", quantity);
        user.put("description", description);

        db.collection("item")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "Added item");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding item",e);
                    }
                });
    }

}
