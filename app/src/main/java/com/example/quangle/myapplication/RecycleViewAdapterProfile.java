package com.example.quangle.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecycleViewAdapterProfile extends RecyclerView.Adapter<RecycleViewAdapterProfile.ViewHolder>{
    private static final String TAG = "RecycleViewAdapter";

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> id = new ArrayList<>();
    private Context mContext;

    public RecycleViewAdapterProfile(Context mContext,ArrayList<String> names, ArrayList<String> urls,
                                     ArrayList<Double> prices,ArrayList<String> id)
    {
        this.names = names;
        this.urls = urls;
        this.mContext = mContext;
        this.prices = prices;
        this.id = id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_listitem,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        Glide.with(mContext)
                .asBitmap()
                .load(urls.get(position))
                .into(holder.itemImg);

        //set the contents on the list
        holder.itemTitle.setText(names.get(position));
        holder.itemPrice.setText(String.format("$%.2f" , prices.get(position)));
        holder.itemImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "onClick: clicked on an image" + names.get(position));
                Toast.makeText(mContext, names.get(position),Toast.LENGTH_SHORT).show();
                openIntent(id.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView itemTitle;
        ImageView itemImg;
        TextView itemPrice;
        public ViewHolder(View itemView)
        {
            super(itemView);
            itemImg= itemView.findViewById(R.id.pItemImage);
            itemTitle=itemView.findViewById(R.id.pItemName);
            itemPrice = itemView.findViewById(R.id.pItemPrice);
        }
    }

    public void openIntent(String s)
    {
        Intent intent = new Intent(mContext, ItemScreenActivity.class);
        intent.putExtra("EXTRA_SESSION_ID", s);
        mContext.startActivity(intent);

    }
}
