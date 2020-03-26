package com.example.sagrada;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import ViewModel.GameViewModel;

public class GamePagerCollectionAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> pages = new ArrayList<>();
    private List<CharSequence> pagesTitle = new ArrayList<>();
    private GameViewModel gameViewModel;

    public GamePagerCollectionAdapter(FragmentManager fm){
        super(fm);
    }

    public void addFragment(Fragment fragment, String title){
        pages.add(fragment);
        pagesTitle.add(title);
        notifyDataSetChanged();
    }

    public void removeFragment(int index){
        pages.remove(index);
        pagesTitle.remove(index);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int i){
        return pages.get(i);
    }

    @Override
    public int getItemPosition(Object object){
        if (pages.contains(object)){
            return pages.indexOf(object);
        } else {
            return POSITION_NONE;
        }
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public CharSequence getPageTitle(int position){
        return pagesTitle.get(position);
    }
}
