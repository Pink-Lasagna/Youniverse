package ru.jaromirchernyavsky.youniverse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.function.Function;

import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.R;

public class SmallCharAdapter extends RecyclerView.Adapter<SmallCharAdapter.ViewHolder> {
    private ArrayList<Card> chars;
    private Function<Card,Void> onClick;

    public SmallCharAdapter(ArrayList<Card> chars, Function<Card,Void> onClick){
        this.chars = chars;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_card_small,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.pfp.setImageURI(chars.get(position).uri);
        holder.pfp.setOnClickListener(v -> onClick.apply(chars.get(position)));
    }

    @Override
    public int getItemCount() {
        return chars.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView pfp;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.pfp);
        }
    }
}
