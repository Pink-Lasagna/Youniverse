package ru.jaromirchernyavsky.youniverse;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
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


    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        public ImageView imageView;
        public TextView name;
        public TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(),0,0,"Скачать в галерею");
            menu.add(this.getAdapterPosition(),1,1,"Удалить");
        }
    }

    public void deleteCard(int pos) {
        new File(cards.get(pos).uri.toString()).delete();
        cards.remove(pos);
        notifyItemRemoved(pos);
    }
}
