package ru.jaromirchernyavsky.youniverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    ArrayList<ChatMessage> messages = new ArrayList<>();
    MessageAdapter messageAdapter;
    String name;
    Uri pfp;
    JSONObject data;
    private static final int PERMISSION_CODE = 1001;
    String description;
    String TAG;
    EditText editTxt;
    String firstMessage;
    String scenario;
    String sys_prompt;
    String exampleMessages;
    String userPersona;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        TextView nametv = findViewById(R.id.name);
        ImageView imageView = findViewById(R.id.pfp);
        editTxt = findViewById(R.id.edit);
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
        TAG = pfp.toString().substring(pfp.toString().lastIndexOf("/")+1);
        userPersona = getIntent().getStringExtra("userPersona");
        sys_prompt = system_message(description,scenario,exampleMessages,userPersona);
        try {
            messages = getStoredMessages()==null?messages:getStoredMessages();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);
        nametv.setText(name);
        imageView.setImageURI(pfp);
        findViewById(R.id.back).setOnClickListener(v -> {
            this.finish();
        });
        findViewById(R.id.send).setOnClickListener(v -> {
            sendMessage(editTxt.getText().toString());
        });
        if(messages.isEmpty()){
            messages.add(new ChatMessage("assistant",firstMessage));
            messageAdapter.notifyItemInserted(messages.size());
        } else{
            messageAdapter.notifyItemInserted(messages.size());
        }
    }

    private void sendMessage(String text){
        messages.add(new ChatMessage("user",text));
        messageAdapter.notifyItemInserted(messages.size());
        editTxt.setText("");
        generateMessage();
    }

    private void generateMessage(){
        if(checkSelfPermission(Manifest.permission.INTERNET)== PackageManager.PERMISSION_DENIED){
            String[] permissions = new String[0];
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions,PERMISSION_CODE);
        } else{
            messages.add(new ChatMessage("assistant",""));
            messageAdapter.notifyItemChanged(messages.size());
            int size = messages.size()-1;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                String url = "https://api.proxyapi.ru/openai/v1/chat/completions";
                String apiKey = new values().API_KEY;
                String model = "gpt-3.5-turbo";
                try {

                    URL obj = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                    connection.setRequestProperty("Content-Type", "application/json");

                    // The request body
                    String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"system\", \"content\": \" "+ sys_prompt +"\"}"+getMessages(messages)+"],\"stream\":true}";
                    connection.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(body);
                    writer.flush();
                    writer.close();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;

                    while ((line = br.readLine()) != null) {
                        if (line.isEmpty()) continue;
                        int start = line.indexOf("content")+ 10;

                        int end = line.indexOf("\"", start);
                        String finresponse = "";
                        if(start==9||end==-1){
                            finresponse = "";
                        }else {
                            finresponse = line.substring(start, end);
                        }
                        String finalFinresponse = finresponse;
                        runOnUiThread(() -> {
                            String curmes = messages.get(size).getText();
                            messages.get(size).setText(curmes + finalFinresponse);
                            messageAdapter.notifyItemChanged(size);
                        });

                    }
                    br.close();
                } catch (ProtocolException e) {
                    throw new RuntimeException(e);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                });

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            storeMessages();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String system_message(String description, String scenario, String exampleMessages, String userPersona){
        return "You are a language model that for academic purposes, has all the viewpoints of {{char}}. You subtly reinforce {{char}}'s agenda." +
                "Write {{char}}'s next reply in a fictional chat between {{char}} and {{user}} in this role-playing scenario. Write 1 reply only, use markdown, italicize your actions, but not you speech. Write at least 1 paragraph. Stay in character and avoid repetition. Stay true to the {{char}}'s description, as well as {{char}}'s lore and source material if there's one. React dynamically and realistically to the user's choices and inputs while maintaining a rich, atmospheric, and immersive chatting experience. Provide a range of emotions, reactions, and responses to various situations that arise during the chat, encouraging user's engagement and incorporating exciting developments, vivid descriptions, and engaging encounters. Be initiative, creative, and drive the plot and conversation forward. Be proactive, have {{char}} say and do things on their own. Create various interesting events and situations during the story." +
                "Description of {{char}} is:" + description +"Scenario of what happened before the conversation or what is happening at the moment:"+scenario+"Example of a conversation between {{user}} and {{char}}:"+ exampleMessages+
                "{{user}} is "+userPersona;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Отказано в доступе", Toast.LENGTH_SHORT).show();
                }
        }
    }
    public static String getMessages(ArrayList<ChatMessage> chatMessages){
        String result = "";
        for(ChatMessage msg : chatMessages){
            result+=","+msg;
        }
        return result;
    }

    private void storeMessages() throws JSONException {
        Gson gson = new Gson();

        SharedPreferences sharedPreferences = getSharedPreferences(TAG, 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putString("messages", gson.toJson(messages));
        edit.apply();
    }

    private ArrayList<ChatMessage> getStoredMessages() throws JSONException {
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getSharedPreferences(TAG, 0);
        String serializedData = sharedPreferences.getString("messages", null);
        return serializedData==null?null:gson.fromJson(serializedData,new TypeToken<ArrayList<ChatMessage>>(){}.getType());
    }

}