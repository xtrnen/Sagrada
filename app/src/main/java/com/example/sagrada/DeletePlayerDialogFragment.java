package com.example.sagrada;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DeletePlayerDialogFragment extends DialogFragment {
    public interface IDeletePlayerDialogListener{
        public void onDeletePlayerAgreed();
        public void onDeletePlayerCanceled();
    }

    IDeletePlayerDialogListener listener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            listener = (IDeletePlayerDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "No IDeletePlayerDialogListener implementation");
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.deletePlayerDialogMsg);
        builder.setPositiveButton(R.string.deletePlayerDialogPositiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDeletePlayerAgreed();
            }
        });
        builder.setNegativeButton(R.string.deletePlayerDialogNegativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDeletePlayerCanceled();
            }
        });
        return builder.create();
    }
}
