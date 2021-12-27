package com.lingdeqin.secrets.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.helper.GoogleDriveHelper;

public class GoogleAccountDialogFragment extends DialogFragment {

    private final String TAG = "GoogleAccountDialogFragment";

    private GoogleAccountDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        GoogleSignInAccount googleSignInAccount = GoogleDriveHelper.getInstance().getGoogleSignInAccount(getContext());
        builder.setTitle(googleSignInAccount.getEmail())
                .setItems(R.array.google_account, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener == null){
                            return;
                        }
                        listener.onClick((AlertDialog) dialog,which);
                    }
                });
        return builder.create();
    }

    public void setListener(GoogleAccountDialogListener listener) {
        this.listener = listener;
    }

    public interface GoogleAccountDialogListener {
        void onClick(AlertDialog dialog, int which);
    }

}
