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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.jaromirchernyavsky.youniverse.adapters.MessageAdapter;

public class ChatActivity extends AppCompatActivity {
    ArrayList<ChatMessage> messages = new ArrayList<>();
    private static final int PERMISSION_CODE = 1001;
    String TAG;
    MessageAdapter messageAdapter;
    String name;
    Uri pfp;
    JSONObject data;

    String description;
    EditText editTxt;
    String scenario;
    String exampleMessages;
    RecyclerView recyclerView;
    int chatid;
    String sys_prompt;
    boolean world;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        TextView nametv = findViewById(R.id.name);
        ImageView imageView = findViewById(R.id.pfp);
        editTxt = findViewById(R.id.edit);
        recyclerView = findViewById(R.id.cards);

        try {
            data = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("data")));
            description = data.getString("description");
            scenario = data.getString("scenario");
            chatid = getIntent().getIntExtra("chatid",0);
            name = getIntent().getStringExtra("name");
            pfp = getIntent().getParcelableExtra("uri");
            exampleMessages = data.getString("mes_example");
            world = getIntent().getBooleanExtra("world",false);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        TAG = pfp.toString().substring(pfp.toString().lastIndexOf("/")+1);
        String username = Utilities.getName(this);
        String description = Utilities.getDescription(this);
        if(world){
            sys_prompt = Utilities.worldSysPrompt(name,description,scenario,exampleMessages,username,description);
        } else{
            sys_prompt = Utilities.charSysPrompt(name,description,scenario,exampleMessages,username,description);
        }

        messages = Utilities.getStoredMessages(this,TAG,chatid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messages, true);
        recyclerView.setAdapter(messageAdapter);
        nametv.setText(name);
        imageView.setImageURI(pfp);
        findViewById(R.id.back).setOnClickListener(v -> this.finish());
        findViewById(R.id.send).setOnClickListener(v -> {
            if(!editTxt.getText().toString().isEmpty())sendMessage(editTxt.getText().toString());
            generateMessage();
        });
        messageAdapter.notifyItemInserted(messages.size());
    }

    private void sendMessage(String text){
        messages.add(new ChatMessage("user",text));
        messageAdapter.notifyItemInserted(messages.size());
        editTxt.setText("");
        recyclerView.scrollToPosition(messages.size());
    }

    private void generateMessage(){
        if(checkSelfPermission(Manifest.permission.INTERNET)== PackageManager.PERMISSION_DENIED){
            String[] permissions;
            permissions = new String[]{Manifest.permission.INTERNET};
            requestPermissions(permissions,PERMISSION_CODE);
        } else{
            messages.add(new ChatMessage("assistant",""));
            messageAdapter.notifyItemChanged(messages.size());
            int size = messages.size()-1;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                String url = "https://api.proxyapi.ru/openai/v1/chat/completions";
                String apiKey = values.API_KEY;
                String model = "gpt-3.5-turbo";
                try {

                    URL obj = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                    connection.setRequestProperty("Content-Type", "application/json");

                    // The request body
                    String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"system\", \"content\": \""+ sys_prompt +"\"}"+Utilities.getMessages(messages)+"],\"stream\":true}";
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
                        String finresponse;
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                });

        }
    }

    @Override
    protected void onPause() {
        Utilities.storeMessages(this, messages, TAG, chatid);
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
                    int size = messages.size();
                    messages.removeIf(chatMessage -> messages.indexOf(chatMessage) >= item.getGroupId());
                    messageAdapter.notifyItemRangeRemoved(item.getGroupId(), size - 1);
                    if(item.getItemId()==0) generateMessage();
                })
                .setPositiveButton("Нет",(dialog, which) -> {
                }).show();
        return true;
    }
}