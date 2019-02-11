package com.example.quangle.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Payment extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_main);
        ListView listView=(ListView)findViewById(R.id.listView);
        CustomAdapter customAdapter = new CustomAdapter();
        listView.setAdapter(customAdapter);
    }

    class CustomAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return 1;
        }

        @Override
        public Object getItem(int i)
        {
            return null;
        }

        @Override
        public long getItemId(int i)
        {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            view = getLayoutInflater().inflate(R.layout.list_item,null);
            ImageView imageView = (ImageView)view.findViewById(R.id.itemImg);
            TextView textView_name = (TextView)view.findViewById(R.id.itemTitle);
            TextView textView_price = (TextView)view.findViewById(R.id.itemPrice);
            TextView textView_quantity = (TextView)view.findViewById(R.id.itemQuantity);

            imageView.setImageResource(R.drawable.wesell);
            textView_name.setText("test");
            textView_price.setText("10.00");
            textView_quantity.setText("1");
            return view;

        }
    }
}
