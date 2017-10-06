package edu.dartmouth.cs.userprofile;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Context;
import android.app.Activity;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Dialog;
import java.io.*;





/**
 * Created by johnnybrady on 10/5/17.
 *
 * idea to use Dialog fragment adapted from:
 * http://www.cs.dartmouth.edu/~sergey/cs65/examples/Dialog/AuthDialog.java
 */

public class PasswordDialog extends DialogFragment  {

    private String pass;
    private Boolean okClicked;

    EditText text1;


    public String getPass(){
        return pass;
    }

    public Boolean getOkClicked(){
        return okClicked;
    }

    public static String FILE = "FILE";

    /*
    *   interface to be used by the MainActivity
    */
    public interface PasswordDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    PasswordDialogListener pdListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        final View dialogView = inflater.inflate(R.layout.password_dialog, null);

        text1 = dialogView.findViewById(R.id.dialogPassword);

        okClicked = false;

        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("OK", "OK clicked");

                        // collect the user confirmation password
                        pass = text1.getText().toString ();
                        okClicked = true;


                        pdListener.onDialogPositiveClick(PasswordDialog.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        pdListener.onDialogNegativeClick(PasswordDialog.this);
                        okClicked = false;
                    }
                });

        return builder.create();
    }




    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DialogListener so we can send events to the host
            pdListener = (PasswordDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement PasswordDialogListener");
        }
        Log.d("DIALOG", "attached");
    }


}
