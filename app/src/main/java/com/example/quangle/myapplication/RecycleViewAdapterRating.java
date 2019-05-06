package com.example.quangle.myapplication;
import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RecycleViewAdapterRating extends RecyclerView.Adapter<RecycleViewAdapterRating.ViewHolder>{
    private static final String TAG = "RecycleViewAdapter";

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> conditions = new ArrayList<>();
    private ArrayList<Boolean> rate = new ArrayList<>();
    private Context mContext;
    private int mode;
    public AdapterListener onClickListener;

    public RecycleViewAdapterRating(Context mContext,ArrayList<String> names, ArrayList<String> urls, ArrayList<Double> prices, ArrayList<String> conditions) {
        this.names = names;
        this.urls = urls;
        this.mContext = mContext;
        this.prices = prices;
        this.conditions=conditions;
    }
    public RecycleViewAdapterRating(Context mContext,ArrayList<String> names, ArrayList<String> urls, ArrayList<Double> prices, ArrayList<String> conditions, ArrayList<Boolean> rate,AdapterListener listener ) {
        this.names = names;
        this.urls = urls;
        this.mContext = mContext;
        this.prices = prices;
        this.conditions=conditions;
        this.rate = rate;
        this.onClickListener = listener;
    }

    @Override
    public RecycleViewAdapterRating.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);

        return new RecycleViewAdapterRating.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecycleViewAdapterRating.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        if(rate.get(position) == false)
        {
            holder.rateSeller.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.rateSeller.setVisibility(View.INVISIBLE);
        }
        Glide.with(mContext)
                .asBitmap()
                .load(urls.get(position))
                .into(holder.itemImg);

        //set the contents on the list
        holder.itemName.setText(names.get(position));
        holder.itemPrice.setText(String.format("$%.2f" , prices.get(position)));
        holder.itemCondition.setText(conditions.get(position));

        holder.itemImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "onClick: clicked on an image" + names.get(position));
                Toast.makeText(mContext, names.get(position),Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        Button rateSeller;
        TextView itemName;
        TextView itemPrice;
        TextView itemCondition;
        ImageView itemImg;
        public ViewHolder(View itemView)
        {
            super(itemView);
            itemName = (TextView) itemView.findViewById(R.id.itemTitle);
            itemPrice = (TextView) itemView.findViewById(R.id.itemPrice);
            itemCondition = (TextView) itemView.findViewById(R.id.itemCondition);
            itemImg= itemView.findViewById(R.id.itemImg);
            rateSeller = (Button) itemView.findViewById(R.id.rateSeller);
            rateSeller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.rateButtonOnClick(view, getAdapterPosition());
                    rated(getAdapterPosition());
                }
            });



        }
    }
    public interface AdapterListener
    {
        void rateButtonOnClick( View v, int position);
    }

    //update layout
    public void rated(int position)
    {
        notifyItemChanged(position);
    }


}
