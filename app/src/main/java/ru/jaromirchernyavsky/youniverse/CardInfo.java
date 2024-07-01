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
import android.graphics.PorterDuff;
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
import ru.jaromirchernyavsky.youniverse.custom.AIWriter;

public class CardInfo extends AppCompatActivity implements TextWatcher {
    private Uri pfp;
    private JSONObject data;
    private ArrayList<Card> cards = new ArrayList<>();
    private EditCardAdapter editCardAdapter;
    private TextInputLayout name;
    private TextInputLayout summary;
    private ImageView image;
    private TextInputLayout first_message;
    private TextInputLayout scenario;
    private TextInputLayout example;
    private FrameLayout frameLayout;
    private ImageButton magic;
    private RecyclerView recyclerView;
    private LinearLayout linearLayout;
    private CircularProgressButton btn;
    private boolean world;
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
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
                                    name.setHint("Имя");
                                    summary.setHint("Описание");
                                    linearLayout.setVisibility(View.GONE);
                                    cards = jsonCards;
                                } else{
                                    world = false;
                                    name.setHint("Название");
                                    summary.setHint("Сюжет мира");
                                    linearLayout.setVisibility(View.VISIBLE);
                                }
                            });
                            alert.show();
                        } catch (JSONException | FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (PngjInputException ignored){
                        }
                        btn.setEnabled(pfp!=null&&!Objects.requireNonNull(name.getEditText()).getText().toString().isEmpty() && !Objects.requireNonNull(summary.getEditText()).getText().toString().isEmpty() && !Objects.requireNonNull(first_message.getEditText()).getText().toString().isEmpty());
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
        name = findViewById(R.id.name);
        summary = findViewById(R.id.Summary);
        first_message = findViewById(R.id.first_message);
        scenario = findViewById(R.id.Scenario);
        example = findViewById(R.id.Primer);
        frameLayout = findViewById(R.id.btn_layout);
        image = findViewById(R.id.image);
        magic = findViewById(R.id.buttonmagic);
        linearLayout = findViewById(R.id.worldGroup);
        btn = findViewById(R.id.CreateButton);
        ImageButton btn_edit = findViewById(R.id.edit);
        findViewById(R.id.cards);


        ImageButton btn_chat = findViewById(R.id.chat);
        recyclerView = findViewById(R.id.cards);
        initEdits();
        Objects.requireNonNull(name.getEditText()).addTextChangedListener(this);
        Objects.requireNonNull(summary.getEditText()).addTextChangedListener(this);
        Objects.requireNonNull(first_message.getEditText()).addTextChangedListener(this);
        Objects.requireNonNull(scenario.getEditText()).addTextChangedListener(this);
        Objects.requireNonNull(example.getEditText()).addTextChangedListener(this);
        btn_chat.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ViewChats.class);
            intent.putExtra("name",name.getEditText().getText().toString());
            intent.putExtra("uri",pfp);
            intent.putExtra("data",data.toString());
            intent.putExtra("firstMessage",first_message.getEditText().getText().toString());
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
                metadata = Utilities.generateMetadata(this,summary,first_message,example,name,scenario,editCardAdapter.getAdded());
                cards = editCardAdapter.getAdded();
            } else{
                metadata = Utilities.generateMetadata(this,summary,first_message,example,name,scenario, new ArrayList<>());
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
        Objects.requireNonNull(this.name.getEditText()).setText(name);
        Objects.requireNonNull(this.summary.getEditText()).setText(summary);
        Objects.requireNonNull(first_message.getEditText()).setText(firstMessage);
        Objects.requireNonNull(this.scenario.getEditText()).setText(scenario);
        Objects.requireNonNull(this.example.getEditText()).setText(example);
        btn.requestFocus();

    }

    /** @noinspection ResultOfMethodCallIgnored*/
    private void initEdits(){
        try {
            data = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("data")));
            String description = data.getString("description");
            String firstMessage = data.getString("first_mes");
            String scenariostr = data.getString("scenario");
            String namestr = getIntent().getStringExtra("name");
            pfp = getIntent().getParcelableExtra("uri");
            String exampleMessages = data.getString("mes_example");
            world = getIntent().getBooleanExtra("world",false);
            setEdits(namestr, description,firstMessage, scenariostr, exampleMessages);
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
                    String metadata = Utilities.generateMetadata(this,summary,first_message,example,name,scenario, new ArrayList<>());
                    Utilities.saveCard(this,finalBitmap,metadata,pfp.getLastPathSegment(),world,false);
                }
                linearLayout.setVisibility(View.VISIBLE);
                WorldCardAdapter worldCardAdapter = new WorldCardAdapter(cards);
                recyclerView.setAdapter(worldCardAdapter);
            }
            btn.requestFocus();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if(name.isEnabled()){
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Вы уверены, что хотите прекратить редактировать?")
                        .setMessage("Все изменения будут потеряны")
                        .setNegativeButton("Да",(dialog, which) -> {
                            getOnBackPressedDispatcher().onBackPressed();

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
    private void toggleEdits(){
        boolean enable = !name.isEnabled();
        summary.setEnabled(enable);
        first_message.setEnabled(enable);
        scenario.setEnabled(enable);
        example.setEnabled(enable);
        frameLayout.setVisibility(enable?View.VISIBLE:View.GONE);
        if(enable){

            name.getEditText().setOnFocusChangeListener(new AIWriter(magic,name.getHint()));
            name.getEditText().addTextChangedListener((TextWatcher) name.getEditText().getOnFocusChangeListener());

            summary.getEditText().setOnFocusChangeListener(new AIWriter(magic,summary.getHint()));
            summary.getEditText().addTextChangedListener((TextWatcher) summary.getEditText().getOnFocusChangeListener());

            first_message.getEditText().setOnFocusChangeListener(new AIWriter(magic,first_message.getHint()));
            first_message.getEditText().addTextChangedListener((TextWatcher) first_message.getEditText().getOnFocusChangeListener());

            scenario.getEditText().setOnFocusChangeListener(new AIWriter(magic,scenario.getHint()));
            scenario.getEditText().addTextChangedListener((TextWatcher) scenario.getEditText().getOnFocusChangeListener());

            example.getEditText().setOnFocusChangeListener(new AIWriter(magic,example.getHint()));
            example.getEditText().addTextChangedListener((TextWatcher) example.getEditText().getOnFocusChangeListener());
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
            image.setColorFilter(this.getColor(R.color.md_theme_outline), PorterDuff.Mode.MULTIPLY);
            findViewById(R.id.text_add).setVisibility(View.VISIBLE);
        } else{

            image.setOnClickListener(v -> {});
            image.setColorFilter(null);
            findViewById(R.id.text_add).setVisibility(View.GONE);
        }
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
        name.setEnabled(enable);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(name.isEnabled()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Вы уверены, что хотите прекратить редактировать?")
                        .setMessage("Все изменения будут потеряны")
                        .setNegativeButton("Да", (dialog, which) -> {
                            getOnBackPressedDispatcher().onBackPressed();
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
        btn.setEnabled(Objects.requireNonNull(name.getEditText()).getText().toString().length()<=20&&pfp!=null&&!Objects.requireNonNull(name.getEditText()).getText().toString().isEmpty() && !Objects.requireNonNull(summary.getEditText()).getText().toString().isEmpty() && !Objects.requireNonNull(first_message.getEditText()).getText().toString().isEmpty());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}