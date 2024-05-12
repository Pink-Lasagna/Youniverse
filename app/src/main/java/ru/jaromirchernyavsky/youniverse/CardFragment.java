package ru.jaromirchernyavsky.youniverse;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import ru.jaromirchernyavsky.youniverse.databinding.FragmentCardBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment extends Fragment {

    private static final String DATA = "";
    private static final String URI = "";

    private String mName;
    private Uri mUri;
    private String mData;
    private String mDescription;

    public CardFragment() {
        // Required empty public constructor
    }
    FragmentCardBinding binding;
    public static CardFragment newInstance(String data, Uri uri) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putString(DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mData = getArguments().getString(DATA);
        }
        try {
            JSONObject jsonObject = new JSONObject(mData.toString());
            mName = jsonObject.getString("name");
            mDescription = jsonObject.getString("description");
            if(mDescription.length()>100){
                mDescription = mDescription.substring(0,100)+"...";
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inf = inflater.inflate(R.layout.fragment_card, container, false);
        MaterialCardView card = inf.findViewById(R.id.card);
        TextView name = inf.findViewById(R.id.name);
        TextView description = inf.findViewById(R.id.description);
        name.setText(mName);
        description.setText(mDescription);
        return inf;
    }
}