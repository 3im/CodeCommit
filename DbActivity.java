package com.example.zim.testapp;



import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class DbActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);
        List list = new ArrayList<>();
        list.addAll(MainActivity.MyCurrency.getAll());
        Adapter adapter = new Adapter(this, R.layout.db_item, list);
        ListView x = (ListView)findViewById(R.id.listView);
        x.setAdapter(adapter);
        final Activity t = this;
        Button back = (Button)findViewById(R.id.dummy_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    t.finish();
                } catch (Throwable e){}
            }
        });
    }
    class Adapter extends ArrayAdapter<MainActivity.MyCurrency> {
        private List<MainActivity.MyCurrency> items;
        private int layoutResourceId;
        private Context context;
        public Adapter(Context context, int layoutResourceId, List<MainActivity.MyCurrency> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            CurrencyHolder holder = null;

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new CurrencyHolder();
            holder.xcurrency = items.get(position);
            holder.date = holder.xcurrency.date;

            holder.text = (TextView)row.findViewById(R.id.itemText);
            holder.logo = (ImageView)row.findViewById(R.id.itemImg);

            row.setTag(holder);
            setupItem(holder);
            return row;
        }

        private void setupItem(CurrencyHolder holder) {
            if (!holder.date.isEmpty()){
                holder.text.setText("Date: " + holder.date + "\nBuy: " + holder.xcurrency.buy + "\nSell: " + holder.xcurrency.sell);
            } else {holder.text.setText("\nBuy: " + holder.xcurrency.buy + "\nSell: " + holder.xcurrency.sell);}
            switch (holder.xcurrency.currency){
                case "RUR": holder.logo.setImageResource(R.drawable.rub);
                    break;
                case "EUR": holder.logo.setImageResource(R.drawable.euro);
                    break;
                case "USD": holder.logo.setImageResource(R.drawable.dollar);
                    break;
            }
        }

        public class CurrencyHolder {
            MainActivity.MyCurrency xcurrency;
            ImageView logo;
            TextView text;
            String date = "";
        }
    }


}
