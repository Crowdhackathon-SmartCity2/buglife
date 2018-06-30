package com.maliotis.traffic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment {
    String title;
    String message;
    String positiveButton;
    String negativeButton;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Setting default values in case something goes south!!
        title = getArguments().getString("title","Oopps");
        message = getArguments().getString("message","Something went wrong!");
        positiveButton = getArguments().getString("OK","OK");
        negativeButton = getArguments().getString("Cancel","Cancel");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        return builder.create();
    }
}
