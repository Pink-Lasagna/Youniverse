package ru.jaromirchernyavsky.youniverse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.ChatMessage;
import ru.jaromirchernyavsky.youniverse.R;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder>{
    private final ArrayList<ArrayList<ChatMessage>> messages;
    private final View.OnClickListener onClickListener;
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
        MessageAdapter messageAdapter = new MessageAdapter(messages.get(position), false);
        holder.lastMessages.setAdapter(messageAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(holder.lastMessages.getContext());
        layoutManager.setStackFromEnd(true);
        holder.lastMessages.setLayoutManager(layoutManager);
        holder.lastMessages.setClickable(false);
        holder.chat.setOnClickListener(v -> {
            setPos(holder.getAdapterPosition());
            onClickListener.onClick(v);
        });
        holder.delete.setOnClickListener(v -> {
            messages.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
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
        public final ImageButton delete;
        public final RecyclerView lastMessages;
        public final ImageButton chat;

        public ViewHolder(View itemView) {
            super(itemView);
            delete = itemView.findViewById(R.id.delete);
            lastMessages = itemView.findViewById(R.id.recycler);
            chat = itemView.findViewById(R.id.chat);
        }
    }
}
