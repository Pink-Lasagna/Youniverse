package ru.jaromirchernyavsky.youniverse.ui.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import java.util.ArrayList;

import ar.com.hjg.pngj.PngReader;
import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.R;
import ru.jaromirchernyavsky.youniverse.RecyclerAdapter;
import ru.jaromirchernyavsky.youniverse.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private ArrayList<Card> cards = new ArrayList<Card>();
    private FragmentHomeBinding binding;
    GridView gridView;
    RecyclerView recyclerView;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = getView().findViewById(R.id.recycle);
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
        RecyclerAdapter adapter = new RecyclerAdapter(cards);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
    }
}