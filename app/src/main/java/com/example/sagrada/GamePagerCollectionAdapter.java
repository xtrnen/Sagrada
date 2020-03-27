package com.example.sagrada;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class GamePagerCollectionAdapter extends FragmentStateAdapter {
    private List<Fragment> pages = new ArrayList<>();
    private List<CharSequence> pagesTitle = new ArrayList<>();

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

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PlayerFragment.newInstance(position, pagesTitle.get(position).toString());
    }

    @Override
    public int getItemCount() {
       return pages.size();
    }

    public String getTitle(int position){
        return pagesTitle.get(position).toString();
    }
}
