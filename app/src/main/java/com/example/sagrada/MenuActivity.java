package com.example.sagrada;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

public class MenuActivity extends AppCompatActivity {
    private Button startGameButton;
    private Button settingsButton;
    private Button quitAppButton;

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
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.INFO, "StartGameListener", "Starting game");
                DialogFragment gameModeDialog = new GameModeDialogFragment();
                gameModeDialog.show(getSupportFragmentManager(), "GameModeDialog");
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.INFO, "StartSettingsListener", "Settings");
            }
        });

        quitAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.INFO, "StartQuitAppListener", "Quit App");
                finish();
                System.exit(0);
            }
        });
    }
}
