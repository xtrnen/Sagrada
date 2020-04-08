package com.example.sagrada;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Model.GameBoard.Player;
import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import ViewModel.GameViewModel;

public class GameActivity extends AppCompatActivity implements CreatePlayerDialogFragment.ICreatePlayerDialogListener, DeletePlayerDialogFragment.IDeletePlayerDialogListener {
    GamePagerCollectionAdapter gamePagerCollectionAdapter;
    GameViewModel gameViewModel;
    ViewPager2 viewPager;
    TabLayout tabLayout;
    public int cqFlag;
    //TODO: Dialog reacts to click anywhere with dismiss of dialog...

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);
        /*Toolbar*/
        Toolbar toolbar = findViewById(R.id.GameMenuToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameViewModel.addContext(this);

        /*ImageButtons in ActionBar*/
        ImageButton deletePlayerButton = findViewById(R.id.playerToolbarDeleteButton);
        ImageButton createPlayerButton = findViewById(R.id.playerToolbarCreatePlayer);
        ImageButton addCQButton = findViewById(R.id.gameToolbarAddCQID);
        //set onClick actions
        deletePlayerButton.setOnClickListener(v -> ShowDeletePlayerDialog());
        createPlayerButton.setOnClickListener(v -> ShowCreatePlayerDialog());
        addCQButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.cq_menu_layout, popupMenu.getMenu());
            popupMenu.getMenu().clear();
            addItemsToMenu(popupMenu.getMenu(), Arrays.asList(getResources().getStringArray(R.array.groupQuestStrings)));
            popupMenu.setOnMenuItemClickListener(item -> {
                gameViewModel.addCommonQuest(item.getItemId());
                return true;
            });
            popupMenu.show();
        });
        /*Show Creation Dialog so we create first user*/
        ShowCreatePlayerDialog();

        viewPager = (ViewPager2) findViewById(R.id.GamePager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        gamePagerCollectionAdapter = new GamePagerCollectionAdapter(this);
        viewPager.setAdapter(gamePagerCollectionAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            String title = gamePagerCollectionAdapter.getTitle(position);
            tab.setText(title);
        }).attach();
    }

    /*Dialogs creation*/
    public void ShowCreatePlayerDialog(){
        DialogFragment gameModeDialog;
        if(gameViewModel.getPlayers().getValue() == null){
            gameModeDialog = new CreatePlayerDialogFragment(true);
        } else {
            gameModeDialog = new CreatePlayerDialogFragment(false);
        }
        gameModeDialog.show(getSupportFragmentManager(), "CreatePlayerDialog");
    }
    public void ShowDeletePlayerDialog(){
        DialogFragment deletePlayerDialog = new DeletePlayerDialogFragment();
        deletePlayerDialog.show(getSupportFragmentManager(), "DeletePlayerDialog");
    }

    /*Dialog interface functions implementation*/
    @Override
    public void onCreatePlayerSubmit(String username, int cqIndex, int pqIndex) {
        gameViewModel.addPlayer(new Player(username));
        PlayerFragment playerFragment = new PlayerFragment();
        if(cqIndex != -42){
            //cqFlag = cqIndex;
            gameViewModel.addCommonQuest(cqIndex);
        }
        gamePagerCollectionAdapter.addFragment(playerFragment, username, cqFlag, pqIndex);
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
    @Override
    public void onDeletePlayerAgreed() {
        removePlayerPage(viewPager.getCurrentItem());
    }
    @Override
    public void onDeletePlayerCanceled() {}


    /*APPBAR OPTIONS MENU*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu_layout, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.GameMenuHelp:
                Log.println(Log.INFO, "MenuOption", "Help msg!");
                //TODO: Dialog or activity with guidelines
                return true;
            case R.id.GameMenuQuitGame:
                Log.println(Log.INFO, "MenuOption", "Quit clicked");
                Intent intent = new Intent(GameActivity.this, MenuActivity.class);
                this.finish();
                startActivity(intent);
            case R.id.GameMenuSettings:
                Log.println(Log.INFO, "MenuOption", "Settings clicked");
                //TODO: Go to settings activity
                return true;
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

    private void addItemsToMenu(Menu menu, List<String>titles){
        for (int order = 0; order < titles.size(); order++){
            menu.add(0,order, 0, titles.get(order));
        }
    }
}
