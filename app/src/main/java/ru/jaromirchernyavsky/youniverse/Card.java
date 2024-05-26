package ru.jaromirchernyavsky.youniverse;

import android.net.Uri;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Objects;

public class Card {
    String data;
    JSONObject convertedData;
    Uri uri;
    String name;
    String description;
    boolean world;
    public Card(String data, Uri uri, boolean world) throws JSONException {
        this.data = data;
        this.uri = uri;
        this.world = world;
        convertedData = new JSONObject(new JSONObject(new String(Base64.decode(data, Base64.DEFAULT))).getString("data"));
        name = convertedData.getString("name");
        description = convertedData.getString("description");
    }

    public String toJsonStringFile(){
        return "{\"fileName\":\""+uri.getLastPathSegment()+"\"}";
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        Card card = (Card) o;
        return uri.getLastPathSegment().toString().equals(card.uri.getLastPathSegment().toString());
    }
}
