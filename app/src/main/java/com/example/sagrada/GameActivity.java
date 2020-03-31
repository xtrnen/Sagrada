package com.example.sagrada;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import Model.GameBoard.Player;
import ViewModel.GameViewModel;

public class GameActivity extends AppCompatActivity implements CreatePlayerDialogFragment.ICreatePlayerDialogListener {
    GamePagerCollectionAdapter gamePagerCollectionAdapter;
    GameViewModel gameViewModel;
    ViewPager2 viewPager;
    TabLayout tabLayout;
    //TODO: Dialog reacts to click anywhere with dismiss of dialog...
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);

        /*Toolbar*/
        Toolbar toolbar = (Toolbar)findViewById(R.id.GameMenuToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*ImageButtons in ActionBar*/
        ImageButton deletePlayerButton = (ImageButton) findViewById(R.id.playerToolbarDeleteButton);
        //ImageButton addPlayerButton = (ImageButton) findViewById(R.id.playerT)
        ImageButton playerImageInfoButton = (ImageButton) findViewById(R.id.playerToolbarInfoButton);
        ImageButton playerCameraButton = (ImageButton) findViewById(R.id.playerToolbarCameraButton);
        //set onClick actions
        deletePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.INFO, "MenuOption", "Delete clicked");
                removePlayerPage(viewPager.getCurrentItem());
            }
        });
        playerImageInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.INFO, "Toolbar", "Player info");
            }
        });
        playerCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.INFO, "MenuOption", "Take picture");
            }
        });

        /*Show Creation Dialog so we create first user*/
        ShowCreatePlayerDialog();

        viewPager = (ViewPager2) findViewById(R.id.GamePager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        gamePagerCollectionAdapter = new GamePagerCollectionAdapter(this);
        viewPager.setAdapter(gamePagerCollectionAdapter);
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                String title = gamePagerCollectionAdapter.getTitle(position);
                tab.setText(title);
            }
        }).attach();

        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
    }

    /*CREATE PLAYER DIALOG*/
    public void ShowCreatePlayerDialog(){
        DialogFragment gameModeDialog = new CreatePlayerDialogFragment();
        gameModeDialog.show(getSupportFragmentManager(), "CreatePlayerDialog");
    }

    @Override
    public void onCreatePlayerSubmit(String username) {
        gameViewModel.addPlayer(new Player(username));
        PlayerFragment playerFragment = new PlayerFragment();
        gamePagerCollectionAdapter.addFragment(playerFragment, username);
        viewPager.setCurrentItem(gamePagerCollectionAdapter.getItemCount());
    }

    @Override
    public void onCreatePlayerCanceled() {
        List<Player> existingPlayers = gameViewModel.getPlayers().getValue();
        if(existingPlayers == null || existingPlayers.isEmpty()){
            Intent intent = new Intent(GameActivity.this, MenuActivity.class);
            this.finish();
            startActivity(intent);
        }
    }

    /*APPBAR OPTIONS MENU*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu_layout, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.GameMenuNewUserItem:
                ShowCreatePlayerDialog();
                return true;
            case R.id.GameMenuQuitGame:
                Log.println(Log.INFO, "MenuOption", "Quit clicked");
                Intent intent = new Intent(GameActivity.this, MenuActivity.class);
                this.finish();
                startActivity(intent);
            case R.id.GameMenuCurrentUserDelete:
                Log.println(Log.INFO, "MenuOption", "Delete clicked");
                removePlayerPage(viewPager.getCurrentItem());
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*UNSORTED*/
    private void removePlayerPage(int position){
        String currentUsername = gamePagerCollectionAdapter.getTitle(position);
        gamePagerCollectionAdapter.removeFragment(position);
        gameViewModel.removePlayer(currentUsername);
    }
}
