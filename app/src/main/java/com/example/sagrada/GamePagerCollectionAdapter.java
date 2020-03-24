package com.example.sagrada;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class GamePagerCollectionAdapter extends FragmentStatePagerAdapter {


    public GamePagerCollectionAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int i){
        Fragment fragment = new PlayerObjectFragment();
        Bundle args = new Bundle();

        args.putInt(PlayerObjectFragment.SObject, i + 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return "Object" + (position + 1);
    }
}
