package com.example.sagrada;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    private Button startGameButton;
    private Button settingsButton;
    private Button quitAppButton;
    public final static int GAME_ROWS = 4;
    public final static int GAME_COLS = 5;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);

        //Bind layout buttons with class props & check binding
        startGameButton = (Button) findViewById(R.id.StartGameButton);
        assert startGameButton != null;
        settingsButton = (Button) findViewById(R.id.SettingsButton);
        assert settingsButton != null;
        quitAppButton = (Button) findViewById(R.id.QuitAppButton);
        assert quitAppButton != null;

        //Set listeners
        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GameActivity.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            //TODO: Settings
        });

        quitAppButton.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });
    }
}
