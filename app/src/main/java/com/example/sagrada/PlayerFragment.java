package com.example.sagrada;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlayerFragment extends Fragment {
    //private PlayerViewModel playerViewModel;
    private static final String ARG_COUNT = "Pos";
    private Integer counter;
    private String titleName;

    public PlayerFragment(){}

    public static PlayerFragment newInstance(Integer position){
        PlayerFragment pf = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COUNT, position);
        pf.setArguments(args);
        return  pf;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            counter = getArguments().getInt(ARG_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_object_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        //playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        ((TextView) view.findViewById(R.id.playerNameID)).setText("Unknown");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
