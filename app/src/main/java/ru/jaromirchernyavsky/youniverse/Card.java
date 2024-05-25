package ru.jaromirchernyavsky.youniverse;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

public class Card {
    String data;
    JSONObject convertedData;
    Uri uri;
    String name;
    String description;
    public Card(String data, Uri uri) throws JSONException {
        this.data = data;
        this.uri = uri;
        convertedData = new JSONObject(new JSONObject(new String(Base64.decode(data, Base64.DEFAULT))).getString("data"));
        name = convertedData.getString("name");
        description = convertedData.getString("description");
    }
}
