package ru.jaromirchernyavsky.youniverse.ui.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.PngChunkTEXT;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;
import ru.jaromirchernyavsky.youniverse.databinding.FragmentCardBinding;
import ru.jaromirchernyavsky.youniverse.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private static void readCards(Context context){
        File myDir = new File(context.getFilesDir() + "/saved_images");
        File[] files = myDir.listFiles();
        for(int i = 0;i<files.length;i++){
            PngReader pngr = new PngReader(files[i]);
            String data = pngr.getMetadata().getTxtForKey("chara");
            Uri uri = Uri.fromFile(files[i]);
            pngr.close();
        }
    }
}