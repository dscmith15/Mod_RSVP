package com.example.dustin.ModRSVP;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;


/**
 * Created by Dustin on 4/18/2016.
 */
public class PnumDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pnum)
                .setItems(R.array.pnum_list, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        MainActivity callingActivity = (MainActivity) getActivity();
                        callingActivity.onPnumSelectValue(which);

                    }
                });

        return builder.create();
    }


}

