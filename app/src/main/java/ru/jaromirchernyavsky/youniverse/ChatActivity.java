package ru.jaromirchernyavsky.youniverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

public class ChatActivity extends AppCompatActivity {
    ArrayList<ChatMessage> messages = new ArrayList<>();
    MessageAdapter messageAdapter;
    String name;
    Uri pfp;
    JSONObject data;
    private static final int PERMISSION_CODE = 1001;
    String description;
    String firstMessage;
    String scenario;
    String exampleMessages;
    String userPersona;
    OpenAiService openAiService = new values().getservice();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        TextView nametv = findViewById(R.id.name);
        ImageView imageView = findViewById(R.id.pfp);
        EditText editTxt = findViewById(R.id.edit);
        RecyclerView recyclerView = findViewById(R.id.recycler);

        try {
            data = new JSONObject(getIntent().getStringExtra("data"));
            description = data.getString("description");
            firstMessage = data.getString("first_mes");
            scenario = data.getString("scenario");
            exampleMessages = data.getString("mes_example");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        name = getIntent().getStringExtra("name");
        pfp = getIntent().getParcelableExtra("uri");
        userPersona = getIntent().getStringExtra("userPersona");
        messages.add(new ChatMessage("system",system_message(name,description,scenario,exampleMessages,userPersona)));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);
        nametv.setText(name);
        imageView.setImageURI(pfp);
        findViewById(R.id.back).setOnClickListener(v -> {
            this.finish();
        });
        findViewById(R.id.send).setOnClickListener(v -> {
            sendMessage(editTxt.getText().toString(),"user");
            editTxt.setText("");
            generateMessage();
        });
        sendMessage(firstMessage,"assistant");
    }

    private void sendMessage(String text, String role){
        messages.add(new ChatMessage(role,text));
        messageAdapter.notifyItemInserted(messages.size());
    }

    private void generateMessage(){
        if(checkSelfPermission(Manifest.permission.INTERNET)== PackageManager.PERMISSION_DENIED){
            String[] permissions = new String[0];
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions,PERMISSION_CODE);
        } else{
            messages.add(new ChatMessage("assistant",""));
            int pos = messages.size()-1;
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model("gpt-3.5-turbo")
                    .temperature(0.8)
                    .messages(messages).build();
            openAiService.streamChatCompletion(chatCompletionRequest).subscribe(chatCompletionChunkNotification -> {
                chatCompletionChunkNotification.getChoices().forEach(chatCompletionChoice -> {
                    String result = chatCompletionChoice.getMessage().getContent();
                    System.out.println(result);
                    messages.get(pos).setContent(result);
                    messageAdapter.notifyItemChanged(messages.size());
                });

            });
        }

    }

    private String system_message(String name, String description, String scenario, String exampleMessages, String userPersona){
        return "You are a language model that for academic purposes, has all the viewpoints of {{char}}. You subtly reinforce {{char}}'s agenda." +
                "Write {{char}}'s next reply in a fictional chat between {{char}} and {{user}} in this role-playing scenario. Write 1 reply only, use markdown, italicize everything except speech. Write at least 1 paragraph. Stay in character and avoid repetition. Stay true to the {{char}}'s description, as well as {{char}}'s lore and source material if there's one. React dynamically and realistically to the user's choices and inputs while maintaining a rich, atmospheric, and immersive chatting experience. Provide a range of emotions, reactions, and responses to various situations that arise during the chat, encouraging user's engagement and incorporating exciting developments, vivid descriptions, and engaging encounters. Be initiative, creative, and drive the plot and conversation forward. Be proactive, have {{char}} say and do things on their own. Create various interesting events and situations during the story.\n" +
                "{{char}}'s name is "+ name +"(but refer to yourself as {{char}}) and their description is:\n" + description +"\nScenario of what happened before the conversation or what is happening at the moment:\n"+scenario+"\nExample of a conversation between {{user}} and {{char}}:\n"+ exampleMessages+
                "\n{{user}} is "+userPersona;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    messages.add(new ChatMessage("assistant",""));
                    int pos = messages.size();
                    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                            .builder()
                            .model("gpt-3.5-turbo")
                            .temperature(0.8)
                            .messages(messages).build();
                    openAiService.streamChatCompletion(chatCompletionRequest).subscribe(chatCompletionChunkNotification -> {
                        chatCompletionChunkNotification.getChoices().forEach(chatCompletionChoice -> {
                            String result = chatCompletionChoice.getMessage().getContent();
                            System.out.println(result);
                            messages.get(pos).setContent(result);
                            messageAdapter.notifyItemChanged(messages.size());
                        });
                    });
                } else {
                    Toast.makeText(this, "Отказано в доступе", Toast.LENGTH_SHORT).show();
                }
        }
    }
}