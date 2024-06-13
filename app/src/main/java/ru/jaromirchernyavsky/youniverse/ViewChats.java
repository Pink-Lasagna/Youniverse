package ru.jaromirchernyavsky.youniverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import ru.jaromirchernyavsky.youniverse.adapters.ChatsAdapter;

public class ViewChats extends AppCompatActivity implements View.OnClickListener {
    ChatsAdapter chatsAdapter;
    ArrayList<ArrayList<ChatMessage>> chatMessages = new ArrayList<>();
    String name;
    Uri pfp;
    String data;
    String firstMessage;
    boolean world;
    String TAG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_chats);
        data = getIntent().getStringExtra("data");
        name = getIntent().getStringExtra("name");
        pfp = getIntent().getParcelableExtra("uri");
        world = getIntent().getBooleanExtra("world",false);
        firstMessage = getIntent().getStringExtra("firstMessage");
        TAG = pfp.toString().substring(pfp.toString().lastIndexOf("/") + 1);
        ActionBar bar = getSupportActionBar();
        Objects.requireNonNull(bar).setDisplayShowTitleEnabled(false);
        bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.main)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_btn, menu);
        return true;
    }

    @Override
    protected void onResume() {
        chatMessages=new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences(TAG, 0);
        for (int i = 0; i < sharedPreferences.getAll().size(); i++) {
            chatMessages.add(new ArrayList<>());
        }
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            int index = Integer.parseInt(entry.getKey());
            ArrayList<ChatMessage> value = Utilities.getStoredMessages(this, TAG, index);
            chatMessages.set(index, value);
        }
        chatsAdapter = new ChatsAdapter(chatMessages,this);
        RecyclerView recyclerView = findViewById(R.id.recycle);
        recyclerView.setAdapter(chatsAdapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        if(sharedPreferences.getAll().size()==0){
            chatMessages.add(createStartMessage());
        }
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_add){
            chatMessages.add(createStartMessage());
        }else if(item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    public ArrayList<ChatMessage> createStartMessage(){
        ArrayList<ChatMessage> defchat = new ArrayList<>();
        defchat.add(new ChatMessage("assistant",firstMessage));
        chatsAdapter.notifyItemInserted(chatMessages.size());
        return defchat;
    }
    @Override
    protected void onPause() {
        getSharedPreferences(TAG,0).edit().clear().apply();
        for (int i = 0; i < chatMessages.size(); i++) {
            Utilities.storeMessages(this, chatMessages.get(i), TAG, i);
        }
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("uri", pfp);
        intent.putExtra("data", data);
        intent.putExtra("userPersona", "human");
        intent.putExtra("chatid", chatsAdapter.getPos());
        intent.putExtra("world", world);
        this.startActivity(intent);
    }
}