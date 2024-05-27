package ru.jaromirchernyavsky.youniverse.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.ChatMessage;
import ru.jaromirchernyavsky.youniverse.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    ArrayList<ChatMessage> messages;
    public MessageAdapter(ArrayList<ChatMessage> messages){
        this.messages = messages;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = new View(parent.getContext());
        if (viewType == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_sent, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_recieved, parent, false);
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(holder.getItemViewType()!=2) {
            holder.message.setText(messages.get(position).getSpannable());
        }

    }

    @Override
    public int getItemViewType(int position) {
        switch (messages.get(position).getRole()){
            case "user":
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView message;

        public ViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if(this.getAdapterPosition()!=0){
                switch (this.getItemViewType()){
                    case 1:
                        menu.add(this.getAdapterPosition(),1,0,"Удалить");
                        break;
                    case 0:
                        menu.add(this.getAdapterPosition(),0,0,"Перегенирировать");
                        menu.add(this.getAdapterPosition(),1,0,"Удалить");
                        break;
                }
            }
        }
    }
}
