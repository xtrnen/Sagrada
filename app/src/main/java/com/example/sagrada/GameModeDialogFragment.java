package com.example.sagrada;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

public class GameModeDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.gameModeDialog_title);
        builder.setMessage(R.string.gameModeDialogMsg);
        builder.setNeutralButton(R.string.gameModeDialogNeutral_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.println(Log.INFO, "GameModeDialog", "Canceled");
            }
        });
        builder.setNegativeButton(R.string.gameModeDialogNegative_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.println(Log.INFO, "GameModeDialog", "No - multiplayer option");
            }
        });
        builder.setPositiveButton(R.string.gameModeDialogPositive_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.println(Log.INFO, "GameModeDialog", "Yes - singleplayer option");
            }
        });
        return builder.create();
    }
}
