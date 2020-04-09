package com.example.sagrada;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CraftsmanPointsDialogFragment extends DialogFragment {
    public  interface ICraftsmanPointsListener{
        void AddPoints(int points);
    }

    ICraftsmanPointsListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ICraftsmanPointsListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "No ICraftsmanPointsListener implementation");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.craftsman_point_layout, null);

        builder.setView(view);
        builder.setTitle("Konečný počet bodů řemeslníků");

        EditText editText = view.findViewById(R.id.craftsmanEditTextID);

        builder.setNeutralButton("Zrušit", (dialog, which) -> {});
        builder.setPositiveButton("Potvrdit", (dialog, which) -> {
            listener.AddPoints(Integer.parseInt(editText.getText().toString()));
        });
        return builder.create();
    }
}
