package com.example.quangle.myapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ListItemActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String name, sold, condition, description;
    private int price, distance, quantity;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_listing);

        final EditText nameInput = (EditText) findViewById(R.id.title);
        final EditText descInput = (EditText) findViewById(R.id.description);
        final EditText priceInput = (EditText) findViewById(R.id.price);
        final EditText conditionInput = (EditText) findViewById(R.id.condition);
        final EditText distanceInput = (EditText) findViewById(R.id.category);
        final EditText sellbyInput = (EditText) findViewById(R.id.sellby);
        final EditText quantityInput = (EditText) findViewById(R.id.quantity);

        Button addItem = (Button) findViewById(R.id.button);
        addItem.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                name = nameInput.getText().toString();
                sold = sellbyInput.getText().toString();
                condition = conditionInput.getText().toString();
                description = descInput.getText().toString();
                price = Integer.parseInt(priceInput.getText().toString());
                distance = Integer.parseInt(distanceInput.getText().toString());
                quantity = Integer.parseInt(quantityInput.getText().toString());

                listItem(name, sold, condition, description, price, distance, quantity);
            }
        });
    }

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
