package ru.jaromirchernyavsky.youniverse.ui.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.PngChunkTEXT;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;
import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.MainAdapter;
import ru.jaromirchernyavsky.youniverse.R;
import ru.jaromirchernyavsky.youniverse.databinding.FragmentCardBinding;
import ru.jaromirchernyavsky.youniverse.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private ArrayList<Card> cards = new ArrayList<Card>();
    private FragmentHomeBinding binding;
    GridView gridView;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        gridView = getView().findViewById(R.id.gridView);
        try {
            readCards(getContext());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        showCards(getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cards.clear();
        binding = null;
    }
    private void readCards(Context context) throws JSONException {
        File myDir = new File(context.getFilesDir() + "/saved_images");
        File[] files = myDir.listFiles();
        for (File file : files) {
            PngReader pngr = new PngReader(file);
            String data = pngr.getMetadata().getTxtForKey("chara");
            Uri uri = Uri.fromFile(file);
            cards.add(new Card(data,uri));
            pngr.close();
        }
    }
    private void showCards(Context context){
        MainAdapter adapter = new MainAdapter(context,cards);
        gridView.setAdapter(adapter);
    }
}