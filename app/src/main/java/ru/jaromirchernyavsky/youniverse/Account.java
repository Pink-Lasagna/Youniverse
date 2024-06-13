package ru.jaromirchernyavsky.youniverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;

import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class Account extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ActionBar bar = getSupportActionBar();
        Objects.requireNonNull(bar).setDisplayShowTitleEnabled(false);
        bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.main)));
        bar.setDisplayHomeAsUpEnabled(true);
        TextInputLayout etUser = findViewById(R.id.name);
        TextInputLayout etDescription = findViewById(R.id.description);
        Objects.requireNonNull(etUser.getEditText()).setText(Utilities.getUsername(this));
        Objects.requireNonNull(etDescription.getEditText()).setText(Utilities.getDescription(this));
        CircularProgressButton btn = findViewById(R.id.create);
        btn.setOnClickListener(v -> {
            Utilities.storeUsernameDescription(this,etUser.getEditText().getText().toString(), etDescription.getEditText().getText().toString());
            this.finish();
        });
        etUser.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn.setEnabled(s.length()>0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}