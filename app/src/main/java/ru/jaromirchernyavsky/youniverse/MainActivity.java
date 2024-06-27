package ru.jaromirchernyavsky.youniverse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.adapters.RecyclerAdapter;
import ru.jaromirchernyavsky.youniverse.custom.DeleteConfirmation;
import ru.jaromirchernyavsky.youniverse.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ArrayList<Card> cards;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private boolean world=true;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateCard.class);
            this.startActivity(intent);
        });

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnItemSelectedListener(v->{
            world= v.getItemId() == R.id.navigation_home;
            getCards();
            return true;
        });

        EditText search = findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filter(search.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        findViewById(R.id.account).setOnClickListener(v -> {
            Intent intent = new Intent(this, Account.class);
            startActivity(intent);
        });

        textView = findViewById(R.id.textView);
    }
    public void filter(String text){
        ArrayList<Card> temp = new ArrayList<>();
        if(cards==null) return;
        for(Card d: cards){
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if(d.name.toLowerCase().contains(text.toLowerCase())){
                temp.add(d);
            }
        }
        //update recyclerview
        adapter.updateList(temp);
    }
    @Override
    protected void onResume() {
        SharedPreferences prefs = getSharedPreferences("UserInfo", 0);
        boolean previouslyStarted = prefs.getBoolean("launched", false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("launched", true);
            MaterialAlertDialogBuilder alert = getMaterialAlertDialogBuilder(edit);
            alert.show();
        }
        getCards();
        super.onResume();
    }
    private @NonNull MaterialAlertDialogBuilder getMaterialAlertDialogBuilder(SharedPreferences.Editor edit) {
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);
        alert.setTitle("Введите свое имя");
        alert.setView(input);
        alert.setPositiveButton("Выбрать", (dialog, which) -> {
            edit.putString("username", input.getText().toString());
            edit.apply();
        });
        return alert;
    }

    private void getCards(){
        try {
            cards = Utilities.getCards(this,world);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if(cards.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
            if (world) textView.setText(R.string.world_text);
            else textView.setText(R.string.pers_text);
        } else{
            textView.setVisibility(View.GONE);
        }
        adapter = new RecyclerAdapter(cards);
        recyclerView.setAdapter(adapter);
    }

}