package com.example.sagrada;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class GameActivity extends AppCompatActivity {
    FragmentStatePagerAdapter fragmentStatePagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.GamePager);
        fragmentStatePagerAdapter = new GamePagerCollectionAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentStatePagerAdapter);
    }
}
