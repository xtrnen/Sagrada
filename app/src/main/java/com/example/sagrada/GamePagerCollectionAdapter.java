package com.example.sagrada;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class GamePagerCollectionAdapter extends FragmentStateAdapter {
    public List<Fragment> pages = new ArrayList<>();
    private List<CharSequence> pagesTitle = new ArrayList<>();
    private int cq;
    private int pq;

    public GamePagerCollectionAdapter(FragmentActivity fa){
        super(fa);
    }

    public void addFragment(Fragment fragment, String title, int _pq){
        pages.add(fragment);
        pagesTitle.add(title);
        pq = _pq;
        notifyDataSetChanged();
    }

    public void removeFragment(int index){
        pages.remove(index);
        pagesTitle.remove(index);
        notifyItemRemoved(index);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PlayerFragment.newInstance(position, pagesTitle.get(position).toString(), pq);
    }

    @Override
    public int getItemCount() {
       return pages.size();
    }

    @Override
    public long getItemId(int position) {
        return pages.get(position).hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        for (Fragment fragment: pages) {
            if ((long)fragment.hashCode() == itemId){
                return true;
            }
        }
        return false;
    }

    public String getTitle(int position){
        return pagesTitle.get(position).toString();
    }
}
