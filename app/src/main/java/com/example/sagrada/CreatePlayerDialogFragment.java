package com.example.sagrada;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import Model.GameBoard.Player;
import ViewModel.GameViewModel;


public class CreatePlayerDialogFragment extends DialogFragment {

    public interface ICreatePlayerDialogListener{
        public void onCreatePlayerSubmit(String username);
        public void onCreatePlayerCanceled();
    }
    //TODO: On empty username string don't close dialog

    ICreatePlayerDialogListener listener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            listener = (ICreatePlayerDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "No ICreatePlayerDialogListener implementation");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.create_player_layout, null));

        builder.setTitle(R.string.createPlayerDialog_title);

        builder.setNegativeButton(R.string.gameModeDialogNeutral_title, (dialog, which) -> {
            listener.onCreatePlayerCanceled();
        });

        builder.setPositiveButton(R.string.gameModeDialogPositive_title, (dialog, which) -> {
            EditText username = (EditText)getDialog().findViewById(R.id.createUsernameID);
            String playerUsername = username.getText().toString();

            GameViewModel gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);

            List<Player> players = gameViewModel.getPlayers().getValue();

            boolean userExist = false;
            if(players != null){
                for (Player player:players) {
                    if(player.name.equalsIgnoreCase(playerUsername)){
                        userExist = true;
                        break;
                    }
                }
            }

            if(!userExist && !usernameIsEmpty(playerUsername)){
                listener.onCreatePlayerSubmit(playerUsername);
                this.dismiss();
            }
        });

        return builder.create();
    }

    boolean usernameIsEmpty(String username){
            return (username.trim().isEmpty() || username.equals(""));
    }
}
