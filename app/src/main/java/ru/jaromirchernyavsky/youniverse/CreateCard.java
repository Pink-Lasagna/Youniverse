package ru.jaromirchernyavsky.youniverse;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RemoteController;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.imaging.formats.png.chunks.PngChunk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.PngChunkTEXT;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;

public class CreateCard extends AppCompatActivity {
    boolean world = false;
    Uri imageuri;
    ImageView image;
    String username;
    ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        imageuri = data.getData();
                        image.setImageURI(imageuri);
                    }
                }
            });
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        image = findViewById(R.id.image);
        image.setOnClickListener(v -> {
            //Check permission, if no ask for, if granted launch activity
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if(checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)== PackageManager.PERMISSION_DENIED){
                    String[] permissions = new String[0];
                    permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
                    requestPermissions(permissions,PERMISSION_CODE);
                } else{
                    pickImageFromGallery();
                }
            } else{
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    String[] permissions = new String[0];
                    permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions,PERMISSION_CODE);
                } else{
                    pickImageFromGallery();
                }
            }

        });
        TextInputLayout name = findViewById(R.id.name);
        TextInputLayout summary = findViewById(R.id.Summary);
        TextInputLayout first_message = findViewById(R.id.first_message);
        TextInputLayout scenario = findViewById(R.id.Scenario);
        TextInputLayout example = findViewById(R.id.Primer);
        LinearLayout characterGroup = findViewById(R.id.characterGroup);
        findViewById(R.id.world).setOnClickListener(v -> {
            world = true;
            name.setHint("Название");
            characterGroup.setVisibility(View.GONE);
        });
        findViewById(R.id.character).setOnClickListener(v -> {
            world = false;
            name.setHint("Имя");
            characterGroup.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.CreateButton).setOnClickListener(v -> {
            String result = "{\"data\":{\"alternate_greetings\": [], \"avatar\": \"none\",\"character_version\":\"main\",\"creator\": \""+username+"\",\"creator_notes\": \"\",\"description\": \""+summary.getEditText().getText()+"\",\"first_mes\": \""+first_message.getEditText().getText()+"\",\"mes_example\": \""+example.getEditText().getText()+"\",\"name\": \""+name.getEditText().getText()+"\",\"post_history_instructions\": \"\",\"scenario\": \""+scenario.getEditText().getText()+"\",\"system_prompt\": \"\",\"tags\": []},\"spec\": \"chara_card_v2\",\"spec_version\": \"2.0\"}";
            saveCard(this,getContactBitmapFromURI(this,imageuri), result);
            this.finish();
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImage.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Отказано в доступе", Toast.LENGTH_SHORT).show();
                }
        }
    }
    public Bitmap getContactBitmapFromURI(Context context, Uri uri) {
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
    private static void saveCard(Context context, Bitmap finalBitmap, String metadata){
        String base64 = Base64.encodeToString(metadata.getBytes(),Base64.CRLF);
        Random rand = new Random();
        String fname = "Image-"+ rand.nextInt(999999999);
        System.out.println(fname);
        File myDir = new File(context.getFilesDir() + "/saved_images");
        if (!myDir.exists()) {
            boolean success = myDir.mkdirs();
        }
        File file = new File (myDir, fname+".png");
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File destFile = new File (myDir, fname+"1.png");
        PngReader pngr = new PngReader(file);
        PngWriter pngw = new PngWriter(destFile, pngr.imgInfo, true);
        PngChunkTextVar pngctv = new PngChunkTEXT(pngr.imgInfo);
        pngctv.setKeyVal("chara",base64);
        pngctv.setPriority(true);
        pngw.getMetadata().queueChunk(pngctv);
        System.out.println(pngr.getMetadata().getTxtForKey("chara"));
        pngw.copyChunksFrom(pngr.getChunksList(), ChunkCopyBehaviour.COPY_ALL);
        for (int row = 0; row < pngr.imgInfo.rows; row++) {
            IImageLine l1 = pngr.readRow();
            pngw.writeRow(l1);
        }
        pngr.end();
        pngw.end();
        boolean deleted = file.delete();
    }

}