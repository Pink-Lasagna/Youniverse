package ru.jaromirchernyavsky.youniverse;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
    private String role;
    private String text;

    public ChatMessage(String role, String text) {
        this.role = role;
        this.text = text;
    }

    public String getRole() {
        return role;
    }

    public String getText() {
        return text;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setText(String text) {
        this.text = text;
    }

    //TODO Make nested formatting
    public Spannable getSpannable() {
        SpannableStringBuilder result = new SpannableStringBuilder(text);
        int substring = 0;
        int endstring = 0;
        //Checking for asterisks
        //TODO maybe change for Pattern finder
        while (substring != -1 && endstring != -1) {
            substring = result.toString().indexOf("*");
            endstring = result.toString().indexOf("*", substring + 1);
            if (substring != -1 && endstring != -1) {
                result.setSpan(new StyleSpan(Typeface.ITALIC), substring, endstring, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                result.delete(substring, substring + 1);
                result.delete(endstring - 1, endstring);
            }
            substring = result.toString().indexOf("\\n");
            if (substring != -1) {
                result.replace(substring,substring + 2,System.lineSeparator());
                endstring=0;
            }
        }
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "{\"role\":\"" + role + "\",\"content\":\"" + text + "\"}";
    }

    public ChatMessage(JSONObject json) throws JSONException {
        role = json.getString("role");
        text = json.getString("content");
    }
}
