package ru.jaromirchernyavsky.youniverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import ru.jaromirchernyavsky.youniverse.adapters.MessageAdapter;
import ru.jaromirchernyavsky.youniverse.adapters.SmallCharAdapter;

public class ChatActivity extends AppCompatActivity {
    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private static final int PERMISSION_CODE = 1001;
    private String TAG;
    private MessageAdapter messageAdapter;
    private EditText editTxt;
    private RecyclerView recyclerView;
    private int chatID;
    private String sys_prompt;
    private ImageButton sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        TextView nametv = findViewById(R.id.name);
        ImageView imageView = findViewById(R.id.pfp);
        editTxt = findViewById(R.id.edit);
        recyclerView = findViewById(R.id.cards);

        boolean world;
        String exampleMessages;
        String scenario;
        String description;
        Uri pfp;
        String name;
        ArrayList<Card> cards = new ArrayList<>();
        try {
            JSONObject data = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("data")));
            description = data.getString("description");
            scenario = data.getString("scenario");
            chatID = getIntent().getIntExtra("chatID",0);
            name = getIntent().getStringExtra("name");
            pfp = getIntent().getParcelableExtra("uri");
            exampleMessages = data.getString("mes_example");
            world = getIntent().getBooleanExtra("world",false);
            if(world) cards = Utilities.getCardsFromJsonList(this,data.getString("characters"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        TAG = pfp.toString().substring(pfp.toString().lastIndexOf("/")+1);
        String username = Utilities.getName(this);
        String user_description = Utilities.getDescription(this);
        if(world){
            sys_prompt = Utilities.worldSysPrompt(name, description, scenario, exampleMessages,username,user_description);
            if(!cards.isEmpty()){
                ImageButton expand = findViewById(R.id.expand);
                expand.setVisibility(View.VISIBLE);
                RecyclerView smallRecycler = findViewById(R.id.smallRecycler);
                SmallCharAdapter smallCharAdapter = new SmallCharAdapter(cards,card->{
                    if(sendBtn.getVisibility()==View.VISIBLE){
                        try {
                            generateMessage(new ChatMessage(card.description, "",card.uri),Utilities.specialCharPrompt(name,description,exampleMessages,username,user_description,card));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                });
                LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
                smallRecycler.setLayoutManager(layoutManager);
                smallRecycler.setAdapter(smallCharAdapter);
                expand.setOnClickListener(v -> {
                    if (smallRecycler.getVisibility() == View.GONE) {
                        smallRecycler.setVisibility(View.VISIBLE);
                    } else {
                        smallRecycler.setVisibility(View.GONE);
                    }
                });
            }
        } else{
            sys_prompt = Utilities.charSysPrompt(name, description, scenario, exampleMessages,username,user_description);
        }

        messages = Utilities.getStoredMessages(this,TAG, chatID);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messages, true);
        recyclerView.setAdapter(messageAdapter);
        nametv.setText(name);
        imageView.setImageURI(pfp);
        findViewById(R.id.back).setOnClickListener(v -> this.finish());
        sendBtn = findViewById(R.id.send);
        sendBtn.setOnClickListener(v -> {
            if(!editTxt.getText().toString().isEmpty())sendMessage(editTxt.getText().toString());
            generateMessage(new ChatMessage("assistant","",pfp),sys_prompt);
        });
        messageAdapter.notifyItemInserted(messages.size());
    }

    private void sendMessage(String text){
        messages.add(new ChatMessage("user",text,getDrawable(R.mipmap.ic_launcher)));
        messageAdapter.notifyItemInserted(messages.size());
        editTxt.setText("");
        recyclerView.scrollToPosition(messages.size());
    }

    private void generateMessage(ChatMessage chatMessage, String sys_prompt){
        if(checkSelfPermission(Manifest.permission.INTERNET)== PackageManager.PERMISSION_DENIED){
            String[] permissions;
            permissions = new String[]{Manifest.permission.INTERNET};
            requestPermissions(permissions,PERMISSION_CODE);
        } else{
            messages.add(chatMessage);
            messageAdapter.notifyItemChanged(messages.size());
            int size = messages.size()-1;
            sendBtn.setVisibility(View.GONE);
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            Utilities.generateChatGPT("{\"role\": \"system\", \"content\": \""+ sys_prompt +"\"}"+Utilities.getMessages(messages),(String str)->{
                runOnUiThread(() -> {
                    String curmes = messages.get(size).getText();
                    messages.get(size).setText(curmes + str);
                    messageAdapter.notifyItemChanged(size);
                });
                return null;
            },()->{sendBtn.setVisibility(View.VISIBLE);findViewById(R.id.progress_bar).setVisibility(View.GONE);});
        }
    }

    @Override
    protected void onPause() {
        Utilities.storeMessages(this, messages, TAG, chatID);
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Отказано в доступе", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Вы уверены, что xотите это сделать?")
                .setMessage("Все сообщени после выбранного будут удалены")
                .setNegativeButton("Да",(dialog, which) -> {
                    int size = messages.size()-1;
                    String role = messages.get(size).getRole();
                    Object pfp = messages.get(size).getPfp();
                    messages.removeIf(chatMessage -> messages.indexOf(chatMessage) >= item.getGroupId());
                    messageAdapter.notifyItemRangeRemoved(item.getGroupId(), size);
                    if(item.getItemId()==0) generateMessage(new ChatMessage(role,"",pfp),sys_prompt);
                })
                .setPositiveButton("Нет",(dialog, which) -> {
                }).show();
        return true;
    }
}