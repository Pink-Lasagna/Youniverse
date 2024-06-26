package ru.jaromirchernyavsky.youniverse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.R;

public class WorldCardAdapter extends RecyclerView.Adapter<WorldCardAdapter.ViewHolder>{
    private final ArrayList<Card> cards;
    public WorldCardAdapter(ArrayList<Card> cards){
        this.cards = cards;
    }
    @NonNull
    @Override
    public WorldCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_card_forworld, parent, false);
        return new WorldCardAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WorldCardAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageURI(cards.get(position).uri);
        holder.name.setText(cards.get(position).name);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
        }
    }
}
