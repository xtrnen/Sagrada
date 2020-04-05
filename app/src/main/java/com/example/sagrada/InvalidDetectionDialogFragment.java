package com.example.sagrada;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class InvalidDetectionDialogFragment extends DialogFragment {
    public interface IDetectionFailedDialogListener {
        public void onCaptureAgain();
        public void onUserHandle();
        public void onCancel();
    }
    private IDetectionFailedDialogListener detectionFailedDialogListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            detectionFailedDialogListener = (IDetectionFailedDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + "No ICreatePlayerDialogListener implementation");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Capture Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                detectionFailedDialogListener.onCaptureAgain();
            }
        });
        builder.setNegativeButton("Fill myself", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                detectionFailedDialogListener.onUserHandle();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                detectionFailedDialogListener.onCancel();
            }
        });
        return builder.create();
    }


}
