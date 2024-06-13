package ru.jaromirchernyavsky.youniverse.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.R;
import ru.jaromirchernyavsky.youniverse.card_info;

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
            Intent intent = new Intent(v.getContext(), card_info.class);
            intent.putExtra("name",cards.get(holder.getAdapterPosition()).name);
            intent.putExtra("uri",cards.get(holder.getAdapterPosition()).uri);
            intent.putExtra("data",cards.get(holder.getAdapterPosition()).convertedData.toString());
            intent.putExtra("userPersona","human");
            intent.putExtra("world",cards.get(holder.getAdapterPosition()).world);
            v.getContext().startActivity(intent);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(ArrayList<Card> list){
        cards = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        public final ImageView imageView;
        public final TextView name;
        public final TextView description;

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

    /** @noinspection ResultOfMethodCallIgnored*/
    public void deleteCard(int pos) {
        new File(cards.get(pos).uri.toString().substring(8)).delete();
        cards.remove(pos);
        notifyItemRemoved(pos);
    }
}
