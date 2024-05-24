package ru.jaromirchernyavsky.youniverse.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import ar.com.hjg.pngj.PngReader;
import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.R;
import ru.jaromirchernyavsky.youniverse.RecyclerAdapter;
import ru.jaromirchernyavsky.youniverse.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    public ArrayList<Card> cards = new ArrayList<>();
    private FragmentHomeBinding binding;
    GridView gridView;
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = getView().findViewById(R.id.recycle);
        adapter = new RecyclerAdapter(cards);
    }

    @Override
    public void onResume() {
        try {
            readCards(getContext());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void readCards(Context context) throws JSONException {
        cards.clear();
        File myDir = new File(context.getFilesDir() + "/saved_images");
        if (!myDir.exists()) {
            boolean success = myDir.mkdirs();
        }
        try{
            myDir.listFiles();
        } catch (Exception e){
                File file = new File (myDir, "example.png");
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(Files.readAllBytes(new File("ru/jaromirchernyavsky/youniverse/cards/example.png").toPath()));
                    out.flush();
                    out.close();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
        }
        for (File file : myDir.listFiles()) {
            PngReader pngr = new PngReader(file);
            String data = pngr.getMetadata().getTxtForKey("chara");
            if(data.isEmpty()) continue;
            Uri uri = Uri.fromFile(file);
            cards.add(new Card(data,uri));
            pngr.close();
        }
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getOrder()){
            case 0:
                break;
            case 1:
                adapter.deleteCard(item.getGroupId());
                break;
        }
        return true;
    }
}