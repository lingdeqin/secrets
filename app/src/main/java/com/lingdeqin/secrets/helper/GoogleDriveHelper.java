package com.lingdeqin.secrets.helper;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
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

    public void signIn(Intent signInIntent, GoogleSignInListener googleSignInListener){
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

    public Task<Void> signOut(Context context){
        googleSignInAccount = null;
        drive = null;
        return getGoogleSignInClient(context).signOut();
    }

    public String fileCreate(Context context,String fileName, String content) throws IOException {
        File file = new File()
                .setParents(Collections.singletonList("root"))
                .setMimeType("application/json")
                .setName(fileName);
        return create(context,file,content);
    }

    public String create(Context context, File file) throws IOException {
        File driveFile = getDrive(context).files().create(file).execute();
        if (driveFile == null) {
            throw new IOException("上传文件异常.");
        }
        return driveFile.getId();
    }
    public String create(Context context, File file, String content) throws IOException {
        ByteArrayContent contentStream = ByteArrayContent.fromString("application/json", content);
        return create(context,file,contentStream);
    }

    public String create(Context context, File file, byte[] content) throws IOException {
        ByteArrayContent contentStream = new ByteArrayContent("application/json",content);
        return create(context,file,contentStream);
    }

    public String create(Context context, File file, ByteArrayContent contentStream) throws IOException {
        File driveFile = getDrive(context).files().create(file,contentStream).execute();
        if (driveFile == null) {
            throw new IOException("上传文件异常.");
        }
        return driveFile.getId();
    }

    public FileList list(Context context) throws IOException {
        return getDrive(context).files().list().setSpaces("drive").execute();
    }

    public About about(Context context) throws IOException {
        Drive.About.Get get = getDrive(context).about().get();
        get.setFields("user,storageQuota");
        return get.execute();
    }


    public GoogleSignInOptions getGoogleSignInOptions(){
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
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
                            Collections.singleton(DriveScopes.DRIVE));
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


