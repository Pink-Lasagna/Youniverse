package ru.jaromirchernyavsky.youniverse;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.adapters.EditCardAdapter;
import ru.jaromirchernyavsky.youniverse.adapters.WorldCardAdapter;

public class card_info extends AppCompatActivity  {
    String name;
    Uri pfp;
    JSONObject data;

    String description;
    String firstMessage;
    String scenario;
    String exampleMessages;
    String userPersona;
    ArrayList<Card> cards = new ArrayList<>();
    EditCardAdapter editCardAdapter;
    TextInputLayout til_name;
    TextInputLayout til_summary;
    TextInputLayout til_first_message;
    TextInputLayout til_scenario;
    TextInputLayout til_example;
    FrameLayout frameLayout;
    RecyclerView recyclerView;
    boolean world;

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
        ImageView image = findViewById(R.id.image);
        LinearLayout linearLayout = findViewById(R.id.worldGroup);
        CircularProgressButton btn = findViewById(R.id.CreateButton);
        ImageButton btn_edit = findViewById(R.id.edit);
        findViewById(R.id.cards);
        ImageButton btn_chat = findViewById(R.id.chat);
        recyclerView = findViewById(R.id.cards);
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
                    String metadata = Utilities.generateMetadata("",til_summary,til_first_message,til_example,til_name,til_scenario,editCardAdapter.getAdded());
                    Utilities.saveCard(this,finalBitmap,metadata,pfp.getLastPathSegment(),world,false);
                }
                linearLayout.setVisibility(View.VISIBLE);
                WorldCardAdapter worldCardAdapter = new WorldCardAdapter(cards);
                recyclerView.setAdapter(worldCardAdapter);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        til_name.getEditText().setText(name);
        til_summary.getEditText().setText(description);
        til_first_message.getEditText().setText(firstMessage);
        til_scenario.getEditText().setText(scenario);
        til_example.getEditText().setText(exampleMessages);
        image.setImageURI(pfp);
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
            String metadata = Utilities.generateMetadata("",til_summary,til_first_message,til_example,til_name,til_scenario,editCardAdapter.getAdded());
            Utilities.saveCard(this,finalBitmap,metadata,pfp.getLastPathSegment(),world,false);
            cards = editCardAdapter.getAdded();
            toggleEdits();
        });
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
}