package com.example.sagrada.Fragments.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.sagrada.R;

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
        builder.setPositiveButton(R.string.deletePlayerDialogPositiveButton, (dialog, which) -> listener.onDeletePlayerAgreed());
        builder.setNegativeButton(R.string.deletePlayerDialogNegativeButton, (dialog, which) -> listener.onDeletePlayerCanceled());
        return builder.create();
}
}
