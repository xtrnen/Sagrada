package com.example.sagrada;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

import Model.GameBoard.Player;
import ViewModel.GameViewModel;

public class GameActivity extends AppCompatActivity implements CreatePlayerDialogFragment.ICreatePlayerDialogListener {
    GamePagerCollectionAdapter gamePagerCollectionAdapter;
    GameViewModel gameViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);

        /*Toolbar*/
        Toolbar toolbar = (Toolbar)findViewById(R.id.GameMenuToolbar);
        setSupportActionBar(toolbar);

        /*Show Creation Dialog so we create first user*/
        ShowCreatePlayerDialog();

        ViewPager viewPager = (ViewPager) findViewById(R.id.GamePager);
        gamePagerCollectionAdapter = new GamePagerCollectionAdapter(getSupportFragmentManager());
        viewPager.setAdapter(gamePagerCollectionAdapter);

        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
    }

    public void ShowCreatePlayerDialog(){
        DialogFragment gameModeDialog = new CreatePlayerDialogFragment();
        gameModeDialog.show(getSupportFragmentManager(), "CreatePlayerDialog");
    }

    @Override
    public void onCreatePlayerSubmit(String username) {
        Log.println(Log.INFO, "CreatePlayerSubmit", "Name :" + username);
        gameViewModel.addPlayer(new Player(username));
        PlayerFragment playerFragment = new PlayerFragment();
        gamePagerCollectionAdapter.addFragment(playerFragment, username);
    }

    @Override
    public void onCreatePlayerCanceled() {
        Intent intent = new Intent(GameActivity.this, MenuActivity.class);
        this.finish();
        startActivity(intent);
    }

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
                //RemovePlayerPage();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
