package ru.jaromirchernyavsky.youniverse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Card> cards;
    public MainAdapter(Context context, ArrayList<Card> cards){
        this.context = context;
        this.cards = cards;
    }
    @Override
    public int getCount() {
        return cards.size();
    }

    @Override
    public Object getItem(int position) {
        return cards.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(inflater==null){
            inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(convertView == null){
            convertView=inflater.inflate(R.layout.fragment_card,null);
        }
        ImageView imageView = convertView.findViewById(R.id.image);
        TextView name = convertView.findViewById(R.id.name);
        TextView description = convertView.findViewById(R.id.description);
        imageView.setImageURI(cards.get(position).uri);
        name.setText(cards.get(position).name);
        description.setText(cards.get(position).description);
        return convertView;
    }
}
