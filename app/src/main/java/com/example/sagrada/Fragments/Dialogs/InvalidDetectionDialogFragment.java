package com.example.sagrada.Fragments.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sagrada.R;

public class InvalidDetectionDialogFragment extends DialogFragment {
    public interface IDetectionFailedDialogListener {
        void onCaptureAgain();
        void onUserHandle();
        void onCancel();
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
        builder.setTitle(R.string.invalidDetectDialogTitle);
        builder.setMessage(R.string.invalidDetectDialogText);
        builder.setNeutralButton(R.string.cancelString, (dialog, which) -> detectionFailedDialogListener.onCancel());
        builder.setNegativeButton(R.string.invalidDetectDialogMyself, (dialog, which) -> detectionFailedDialogListener.onUserHandle());
        builder.setPositiveButton(R.string.invalidDetectDialogAgain, (dialog, which) -> detectionFailedDialogListener.onCaptureAgain());
        return builder.create();
    }
}
