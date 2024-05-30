package ru.jaromirchernyavsky.youniverse;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import ar.com.hjg.pngj.PngjInputException;
import ru.jaromirchernyavsky.youniverse.adapters.EditCardAdapter;
import ru.jaromirchernyavsky.youniverse.adapters.WorldCardAdapter;

public class card_info extends AppCompatActivity implements View.OnFocusChangeListener {
    String name;
    Uri pfp;
    JSONObject data;

    String description;
    String firstMessage;
    String scenario;
    String exampleMessages;
    ArrayList<Card> cards = new ArrayList<>();
    EditCardAdapter editCardAdapter;
    TextInputLayout til_name;
    TextInputLayout til_summary;
    ImageView image;
    TextInputLayout til_first_message;
    TextInputLayout til_scenario;
    TextInputLayout til_example;
    FrameLayout frameLayout;
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    CircularProgressButton btn;
    boolean world;
    ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        pfp = data.getData();
                        image.setImageURI(pfp);
                        try {
                            InputStream iStream =   getContentResolver().openInputStream(pfp);
                            JSONObject jsonData = Utilities.getMetadataFromFile(iStream);
                            showDialog();
                            setEdits(jsonData.getString("name"),jsonData.getString("description"),jsonData.getString("first_mes"),jsonData.getString("scenario"),jsonData.getString("mes_example"));
                            ArrayList<Card> jsonCards = new ArrayList<>();
                            try {
                                jsonCards = Utilities.getCardsFromJsonList(getApplicationContext(),jsonData.getString("characters"));
                            } catch (JSONException e){
                                jsonCards = null;
                            }
                            if(jsonCards!=null){
                                world = true;
                                til_name.setHint("Имя");
                                til_summary.setHint("Описание");
                                linearLayout.setVisibility(View.GONE);
                                cards = jsonCards;
                            } else{
                                world = false;
                                til_name.setHint("Название");
                                til_summary.setHint("Сюжет мира");
                                linearLayout.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (PngjInputException e){
                        }
                        btn.setEnabled(pfp!=null&&!til_name.getEditText().getText().toString().isEmpty() && !til_summary.getEditText().getText().toString().isEmpty() && !til_first_message.getEditText().getText().toString().isEmpty());
                    }
                }
            });
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24);
        setContentView(R.layout.activity_card_info);
        til_name = findViewById(R.id.name);
        til_summary = findViewById(R.id.Summary);
        til_first_message = findViewById(R.id.first_message);
        til_scenario = findViewById(R.id.Scenario);
        til_example = findViewById(R.id.Primer);
        frameLayout = findViewById(R.id.btn_layout);
        image = findViewById(R.id.image);
        linearLayout = findViewById(R.id.worldGroup);
        btn = findViewById(R.id.CreateButton);
        ImageButton btn_edit = findViewById(R.id.edit);
        findViewById(R.id.cards);
        ImageButton btn_chat = findViewById(R.id.chat);
        recyclerView = findViewById(R.id.cards);
        initEdits();
        image.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if(checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)== PackageManager.PERMISSION_DENIED){
                    String[] permissions = new String[0];
                    permissions = new String[]{android.Manifest.permission.READ_MEDIA_IMAGES};
                    requestPermissions(permissions,PERMISSION_CODE);
                } else{
                    pickImageFromGallery();
                }
            } else{
                if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    String[] permissions = new String[0];
                    permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions,PERMISSION_CODE);
                } else{
                    pickImageFromGallery();
                }
            }
        });
        til_name.getEditText().setOnFocusChangeListener(this);
        til_summary.getEditText().setOnFocusChangeListener(this);
        til_first_message.getEditText().setOnFocusChangeListener(this);
        til_scenario.getEditText().setOnFocusChangeListener(this);
        til_example.getEditText().setOnFocusChangeListener(this);
        btn_chat.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ViewChats.class);
            intent.putExtra("name",name);
            intent.putExtra("uri",pfp);
            intent.putExtra("data",data.toString());
            intent.putExtra("firstMessage",firstMessage);
            intent.putExtra("userPersona","human");
            intent.putExtra("world",world);
            v.getContext().startActivity(intent);
        });
        btn_edit.setOnClickListener(v -> {
            toggleEdits();
        });
        btn.setOnClickListener(v -> {
            btn.startAnimation();
            Bitmap finalBitmap = Utilities.getContactBitmapFromURI(this,pfp);
            new File(pfp.getPath()).delete();
            String metadata = "";
            if(world){
                metadata = Utilities.generateMetadata(this,til_summary,til_first_message,til_example,til_name,til_scenario,editCardAdapter.getAdded());
                cards = editCardAdapter.getAdded();
            } else{
                metadata = Utilities.generateMetadata(this,til_summary,til_first_message,til_example,til_name,til_scenario,new ArrayList<Card>());
            }
            Utilities.saveCard(this,finalBitmap,metadata,pfp.getLastPathSegment(),world,true);
            toggleEdits();
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImage.launch(intent);
    }
    private void setEdits(String name, String summary, String firstMessage, String scenario, String example){
        til_name.getEditText().setText(name);
        til_summary.getEditText().setText(summary);
        til_first_message.getEditText().setText(firstMessage);
        til_scenario.getEditText().setText(scenario);
        til_example.getEditText().setText(example);
    }

    private void initEdits(){
        try {
            data = new JSONObject(getIntent().getStringExtra("data"));
            description = data.getString("description");
            firstMessage = data.getString("first_mes");
            scenario = data.getString("scenario");
            name = getIntent().getStringExtra("name");
            pfp = getIntent().getParcelableExtra("uri");
            exampleMessages = data.getString("mes_example");
            world = getIntent().getBooleanExtra("world",false);
            if(world) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                cards = Utilities.getCardsFromJsonList(this,data.getString("characters"));
                if(cards.contains(null)){
                    cards.remove(null);
                    Bitmap finalBitmap = Utilities.getContactBitmapFromURI(this,pfp);
                    new File(pfp.getPath()).delete();
                    String metadata = Utilities.generateMetadata(this,til_summary,til_first_message,til_example,til_name,til_scenario,editCardAdapter.getAdded());
                    Utilities.saveCard(this,finalBitmap,metadata,pfp.getLastPathSegment(),world,false);
                }
                linearLayout.setVisibility(View.VISIBLE);
                WorldCardAdapter worldCardAdapter = new WorldCardAdapter(cards);
                recyclerView.setAdapter(worldCardAdapter);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        setEdits(name,description,firstMessage,scenario,exampleMessages);
        image.setImageURI(pfp);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if(til_name.isEnabled()){
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Вы уверены, что хотите прекратить редактировать?")
                        .setMessage("Все изменения будут потеряны")
                        .setNegativeButton("Да",(dialog, which) -> {
                            initEdits();
                            toggleEdits();
                        })
                        .setPositiveButton("Нет",(dialog, which) -> {
                        }).show();
            } else{
                getOnBackPressedDispatcher().onBackPressed();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Отказано в доступе", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showDialog(){
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getBaseContext());
        alert.setTitle("Вы хотите импортировать данные из фото?");
        alert.setMessage("При сканировании фото были найдены данные. Импортировав их, вы потеряете все, что вы вписали");
        alert.setPositiveButton("Да",(dialog, which) -> {});
        alert.setNegativeButton("Нет",(dialog, which) -> {
            throw new PngjInputException("Ignore this");
        });
        alert.show();
    }
    public void toggleEdits(){
        boolean enable = !til_name.isEnabled();
        til_summary.setEnabled(enable);
        til_first_message.setEnabled(enable);
        til_scenario.setEnabled(enable);
        til_example.setEnabled(enable);
        frameLayout.setVisibility(enable?View.VISIBLE:View.GONE);
        if(world){
            if(enable){
                try {
                    ArrayList<Card> temp_cards = new ArrayList<Card>(cards);
                    editCardAdapter = new EditCardAdapter(temp_cards,Utilities.getCards(this,false));
                    recyclerView.setAdapter(editCardAdapter);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }else{
                WorldCardAdapter worldCardAdapter = new WorldCardAdapter(cards);
                recyclerView.setAdapter(worldCardAdapter);
            }
        }
        til_name.setEnabled(enable);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(til_name.isEnabled()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Вы уверены, что хотите прекратить редактировать?")
                        .setMessage("Все изменения будут потеряны")
                        .setNegativeButton("Да", (dialog, which) -> {
                            initEdits();
                            toggleEdits();
                        })
                        .setPositiveButton("Нет", (dialog, which) -> {
                        }).show();
            }else{
                getOnBackPressedDispatcher().onBackPressed();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        btn.setEnabled(pfp!=null&&!til_name.getEditText().getText().toString().isEmpty() && !til_summary.getEditText().getText().toString().isEmpty() && !til_first_message.getEditText().getText().toString().isEmpty());
    }
}