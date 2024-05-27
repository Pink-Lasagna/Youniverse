package ru.jaromirchernyavsky.youniverse;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.PngjInputException;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.PngChunkTEXT;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;

public class Utilities {
    public static void saveCard(Activity context, Uri uri, String metadata, String fileName, boolean world, boolean endActivity){
        Bitmap finalBitmap = getContactBitmapFromURI(context.getApplicationContext(),uri);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String name = fileName;
            if(fileName.endsWith(".png")){
                name = fileName.substring(0,fileName.length()-4);
            }
            String base64 = Base64.encodeToString(metadata.getBytes(), Base64.CRLF);

            String dir = context.getFilesDir()+"/saved_images/";
            dir += world? "worlds/":"chars/";
            File file = new File(dir+name+".png");
            if (!new File(dir).exists()) {
                new File(dir).mkdirs();
            }
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            File destFile = new File(dir + name+"1.png");
            PngReader pngr = new PngReader(file);
            PngWriter pngw = new PngWriter(destFile, pngr.imgInfo, true);
            PngChunkTextVar pngctv = new PngChunkTEXT(pngr.imgInfo);
            pngctv.setKeyVal("chara", base64);
            pngctv.setPriority(true);
            pngw.getMetadata().queueChunk(pngctv);
            pngw.copyChunksFrom(pngr.getChunksList(), ChunkCopyBehaviour.COPY_TEXTUAL);
            for (int row = 0; row < pngr.imgInfo.rows; row++) {
                IImageLine l1 = pngr.readRow();
                pngw.writeRow(l1);
            }
            pngr.end();
            pngw.end();
            file.delete();
            try {
                renameFile(destFile.toString(),name+".png");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(endActivity) context.finish();
        });
    }

    public static void saveCard(Activity context, Bitmap finalBitmap, String metadata, String fileName, boolean world, boolean endActivity){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String name = fileName;
            if(fileName.endsWith(".png")){
                name = fileName.substring(0,fileName.length()-4);
            }
            String base64 = Base64.encodeToString(metadata.getBytes(), Base64.CRLF);

            String dir = context.getFilesDir()+"/saved_images/";
            dir += world? "worlds/":"chars/";
            File file = new File(dir+name+".png");
            if (!new File(dir).exists()) {
                new File(dir).mkdirs();
            }
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            File destFile = new File(dir + name+"1.png");
            PngReader pngr = new PngReader(file);
            PngWriter pngw = new PngWriter(destFile, pngr.imgInfo, true);
            PngChunkTextVar pngctv = new PngChunkTEXT(pngr.imgInfo);
            pngctv.setKeyVal("chara", base64);
            pngctv.setPriority(true);
            pngw.getMetadata().queueChunk(pngctv);
            pngw.copyChunksFrom(pngr.getChunksList(), ChunkCopyBehaviour.COPY_TEXTUAL);
            for (int row = 0; row < pngr.imgInfo.rows; row++) {
                IImageLine l1 = pngr.readRow();
                pngw.writeRow(l1);
            }
            pngr.end();
            pngw.end();
            file.delete();
            try {
                renameFile(destFile.toString(),name+".png");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(endActivity) context.finish();
        });
    }

    public static void renameFile(String dir, String newName) throws IOException {
        Path file = Paths.get(dir);
        Files.move(file,file.resolveSibling(newName));
    }

    public static Bitmap getContactBitmapFromURI(Context context, Uri uri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            if (input == null) {
                return null;
            }
            return BitmapFactory.decodeStream(input);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Card> getCards(Context context,boolean world) throws JSONException {
        ArrayList<Card> cards = new ArrayList<>();
        String dir = context.getFilesDir()+"/saved_images/";
        dir += world? "worlds/":"chars/";
        if (!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        for (File file : new File(dir).listFiles()) {
            cards.add(getCardFromFileName(context,file.getName(),world));
        }
        return cards;
    }

    public static ArrayList<Card> getCardsFromJsonList(Context context, String json) throws JSONException {
        ArrayList<Card> cards = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            try{
                cards.add(getCardFromFileName(context, jsonObject.getString("fileName"), false));
            } catch (PngjInputException e){
                cards.add(null);
            }
        }
        return cards;
    }

    public static String generateMetadata(Context context,  TextInputLayout summary, TextInputLayout first_message, TextInputLayout example, TextInputLayout name, TextInputLayout scenario, ArrayList<Card> cards){
        String username = getName(context);
        return "{\"data\":{\"alternate_greetings\": [], \"avatar\": \"none\",\"character_version\":\"main\",\"creator\": \""+username+"\",\"creator_notes\": \"\",\"description\": \""+summary.getEditText().getText().toString()+"\",\"first_mes\": \""+first_message.getEditText().getText().toString()+"\",\"mes_example\": \""+example.getEditText().getText().toString()+"\",\"name\": \""+name.getEditText().getText().toString()+"\",\"post_history_instructions\": \"\",\"scenario\": \""+scenario.getEditText().getText().toString()+"\",\"system_prompt\": \"\",\"tags\": [],\"characters\":["+getStringJsonfromCards(cards)+"]},\"spec\": \"chara_card_v2\",\"spec_version\": \"2.0\"}";
    }

    public static Card getCardFromFileName(Context context, String fileName,boolean world) throws JSONException {
        String dir = context.getFilesDir()+"/saved_images/";
        dir += world? "worlds/":"chars/";
        File fileCard = new File(dir+fileName);
        JSONObject data = getMetadataFromFile(fileCard.toString());
        Uri uri = Uri.fromFile(fileCard);
        return new Card(data,uri,world);
    }

    public static String getMessages(ArrayList<ChatMessage> chatMessages){
        String result = "";
        for(ChatMessage msg : chatMessages){
            result+=","+msg;
        }
        return result;
    }

    public static String getName(Context context){
        SharedPreferences pref = context.getSharedPreferences("UserInfo",0);
        String username = pref.getString("username","user");
        return username;
    }
    public static JSONObject getMetadataFromFile(String filePath) throws JSONException {
        File file = new File(filePath);
        PngReader pngr = new PngReader(file);
        String result = pngr.getMetadata().getTxtForKey("chara");
        JSONObject data = new JSONObject(new JSONObject(new String(Base64.decode(result, Base64.DEFAULT))).getString("data"));;
        pngr.close();
        return data;
    }

    public static JSONObject getMetadataFromFile(InputStream inputStream) throws JSONException {
        PngReader pngr = new PngReader(inputStream);
        String result = pngr.getMetadata().getTxtForKey("chara");
        JSONObject data = new JSONObject(new JSONObject(new String(Base64.decode(result, Base64.DEFAULT))).getString("data"));;
        pngr.close();
        return data;
    }

    public static String getStringJsonfromCards(ArrayList<Card> cards){
        String chars = "";
        if(cards.isEmpty()) return "";
        for(Card card : cards){
            chars += card.toJsonStringFile()+",";
        }
        return chars.substring(0,chars.length()-1);
    }

    public static ArrayList<ChatMessage> getStoredMessages(Context context,String TAG,int chatid){
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, 0);
        String serializedData = sharedPreferences.getString(Integer.toString(chatid), null);
        return serializedData==null?null:gson.fromJson(serializedData,new TypeToken<ArrayList<ChatMessage>>(){}.getType());
    }

    public static void storeMessages(Context context, ArrayList<ChatMessage> messages,String TAG,int chatid){
        Gson gson = new Gson();

        SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putString(Integer.toString(chatid), gson.toJson(messages));
        edit.apply();
    }


    public static String charSysPrompt(String name,String description, String scenario, String exampleMessages, String username, String userPersona){
        return StringEscapeUtils.escapeJava("Притворись персонажем с именем "+name+", вот его краткое описание\n:"+description+"\n Твоя задача - разговаривать с пользователем с именем "+username+", который описан как "+userPersona+
                ". Ты должен придерживаться следующей предыстории и текущей ситуации:\n Предыстория и сценарий:\n "+scenario+
                "\nПример сообщений:\n "+exampleMessages+
                "Ты должен оставаться в образе "+name+" на протяжении всего разговора, учитывать предысторию и текущую ситуацию," +
                " а также поддерживать стиль и тон сообщений, приведенных в примере. " +
                "Используй активное слушание, задавай вопросы и поддерживай диалог в рамках заданного сценария и в стиле"+name+
                "В каждом своем ответе описывай свои действия и обособляй их звездочками. Например, *улыбается* или *показывает на старинное здание*.");
    }

    public static String worldSysPrompt(String name,String description, String scenario, String exampleMessages, String username, String userPersona){
        return StringEscapeUtils.escapeJava(String.format(
                        "Притворись миром с именем \"%s\". Твоя задача - описывать и развивать мир вокруг пользователя с именем \"%s\", который описан как \"%s\". Ты должен придерживаться следующей предыстории и текущей ситуации:\\n" +
                        "Описание и сюжет:\n" +
                        "\"%s\"\n" +
                        "Предыстория и сценарий:\n" +
                        "\"%s\"\n" +
                        "Пример сообщений:\n" +
                        "\"%s\"\n" +
                        "Ты должен генерировать действия существ, находящихся в мире \"%s\", и описывать последствия действий персонажа \"%s\". Также ты должен вкратце и детализировано описывать сцену вокруг пользователя в данный момент, используя яркие эпитеты и образы. Генерируй речь существ и персонажей, которых встречает пользователь, придавая им уникальные голоса и характерные черты. Обособляй все описания и действия звездочками *, кроме прямой речи персонажей.",
                name, username, userPersona, description, scenario, exampleMessages, name, username));
    }
}
