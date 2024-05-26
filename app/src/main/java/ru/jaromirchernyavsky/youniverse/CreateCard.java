package ru.jaromirchernyavsky.youniverse;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Random;

public class CreateCard extends AppCompatActivity implements View.OnFocusChangeListener {
    boolean world = false;
    Uri imageuri;
    ImageView image;
    ArrayList<Card> cards;
    String username;
    ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.i("gs",result.toString());
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        imageuri = data.getData();
                        image.setImageURI(imageuri);
                        btn.setEnabled(imageuri!=null&&!name.getEditText().getText().toString().isEmpty() && !summary.getEditText().getText().toString().isEmpty() && !first_message.getEditText().getText().toString().isEmpty());
                    }
                }
            });
    private static final int PERMISSION_CODE = 1001;
    TextInputLayout name;
    TextInputLayout summary;
    TextInputLayout first_message;
    CircularProgressButton btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
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
        TextInputLayout scenario = findViewById(R.id.Scenario);
        TextInputLayout example = findViewById(R.id.Primer);
        RecyclerView recyclerView = findViewById(R.id.cards);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        LinearLayout worldlayout = findViewById(R.id.worldGroup);
        recyclerView.setLayoutManager(linearLayoutManager);
        EditCardAdapter editCardAdapter = new EditCardAdapter(new ArrayList<Card>(),cards);
        recyclerView.setAdapter(editCardAdapter);
        findViewById(R.id.world).setOnClickListener(v -> {
            world = true;
            name.setHint("Название");
            summary.setHint("Сюжет мира");
            worldlayout.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.character).setOnClickListener(v -> {
            world = false;
            name.setHint("Имя");
            summary.setHint("Описание");
            worldlayout.setVisibility(View.GONE);
        });
        btn = findViewById(R.id.CreateButton);
        btn.setOnClickListener(v -> {
            btn.startAnimation();
            String result = Utilities.generateMetadata(username,summary,first_message,example,name,scenario,editCardAdapter.getAdded());
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
}