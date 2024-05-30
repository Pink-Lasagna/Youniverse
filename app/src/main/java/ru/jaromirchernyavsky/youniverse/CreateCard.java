package ru.jaromirchernyavsky.youniverse;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import ar.com.hjg.pngj.PngjInputException;
import ru.jaromirchernyavsky.youniverse.adapters.EditCardAdapter;

public class CreateCard extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {
    boolean world = false;
    Uri imageuri;
    ImageView image;
    ArrayList<Card> cards;
    ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        imageuri = data.getData();
                        image.setImageURI(imageuri);
                        try {
                            InputStream iStream =   getContentResolver().openInputStream(imageuri);
                            JSONObject jsonData = Utilities.getMetadataFromFile(iStream);
                            MaterialAlertDialogBuilder alertDialogBuilder = alertDialog();
                            alertDialogBuilder.setPositiveButton("Да",(dialog, which) -> {
                                try {
                                    name.getEditText().setText(jsonData.getString("name"));
                                summary.getEditText().setText(jsonData.getString("description"));
                                first_message.getEditText().setText(jsonData.getString("first_mes"));
                                scenario.getEditText().setText(jsonData.getString("scenario"));
                                example.getEditText().setText(jsonData.getString("mes_example"));
                                ArrayList<Card> jsonCards = new ArrayList<>();
                                try {
                                    jsonCards = Utilities.getCardsFromJsonList(getApplicationContext(),jsonData.getString("characters"));
                                } catch (JSONException e){
                                    jsonCards = null;
                                }
                                if(jsonCards.size()==0) jsonCards=null;
                                if(jsonCards!=null){
                                    materialSwitch.setChecked(true);
                                    world = true;
                                    name.setHint("Название");
                                    summary.setHint("Сюжет мира");
                                    worldlayout.setVisibility(View.VISIBLE);
                                    cards = jsonCards;
                                } else{
                                    materialSwitch.setChecked(false);
                                    world = false;
                                    name.setHint("Имя");
                                    summary.setHint("Описание");
                                    worldlayout.setVisibility(View.GONE);
                                }
                                materialSwitch.setClickable(false);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            alertDialogBuilder.show();
                        } catch (JSONException | FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (PngjInputException ignored){
                        }

                        btn.setEnabled(imageuri!=null&&!name.getEditText().getText().toString().isEmpty() && !summary.getEditText().getText().toString().isEmpty() && !first_message.getEditText().getText().toString().isEmpty());
                    }
                }
            });
    private static final int PERMISSION_CODE = 1001;
    TextInputLayout name;
    TextInputLayout summary;
    TextInputLayout first_message;
    TextInputLayout scenario;
    CircularProgressButton btn;
    TextInputLayout example;
    MaterialSwitch materialSwitch;
    LinearLayout worldlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24);
        setContentView(R.layout.activity_create);
        image = findViewById(R.id.image);
        image.setOnClickListener(v -> {
            //Check permission, if no ask for, if granted launch activity
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if(checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)== PackageManager.PERMISSION_DENIED){
                    String[] permissions = new String[0];
                    permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
                    requestPermissions(permissions,PERMISSION_CODE);
                } else{
                    pickImageFromGallery();
                }
            } else{
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    String[] permissions = new String[0];
                    permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions,PERMISSION_CODE);
                } else{
                    pickImageFromGallery();
                }
            }

        });
        try {
            cards = Utilities.getCards(this,false);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        name = findViewById(R.id.name);
        name.getEditText().setOnFocusChangeListener(this);
        summary = findViewById(R.id.Summary);
        summary.getEditText().setOnFocusChangeListener(this);
        first_message = findViewById(R.id.first_message);
        first_message.getEditText().setOnFocusChangeListener(this);
        scenario = findViewById(R.id.Scenario);
        example = findViewById(R.id.Primer);
        RecyclerView recyclerView = findViewById(R.id.cards);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        worldlayout = findViewById(R.id.worldGroup);
        recyclerView.setLayoutManager(linearLayoutManager);
        EditCardAdapter editCardAdapter = new EditCardAdapter(new ArrayList<Card>(),cards);
        TextView morecards = findViewById(R.id.MoreCards);
        if(cards.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            morecards.setVisibility(View.VISIBLE);
        }
        recyclerView.setAdapter(editCardAdapter);
        materialSwitch = findViewById(R.id.switchView);
        btn = findViewById(R.id.CreateButton);
        materialSwitch.setOnClickListener(this);
        btn.setOnClickListener(v -> {
            btn.startAnimation();
            String result = Utilities.generateMetadata(this,summary,first_message,example,name,scenario,editCardAdapter.getAdded());
            Random random = new Random();
            String typeString = world?"World-":"Character-";
            String filename = typeString+random.nextInt(999999999);
            Utilities.saveCard(this,imageuri, result,filename,world,true);
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImage.launch(intent);
    }
    private MaterialAlertDialogBuilder alertDialog(){
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);
        alert.setTitle("Вы хотите импортировать данные из фото?");
        alert.setMessage("При сканировании фото были найдены данные. Импортировав их, вы потеряете все, что вы вписали");
        alert.setNegativeButton("Нет",(dialog, which) -> {});
        return alert;
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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        btn.setEnabled(imageuri!=null&&!name.getEditText().getText().toString().isEmpty() && !summary.getEditText().getText().toString().isEmpty() && !first_message.getEditText().getText().toString().isEmpty());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(world){
            world = false;
            name.setHint("Имя");
            summary.setHint("Описание");
            worldlayout.setVisibility(View.GONE);
        } else{
            world = true;
            name.setHint("Название");
            summary.setHint("Сюжет мира");
            worldlayout.setVisibility(View.VISIBLE);
        }
    }
}