package ru.jaromirchernyavsky.youniverse;

import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

import org.apache.commons.text.StringEscapeUtils;

public class ChatMessage {
    private final String role;
    private String text;
    private String pfp;

    public ChatMessage(String role, String text, Object pfp) {
        this.role = role;
        this.text = text;
        this.pfp = pfp.toString();
    }

    public String getRole() {
        return role;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    //TODO Make nested formatting
    public Spannable getSpannable() {
        SpannableStringBuilder result = new SpannableStringBuilder(text);
        int substring = 0;
        int endstring;
        //Checking for asterisks
        //TODO maybe change for Pattern finder
        while (substring != -1) {
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
            }
        }
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        if(role=="assistant"||role=="system"||role=="user")
            return "{\"role\":\"" + role + "\",\"content\":\"" + StringEscapeUtils.escapeJava(text) + "\"}";
        return  "{\"role\":\"assistant\",\"content\":\""+role+":" + StringEscapeUtils.escapeJava(text) + "\"}";
    }

    public String getPfp() {
        return pfp;
    }
}
