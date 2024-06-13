package ru.jaromirchernyavsky.youniverse;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Card {
    public final JSONObject convertedData;
    public final Uri uri;
    public String name;
    public String description;
    public final boolean world;
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

    /** @noinspection EqualsWhichDoesntCheckParameterClass*/
    @Override
    public boolean equals(Object o) {
        Card card = (Card) o;
        return Objects.requireNonNull(uri.getLastPathSegment()).equals(Objects.requireNonNull(card.uri.getLastPathSegment()));
    }
}
