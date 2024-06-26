package ru.jaromirchernyavsky.youniverse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

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
    public ArrayList<Card> cards;
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    boolean world=true;

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
        try {
            cards = Utilities.getCards(this,world);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        adapter = new RecyclerAdapter(cards);
        recyclerView.setAdapter(adapter);
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
        adapter = new RecyclerAdapter(cards);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        return true;
    }
}