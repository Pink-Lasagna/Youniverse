package ru.jaromirchernyavsky.youniverse;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.google.android.material.textfield.TextInputLayout;

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
        for(int i=0;i<jsonArray.length();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            cards.add(getCardFromFileName(context,jsonObject.getString("fileName"),false));
        }
        return cards;
    }

    public static String generateMetadata(String username, TextInputLayout summary, TextInputLayout first_message, TextInputLayout example, TextInputLayout name, TextInputLayout scenario, ArrayList<Card> cards){
        return "{\"data\":{\"alternate_greetings\": [], \"avatar\": \"none\",\"character_version\":\"main\",\"creator\": \""+username+"\",\"creator_notes\": \"\",\"description\": \""+summary.getEditText().getText().toString()+"\",\"first_mes\": \""+first_message.getEditText().getText().toString()+"\",\"mes_example\": \""+example.getEditText().getText().toString()+"\",\"name\": \""+name.getEditText().getText().toString()+"\",\"post_history_instructions\": \"\",\"scenario\": \""+scenario.getEditText().getText().toString()+"\",\"system_prompt\": \"\",\"tags\": [],\"characters\":["+getStringJsonfromCards(cards)+"]},\"spec\": \"chara_card_v2\",\"spec_version\": \"2.0\"}";
    }

    public static Card getCardFromFileName(Context context, String fileName,boolean world) throws JSONException {
        String dir = context.getFilesDir()+"/saved_images/";
        dir += world? "worlds/":"chars/";
        File fileCard = new File(dir+fileName);
        PngReader pngr = new PngReader(fileCard);
        String data = pngr.getMetadata().getTxtForKey("chara");
        Uri uri = Uri.fromFile(fileCard);
        pngr.close();
        return new Card(data,uri,world);
    }

    public static String getStringJsonfromCards(ArrayList<Card> cards){
        String chars = "";
        for(Card card : cards){
            chars += card.toJsonStringFile()+",";
        }
        return chars.substring(0,chars.length()-1);
    }
}
