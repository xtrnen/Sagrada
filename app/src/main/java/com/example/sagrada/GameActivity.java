package com.example.sagrada;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import Model.GameBoard.Player;

public class GameActivity extends AppCompatActivity implements CreatePlayerDialogFragment.ICreatePlayerDialogListener {
    FragmentStatePagerAdapter fragmentStatePagerAdapter;
    Player[] players;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);

        ShowCreatePlayerDialog();

        ViewPager viewPager = (ViewPager) findViewById(R.id.GamePager);
        fragmentStatePagerAdapter = new GamePagerCollectionAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentStatePagerAdapter);
    }

    public void ShowCreatePlayerDialog(){
        DialogFragment gameModeDialog = new CreatePlayerDialogFragment();
        gameModeDialog.show(getSupportFragmentManager(), "CreatePlayerDialog");
    }

    @Override
    public void onCreatePlayerSubmit(String username) {
        Log.println(Log.INFO, "CreatePlayerSubmit", "Name :" + username);
    }

    @Override
    public void onCreatePlayerCanceled() {
        this.finish();
    }
}
