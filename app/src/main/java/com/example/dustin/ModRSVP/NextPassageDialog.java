package com.example.dustin.ModRSVP;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by Dustin on 4/19/2016.
 */
public class NextPassageDialog extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.contin)
                .setPositiveButton(R.string.notyet, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity callingActivity = (MainActivity) getActivity();
                        callingActivity.onnextSelectValue(id);
                    }
                })
                .setNegativeButton(R.string.nexpas, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity callingActivity = (MainActivity) getActivity();
                        callingActivity.onnextSelectValue(id);
                    }

                });


        return builder.create();
    }
}
