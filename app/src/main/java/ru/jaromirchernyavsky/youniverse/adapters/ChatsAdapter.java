package ru.jaromirchernyavsky.youniverse.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.ChatMessage;
import ru.jaromirchernyavsky.youniverse.R;
import ru.jaromirchernyavsky.youniverse.ViewChats;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder>{
    private ArrayList<ArrayList<ChatMessage>> messages;
    private View.OnClickListener onClickListener;
    private int pos;
    public ChatsAdapter(ArrayList<ArrayList<ChatMessage>> messages, View.OnClickListener onClickListener){
        this.messages = messages;
        this.onClickListener = onClickListener;
    }
    @NonNull
    @Override
    public ChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat, parent, false);
        return new ChatsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsAdapter.ViewHolder holder, int position) {
        MessageAdapter messageAdapter = new MessageAdapter(messages.get(position));
        holder.lastMessages.setAdapter(messageAdapter);
        holder.lastMessages.setLayoutManager(new LinearLayoutManager(holder.lastMessages.getContext()));
        holder.lastMessages.scrollToPosition(messages.get(position).size());
        holder.chat.setOnClickListener(v -> {
            setPos(holder.getAdapterPosition());
            onClickListener.onClick(v);
        });
        holder.delete.setOnClickListener(v -> {
            try{
                messages.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            } catch (Exception ignore){
            }

        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton delete;
        public RecyclerView lastMessages;
        public ImageButton chat;

        public ViewHolder(View itemView) {
            super(itemView);
            delete = itemView.findViewById(R.id.delete);
            lastMessages = itemView.findViewById(R.id.recycler);
            chat = itemView.findViewById(R.id.chat);
        }
    }
}
