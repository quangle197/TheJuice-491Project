package com.example.quangle.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ChatProfileAdapter extends RecyclerView.Adapter<ChatProfileAdapter.ViewHolder> {
    private static final String TAG = "ChatProfileAdapter";
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<String> ids = new ArrayList<>();
    private Context mContext;

    public ChatProfileAdapter(Context mContext, ArrayList<String> names, ArrayList<String> urls, ArrayList<String> ids)
    {
        this.names = names;
        this.urls = urls;
        this.ids = ids;
        this.mContext = mContext;
    }

    @Override
    public ChatProfileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_profile,parent,false);
        return new ChatProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatProfileAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        Glide.with(mContext)
                .asBitmap()
                .load(urls.get(position))
                .into(holder.itemImg);


        holder.itemImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on an image" + names.get(position));
                Toast.makeText(mContext, names.get(position), Toast.LENGTH_SHORT).show();
                goToChat(ids.get(position));
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
        public ViewHolder(View itemView)
        {
            super(itemView);
            itemImg= itemView.findViewById(R.id.imageViewOther);
            itemTitle=itemView.findViewById(R.id.profileChat);
        }
    }

    protected void goToChat(String recID)
    {
        Intent intent = new Intent(mContext, MessageActivity.class);
        intent.putExtra("EXTRA_SESSION_ID", recID);
        mContext.startActivity(intent);
    }
}
