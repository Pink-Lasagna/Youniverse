package ru.jaromirchernyavsky.youniverse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.R;

public class EditCardAdapter extends RecyclerView.Adapter<EditCardAdapter.ViewHolder>{
    private final ArrayList<Card> added;
    private final ArrayList<Card> all;
    public EditCardAdapter(ArrayList<Card> added,ArrayList<Card> all){
        for(Card card:added){
            all.removeIf(card::equals);
        }
        this.added = added;
        this.all=all;
    }
    @NonNull
    @Override
    public EditCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType){
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_card_edited_minus, parent, false);
                break;
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_card_edited_plus, parent, false);
                break;
        }

        return new EditCardAdapter.ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if(position>added.size()-1){
            return 1;
        } else{
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EditCardAdapter.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        Card card = type==0?added.get(position):all.get(position-added.size());
        holder.imageView.setImageURI(card.uri);
        holder.name.setText(card.name);
        holder.imgbutton.setOnClickListener(v -> {
            if(getItemViewType(position)==0){
                added.remove(card);
                all.add(card);
            } else{
                all.remove(card);
                added.add(card);
            }
            notifyItemChanged(position);
            notifyItemChanged(getItemCount()-1);
        });
    }

    @Override
    public int getItemCount() {
        return added.size()+all.size();
    }

    public ArrayList<Card> getAdded() {
        return added;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final TextView name;
        public final ImageButton imgbutton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            imgbutton = itemView.findViewById(R.id.imgbtn);
        }
    }
}
