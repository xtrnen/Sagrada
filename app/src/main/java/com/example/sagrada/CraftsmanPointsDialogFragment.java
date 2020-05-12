package com.example.sagrada;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CraftsmanPointsDialogFragment extends DialogFragment {
    public interface ICraftsmanCards {
        void getCraftValues(int sandpaper, int eglomise, boolean sandpaperCheck, boolean eglomiseCheck);
    }

    ICraftsmanCards listener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            listener = (ICraftsmanCards) context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "No ICraftsmanCards implementation");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.craftsman_point_layout, null);

        ImageButton plusSandpaper = view.findViewById(R.id.addBtnSandpaper);
        ImageButton plusEglomise = view.findViewById(R.id.addBtnEglomise);
        ImageButton subSandpaper = view.findViewById(R.id.subBtnSandpaper);
        ImageButton subEglomise = view.findViewById(R.id.subBtnEglomise);
        TextView sandpaperText = view.findViewById(R.id.numberSandPaper);
        TextView eglomiseText = view.findViewById(R.id.numberEglomise);
        CheckBox sandpaper = view.findViewById(R.id.sandpaperID);
        CheckBox eglomise = view.findViewById(R.id.eglomiseID);

        plusSandpaper.setOnClickListener(v -> {
            int num = Integer.parseInt(sandpaperText.getText().toString());
            sandpaperText.setText(Integer.toString(++num));
        });
        plusEglomise.setOnClickListener(v -> {
            int num = Integer.parseInt(eglomiseText.getText().toString());
            eglomiseText.setText(Integer.toString(++num));
        });
        subSandpaper.setOnClickListener(v -> {
            int num = Integer.parseInt(sandpaperText.getText().toString());
            if(num != 0){
                sandpaperText.setText(Integer.toString(--num));
            }
        });
        subEglomise.setOnClickListener(v -> {
            int num = Integer.parseInt(eglomiseText.getText().toString());
            if(num != 0){
                eglomiseText.setText(Integer.toString(--num));
            }
        });

        builder.setView(view);
        builder.setTitle(R.string.craftsmanCardTitle);
        builder.setMessage(R.string.craftsmanCardMsg);

        builder.setNeutralButton(R.string.cancelString, (dialog, which) -> {});
        builder.setPositiveButton(R.string.confirmString, (dialog, which) -> listener.getCraftValues(
                Integer.parseInt(sandpaperText.getText().toString()),
                Integer.parseInt(eglomiseText.getText().toString()),
                sandpaper.isChecked(),
                eglomise.isChecked()));
        return builder.create();
    }
}
