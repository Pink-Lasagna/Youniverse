package ru.jaromirchernyavsky.youniverse;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private ArrayList<Card> cards;
    public RecyclerAdapter(ArrayList<Card> cards){
        this.cards = cards;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageView.setImageURI(cards.get(position).uri);
        holder.name.setText(cards.get(position).name);
        holder.description.setText(cards.get(position).description);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("name",cards.get(position).name);
            intent.putExtra("uri",cards.get(position).uri);
            intent.putExtra("data",cards.get(position).convertedData.toString());
            intent.putExtra("userPersona","human");
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView name;
        public TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
        }
    }
}
