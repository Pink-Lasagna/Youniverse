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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import ar.com.hjg.pngj.PngjInputException;
import ru.jaromirchernyavsky.youniverse.adapters.EditCardAdapter;
import ru.jaromirchernyavsky.youniverse.adapters.WorldCardAdapter;

public class card_info extends AppCompatActivity implements TextWatcher {
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
    final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        pfp = Objects.requireNonNull(data).getData();
                        image.setImageURI(pfp);
                        try {
                            InputStream iStream =   getContentResolver().openInputStream(pfp);
                            JSONObject jsonData = Utilities.getMetadataFromFile(iStream);
                            MaterialAlertDialogBuilder alert = showDialog();
                            alert.setPositiveButton("Да",(dialog, which) -> {
                                try {
                                    setEdits(jsonData.getString("name"),jsonData.getString("description"),jsonData.getString("first_mes"),jsonData.getString("scenario"),jsonData.getString("mes_example"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                ArrayList<Card> jsonCards;
                                try {
                                    jsonCards = Utilities.getCardsFromJsonList(getApplicationContext(),jsonData.getString("characters"));
                                } catch (JSONException e){
                                    jsonCards = null;
                                }
                                if(jsonCards.size()==0) jsonCards=null;
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
                            });
                            alert.show();
                        } catch (JSONException | FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (PngjInputException ignored){
                        }
                        btn.setEnabled(pfp!=null&&!Objects.requireNonNull(til_name.getEditText()).getText().toString().isEmpty() && !Objects.requireNonNull(til_summary.getEditText()).getText().toString().isEmpty() && !Objects.requireNonNull(til_first_message.getEditText()).getText().toString().isEmpty());
                    }
                }
            });
    private static final int PERMISSION_CODE = 1001;

    /** @noinspection ResultOfMethodCallIgnored*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar = getSupportActionBar();
        Objects.requireNonNull(bar).setDisplayShowTitleEnabled(false);
        bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.main)));
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
                    String[] permissions;
                    permissions = new String[]{android.Manifest.permission.READ_MEDIA_IMAGES};
                    requestPermissions(permissions,PERMISSION_CODE);
                } else{
                    pickImageFromGallery();
                }
            } else{
                if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    String[] permissions;
                    permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions,PERMISSION_CODE);
                } else{
                    pickImageFromGallery();
                }
            }
        });
        Objects.requireNonNull(til_name.getEditText()).addTextChangedListener(this);
        Objects.requireNonNull(til_summary.getEditText()).addTextChangedListener(this);
        Objects.requireNonNull(til_first_message.getEditText()).addTextChangedListener(this);
        Objects.requireNonNull(til_scenario.getEditText()).addTextChangedListener(this);
        Objects.requireNonNull(til_example.getEditText()).addTextChangedListener(this);
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
        btn_edit.setOnClickListener(v -> toggleEdits());
        btn.setOnClickListener(v -> {
            btn.startAnimation();
            Bitmap finalBitmap = Utilities.getContactBitmapFromURI(this,pfp);
            new File(Objects.requireNonNull(pfp.getPath())).delete();
            String metadata;
            if(world){
                metadata = Utilities.generateMetadata(this,til_summary,til_first_message,til_example,til_name,til_scenario,editCardAdapter.getAdded());
                cards = editCardAdapter.getAdded();
            } else{
                metadata = Utilities.generateMetadata(this,til_summary,til_first_message,til_example,til_name,til_scenario, new ArrayList<>());
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
        Objects.requireNonNull(til_name.getEditText()).setText(name);
        Objects.requireNonNull(til_summary.getEditText()).setText(summary);
        Objects.requireNonNull(til_first_message.getEditText()).setText(firstMessage);
        Objects.requireNonNull(til_scenario.getEditText()).setText(scenario);
        Objects.requireNonNull(til_example.getEditText()).setText(example);
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    private void initEdits(){
        try {
            data = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("data")));
            description = data.getString("description");
            firstMessage = data.getString("first_mes");
            scenario = data.getString("scenario");
            name = getIntent().getStringExtra("name");
            pfp = getIntent().getParcelableExtra("uri");
            exampleMessages = data.getString("mes_example");
            world = getIntent().getBooleanExtra("world",false);
            setEdits(name,description,firstMessage,scenario,exampleMessages);
            image.setImageURI(pfp);
            if(world) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                cards = Utilities.getCardsFromJsonList(this,data.getString("characters"));
                if(cards.contains(null)){
                    cards.remove(null);
                    Bitmap finalBitmap = Utilities.getContactBitmapFromURI(this,pfp);
                    new File(Objects.requireNonNull(pfp.getPath())).delete();
                    String metadata = Utilities.generateMetadata(this,til_summary,til_first_message,til_example,til_name,til_scenario, new ArrayList<>());
                    Utilities.saveCard(this,finalBitmap,metadata,pfp.getLastPathSegment(),world,false);
                }
                linearLayout.setVisibility(View.VISIBLE);
                WorldCardAdapter worldCardAdapter = new WorldCardAdapter(cards);
                recyclerView.setAdapter(worldCardAdapter);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Отказано в доступе", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private MaterialAlertDialogBuilder showDialog(){
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getBaseContext());
        alert.setTitle("Вы хотите импортировать данные из фото?");
        alert.setMessage("При сканировании фото были найдены данные. Импортировав их, вы потеряете все, что вы вписали");
        alert.setNegativeButton("Нет",(dialog, which) -> {});
        return alert;
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
                    ArrayList<Card> temp_cards = new ArrayList<>(cards);
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        btn.setEnabled(Objects.requireNonNull(til_name.getEditText()).getText().toString().length()<=20&&pfp!=null&&!Objects.requireNonNull(til_name.getEditText()).getText().toString().isEmpty() && !Objects.requireNonNull(til_summary.getEditText()).getText().toString().isEmpty() && !Objects.requireNonNull(til_first_message.getEditText()).getText().toString().isEmpty());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}