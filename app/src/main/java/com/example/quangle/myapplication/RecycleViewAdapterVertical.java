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

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecycleViewAdapterVertical extends RecyclerView.Adapter<RecycleViewAdapterVertical.ViewHolder>{
    private static final String TAG = "RecycleViewAdapter";

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> conditions = new ArrayList<>();
    private ArrayList<String> uName = new ArrayList<>();
    private Context mContext;
    private int mode = -1;
    public AdapterListener onClickListener;

    public RecycleViewAdapterVertical(Context mContext,ArrayList<String> names, ArrayList<String> urls, ArrayList<Double> prices, ArrayList<String> conditions) {
        this.names = names;
        this.urls = urls;
        this.mContext = mContext;
        this.prices = prices;
        this.conditions=conditions;
    }

    public RecycleViewAdapterVertical(Context mContext,ArrayList<String> names, ArrayList<String> urls, ArrayList<Double> prices, ArrayList<String> conditions, int visible,AdapterListener listener ) {
        this.names = names;
        this.urls = urls;
        this.mContext = mContext;
        this.prices = prices;
        this.conditions=conditions;
        this.mode = visible;
        this.onClickListener = listener;
    }

    public RecycleViewAdapterVertical(Context mContext,ArrayList<String> names, ArrayList<String> uName, ArrayList<String> urls, ArrayList<Double> prices, ArrayList<String> conditions, int visible,AdapterListener listener ) {
        this.names = names;
        this.uName = uName;
        this.urls = urls;
        this.mContext = mContext;
        this.prices = prices;
        this.conditions=conditions;
        this.mode = visible;
        this.onClickListener = listener;
    }
    @Override
    public RecycleViewAdapterVertical.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        if(mode==0)
        {
            ImageButton remove = (ImageButton) view.findViewById(R.id.removeButton);
            remove.setVisibility(View.VISIBLE);
        }
        else if(mode == 1)
        {
            ImageButton remove = (ImageButton) view.findViewById(R.id.removeButton);
            remove.setVisibility(View.VISIBLE);

            Button sold = (Button) view.findViewById(R.id.soldButton);
            sold.setVisibility(View.VISIBLE);

        }
        return new RecycleViewAdapterVertical.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecycleViewAdapterVertical.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

       Glide.with(mContext)
                .asBitmap()
                .load(urls.get(position))
                .into(holder.itemImg);

        //set the contents on the list
        holder.itemName.setText(names.get(position));
        holder.itemPrice.setText(String.format("$%.2f" , prices.get(position)));
        holder.itemCondition.setText(conditions.get(position));
        if(!uName.isEmpty() && uName.get(position) != null)
        {
            holder.userName.setVisibility(View.VISIBLE);
            holder.userName.append(uName.get(position));
        }

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
        ImageButton removeItem;
        Button markSold;
        TextView itemName;
        TextView itemPrice;
        TextView itemCondition;
        ImageView itemImg;
        TextView userName;
        public ViewHolder(View itemView)
        {
            super(itemView);
            itemName = (TextView) itemView.findViewById(R.id.itemTitle);
            itemPrice = (TextView) itemView.findViewById(R.id.itemPrice);
            itemCondition = (TextView) itemView.findViewById(R.id.itemCondition);
            itemImg= itemView.findViewById(R.id.itemImg);
            userName=itemView.findViewById(R.id.uName);
            removeItem = (ImageButton) itemView.findViewById(R.id.removeButton);
            removeItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.removeButtonOnClick(view, getAdapterPosition());
                    removeItem(getAdapterPosition());
                }
            });

            markSold = (Button) itemView.findViewById(R.id.soldButton);
            markSold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.soldButtonOnClick(view, getAdapterPosition());
                    removeItem(getAdapterPosition());
                }
            });
        }
    }
    public interface AdapterListener
    {
        void removeButtonOnClick( View v, int position);
        void soldButtonOnClick(View v,int position);
    }

    public void removeItem(int position)
    {
        names.remove(position);
        urls.remove(position);
        prices.remove(position);
        conditions.remove(position);
        if(position < uName.size())
        {
            uName.remove(position);
        }
        notifyItemRemoved(position);
        notifyItemRangeChanged(0,names.size());
    }
}
