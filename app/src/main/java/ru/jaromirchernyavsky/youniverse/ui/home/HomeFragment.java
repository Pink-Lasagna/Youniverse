package ru.jaromirchernyavsky.youniverse.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.json.JSONException;

import java.util.ArrayList;

import ru.jaromirchernyavsky.youniverse.Card;
import ru.jaromirchernyavsky.youniverse.R;
import ru.jaromirchernyavsky.youniverse.adapters.RecyclerAdapter;
import ru.jaromirchernyavsky.youniverse.Utilities;
import ru.jaromirchernyavsky.youniverse.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    public ArrayList<Card> cards = new ArrayList<>();
    private FragmentHomeBinding binding;
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }


    @Override
    public void onResume() {
        recyclerView = getView().findViewById(R.id.recycle);
        try {
            cards = Utilities.getCards(getContext(),true);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        adapter = new RecyclerAdapter(cards);
        recyclerView.setAdapter(adapter);
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getOrder()){
            case 0:
                Utilities.addImageToGallery(getContext(),cards.get(item.getItemId()).uri);
                break;
            case 1:
                adapter.deleteCard(item.getGroupId());
                break;
        }
        return true;
    }
}