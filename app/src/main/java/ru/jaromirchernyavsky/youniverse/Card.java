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
    public JSONObject convertedData;
    public Uri uri;
    public String name;
    public String description;
    public boolean world;
    public Card(JSONObject convertedData, Uri uri, boolean world) throws JSONException {
        this.uri = uri;
        this.world = world;
        this.convertedData = convertedData;
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
