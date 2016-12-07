package com.example.dustin.ModRSVP;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by Dustin on 4/19/2016.
 */
public class ModeDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.mode)

                .setPositiveButton(R.string.casual_mode, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                            MainActivity callingActivity = (MainActivity) getActivity();
                            callingActivity.onModeSelectValue(id);
                    }
                })
                .setNegativeButton(R.string.experiment_mode, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                            MainActivity callingActivity = (MainActivity) getActivity();
                            callingActivity.onModeSelectValue(id);
                    }

                });


        return builder.create();
    }
}
