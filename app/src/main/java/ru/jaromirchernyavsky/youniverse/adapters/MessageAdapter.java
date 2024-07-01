package ru.jaromirchernyavsky.youniverse.adapters;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.ChatMessage;
import ru.jaromirchernyavsky.youniverse.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private final ArrayList<ChatMessage> messages;
    private final boolean clickable;
    public MessageAdapter(ArrayList<ChatMessage> messages, boolean clickable){
        this.messages = messages;
        this.clickable = clickable;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (viewType == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_sent, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_recieved, parent, false);
        }
        return new ViewHolder(v, clickable);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.message.setText(messages.get(position).getSpannable());
        String pfp = messages.get(position).getPfp();
        File file = new File(Uri.parse(pfp).getPath());
        if (file.exists()) holder.pfp.setImageURI(Uri.parse(pfp));
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getRole().equals("user")) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public final TextView message;
        public final ImageView pfp;

        public ViewHolder(View itemView, boolean clicklable) {
            super(itemView);
            pfp = itemView.findViewById(R.id.pfp);
            message = itemView.findViewById(R.id.message);
            if(clicklable) itemView.setOnCreateContextMenuListener(this);
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
