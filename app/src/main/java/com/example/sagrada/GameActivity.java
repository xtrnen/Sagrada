package com.example.sagrada;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sagrada.Components.GamePagerCollectionAdapter;
import com.example.sagrada.Fragments.Dialogs.CreatePlayerDialogFragment;
import com.example.sagrada.Fragments.Dialogs.DeletePlayerDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
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
    public final static int REQUEST_SLOTS = 1;
    public final static int REQUEST_DICES = 2;
    public final static int REQUEST_INFO_ACTIVITY = 3;
    public final static String DATA_SLOTS = "Slots";
    public final static String DATA_DICES = "Dices";

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

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameViewModel.addContext(this);

        /*ImageButtons in ActionBar*/
        ImageButton deletePlayerButton = findViewById(R.id.playerToolbarDeleteButton);
        ImageButton createPlayerButton = findViewById(R.id.playerToolbarCreatePlayer);
        //set onClick actions
        deletePlayerButton.setOnClickListener(v -> ShowDeletePlayerDialog());
        createPlayerButton.setOnClickListener(v -> ShowCreatePlayerDialog());
        ShowCreatePlayerDialog();

        viewPager = findViewById(R.id.GamePager);
        tabLayout = findViewById(R.id.tab_layout);
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
            gameViewModel.addCommonQuest(cqIndex);
        }
        gamePagerCollectionAdapter.addFragment(playerFragment, username, pqIndex);
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
                showHelpDialog();
                return true;
            case R.id.GameMenuQuitGame:
                Intent intent = new Intent(GameActivity.this, MenuActivity.class);
                this.finish();
                startActivity(intent);
            /*case R.id.GameMenuSettings:
                return true;*/
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

    private void showHelpDialog(){}
}
