package ru.jaromirchernyavsky.youniverse.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.R;
import ru.jaromirchernyavsky.youniverse.Utilities;
import ru.jaromirchernyavsky.youniverse.card_info;
import ru.jaromirchernyavsky.youniverse.custom.DeleteConfirmation;

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
            intent.putExtra("world",cards.get(holder.getAdapterPosition()).world);
            v.getContext().startActivity(intent);
        });
        holder.more.setOnClickListener(v->{showPopupMenu(holder.itemView.getContext(), holder);});
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


    static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final TextView name;
        public final TextView description;
        public final ImageButton more;

        public ViewHolder(View itemView) {
            super(itemView);
            more = itemView.findViewById(R.id.more);
            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);

        }
    }
    private void showPopupMenu(Context context, ViewHolder v) {
        PopupMenu popupMenu = new PopupMenu(context, v.more);
        popupMenu.getMenu().add(v.getAdapterPosition(),0,0,"Скачать в галерею");
        popupMenu.getMenu().add(v.getAdapterPosition(),1,1,"Удалить");
        popupMenu.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) item -> {
            switch (item.getOrder()) {
                case 0:
                    try {
                        Utilities.addImageToGallery(context, cards.get(item.getItemId()).uri);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case 1:
                    DeleteConfirmation.show(context, (x, y) -> deleteCard(item.getGroupId()));
                    break;
            }
            return false;
        });
        popupMenu.show();
    }
    /** @noinspection ResultOfMethodCallIgnored*/
    public void deleteCard(int pos) {
        new File(cards.get(pos).uri.toString().substring(8)).delete();
        cards.remove(pos);
        notifyItemRemoved(pos);
    }
}
