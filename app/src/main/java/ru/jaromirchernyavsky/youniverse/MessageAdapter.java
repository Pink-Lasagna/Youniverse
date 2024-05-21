package ru.jaromirchernyavsky.youniverse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.ArrayList;

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
        if(holder.getItemViewType()!=2){
            holder.message.setText(messages.get(position).getContent());
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (messages.get(position).getRole()){
            case "user":
                return 1;
            case "system":
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message;

        public ViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
        }
    }
}
