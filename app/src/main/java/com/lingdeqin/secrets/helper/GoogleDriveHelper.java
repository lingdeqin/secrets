package com.lingdeqin.secrets.helper;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public class GoogleDriveHelper {

    private static GoogleDriveHelper instance;

    private GoogleSignInAccount googleSignInAccount;
    private Drive drive;

    public static synchronized GoogleDriveHelper getInstance(){
        if (instance == null){
            instance = new GoogleDriveHelper();
        }
        return instance;
    }

    public void googleSignIn(Intent signInIntent, GoogleSignInListener googleSignInListener){
        GoogleSignIn.getSignedInAccountFromIntent(signInIntent)
                .addOnSuccessListener(googleAccount -> {
                    googleSignInAccount = googleAccount;
                    googleSignInListener.onSuccessCallBack(googleAccount);
                }).addOnCanceledListener(()->{
                    googleSignInListener.onCanceledCallBack();
                }).addOnFailureListener(e -> {
                    googleSignInListener.onFailureCallBack(e);
                });
    }

    public GoogleSignInOptions getGoogleSignInOptions(){
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
    }

    public GoogleSignInClient getGoogleSignInClient(Context context){
        return GoogleSignIn.getClient(context, getGoogleSignInOptions());
    }

    public Intent getSignInIntent(Context context){
        return getGoogleSignInClient(context).getSignInIntent();
    }

    public Drive getDrive(Context context) {
        if (drive == null){
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(context,
                            Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(getGoogleSignInAccount(context).getAccount());
            drive = new Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("Google Drive API")
                            .build();
        }
        return drive;
    }

    public GoogleSignInAccount getGoogleSignInAccount(Context context) {
        if (googleSignInAccount == null){
            googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
        }
        return googleSignInAccount;
    }

    public interface GoogleSignInListener{
        void onSuccessCallBack(GoogleSignInAccount googleSignInAccount);
        void onCanceledCallBack();
        void onFailureCallBack(Exception e);
    }

}


