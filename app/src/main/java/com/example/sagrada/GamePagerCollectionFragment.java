package com.example.sagrada;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class GamePagerCollectionFragment extends Fragment {
    GamePagerCollectionAdapter gamePagerCollectionAdapter;
    ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceStat){
        return inflater.inflate(R.layout.game_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        gamePagerCollectionAdapter = new GamePagerCollectionAdapter(getChildFragmentManager());
        viewPager = view.findViewById(R.id.GamePager);
        viewPager.setAdapter(gamePagerCollectionAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
