package com.lingdeqin.secrets.helper;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class GoogleDriveHelper {

    private static GoogleDriveHelper instance;
    private GoogleSignInAccount googleSignInAccount;

    public static synchronized GoogleDriveHelper getInstance(){
        if (instance == null){
            instance = new GoogleDriveHelper();
        }
        return instance;
    }

    public void googleSignIn(){





    }






}
