package com.example.sagrada;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import ViewModel.GameViewModel;

public class GamePagerCollectionAdapter extends FragmentStateAdapter {
    private List<Fragment> pages = new ArrayList<>();
    private List<CharSequence> pagesTitle = new ArrayList<>();
    private GameViewModel gameViewModel;

    public GamePagerCollectionAdapter(FragmentActivity fa){
        super(fa);
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

    /*@Override
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
    public CharSequence getPageTitle(int position){
        return pagesTitle.get(position);
    }*/

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PlayerFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
       return pages.size();
    }

    public String getTitle(int position){
        return pagesTitle.get(position).toString();
    }
}
