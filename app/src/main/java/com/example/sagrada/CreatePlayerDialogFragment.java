package com.example.sagrada;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;


public class CreatePlayerDialogFragment extends DialogFragment {

    public interface ICreatePlayerDialogListener{
        public void onCreatePlayerSubmit(String username);
        public void onCreatePlayerCanceled();
    }

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

        builder.setNegativeButton(R.string.gameModeDialogNeutral_title, (dialog, which) ->
                listener.onCreatePlayerCanceled());

        builder.setPositiveButton(R.string.gameModeDialogPositive_title, (dialog, which) -> {
            EditText username = (EditText)getDialog().findViewById(R.id.createUsernameID);
            if(!username.getText().toString().trim().isEmpty() && !username.getText().toString().equals("")){
                listener.onCreatePlayerSubmit(username.getText().toString());
                this.dismiss();
            }
        });

        return builder.create();
    }
}
