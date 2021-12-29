package com.lingdeqin.secrets.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.alibaba.fastjson.JSON;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.core.room.AppDatabase;
import com.lingdeqin.secrets.core.room.entity.Secret;
import com.lingdeqin.secrets.helper.GoogleDriveHelper;
import com.lingdeqin.secrets.ui.dialog.GoogleAccountDialogFragment;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BackupFragment extends PreferenceFragmentCompat{

    private static final String TAG = "BackupFragment";
    private ActivityResultLauncher<String> googleSignLauncher;
    Preference GoogleDriveInfo;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.backup_preferences, rootKey);

        // GoogleDrive
        Preference GoogleDrive = findPreference("GoogleDrive");
        GoogleDriveInfo = findPreference("GoogleDriveInfo");
        GoogleSignInAccount googleSignInAccount = GoogleDriveHelper.getInstance().getGoogleSignInAccount(getContext());
        if (googleSignInAccount != null){
            Single.create(new SingleOnSubscribe<About>() {
                @Override
                public void subscribe(@io.reactivex.rxjava3.annotations.NonNull SingleEmitter<About> emitter) throws Throwable {
                    About about = GoogleDriveHelper.getInstance().about(getContext());
                    emitter.onSuccess(about);
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<About>() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        }
                        @Override
                        public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull About about) {
                            GoogleDriveInfo.setTitle(about.getUser().getEmailAddress());
                            long limit = about.getStorageQuota().getLimit()/(1024*1024*1024);
                            long usage = about.getStorageQuota().getUsage()/(1024*1024);
                            GoogleDriveInfo.setSummary(usage+" MB / "+limit+" GB");
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        }
                    });
        }
        GoogleDriveInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.i("TAG", "onPreferenceClick: GoogleDrive=======");
                GoogleSignInAccount googleSignInAccount = GoogleDriveHelper.getInstance().getGoogleSignInAccount(getContext());
                if (googleSignInAccount != null){
                    Bundle bundle = new Bundle();
                    bundle.putString("title",googleSignInAccount.getEmail());
                    GoogleAccountDialogFragment dialog = new GoogleAccountDialogFragment();
                    dialog.setListener(new GoogleAccountDialogFragment.GoogleAccountDialogListener() {
                        @Override
                        public void onClick(AlertDialog dialog, int which) {
                            Log.i(TAG, "onListClick: which=="+which);
                            GoogleDriveHelper.getInstance().signOut(getContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    updateUI();
                                    if (which == 0){
                                        googleSignLauncher.launch("");
                                    }
                                }
                            });
                        }
                    });
                    dialog.show(getParentFragmentManager(),"GoogleAccountDialog");
                }else{
                    googleSignLauncher.launch("");
                }
                return false;
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleSignLauncher = registerForActivityResult(new ActivityResultForGoogleSign(), new ActivityResultCallback<Intent>() {
            @Override
            public void onActivityResult(Intent result) {

                GoogleDriveHelper.getInstance().signIn(result, new GoogleDriveHelper.GoogleSignInListener() {
                    @Override
                    public void onSuccessCallBack(GoogleSignInAccount googleSignInAccount) {
                        Snackbar.make(getView(),"欢迎登录"+googleSignInAccount.getEmail(),Snackbar.LENGTH_LONG);
                        Log.i(TAG, "onSuccessCallBack: 欢迎登录"+googleSignInAccount.getEmail());
                        Single.create(new SingleOnSubscribe<Integer>() {
                            @Override
                            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull SingleEmitter<Integer> emitter) throws Throwable {
                                try {

                                    FileList fileList = GoogleDriveHelper.getInstance().list(getContext());
                                    String folderId = null;
                                    for (File driveFile:fileList.getFiles()) {
                                        if (driveFile.getMimeType().equals(DriveFolder.MIME_TYPE)
                                                && driveFile.getName().equals("SecretsBackup")){
                                            folderId = driveFile.getId();
                                        }
                                    }
                                    if (folderId == null){
                                        File folder = new File()
                                                .setParents(Collections.singletonList("root"))
                                                .setMimeType(DriveFolder.MIME_TYPE)
                                                .setName("SecretsBackup");
                                        folderId = GoogleDriveHelper.getInstance().create(getContext(),folder);
                                    }
                                    Log.i(TAG, "onSuccessCallBack: folderId="+folderId);
                                    File file = new File()
                                            .setParents(Collections.singletonList(folderId))
                                            .setMimeType("application/json")
                                            .setName("secret.json");
                                    List<Secret> secrets = AppDatabase.getInstance().secretDao().getAll();
                                    Map<String,Object> data = new HashMap<>();
                                    data.put("secrets",secrets);

                                    String fileId = GoogleDriveHelper.getInstance().create(getContext(),file,JSON.toJSONBytes(data));
                                    Log.i(TAG, "onSuccessCallBack: fileId="+fileId);

                                    emitter.onSuccess(1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    emitter.onError(e);
                                }
                            }
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Integer>() {
                                    @Override
                                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                                    }
                                    @Override
                                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Integer integer) {
                                        Log.i(TAG, "onSuccess: 创建文件成功");

                                    }
                                    @Override
                                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                                    }
                                });


                    }
                    @Override
                    public void onCanceledCallBack() {
                        Log.i(TAG, "onCanceledCallBack: ");
                    }
                    @Override
                    public void onFailureCallBack(Exception e) {
                        Log.e(TAG, "onFailureCallBack: ", e);
                    }
                });
            }
        });


    }

    private void updateUI(){
        GoogleSignInAccount googleSignInAccount = GoogleDriveHelper.getInstance().getGoogleSignInAccount(getContext());
        if (googleSignInAccount != null) {
            GoogleDriveInfo.setTitle(googleSignInAccount.getEmail());
        }else{
            GoogleDriveInfo.setTitle(R.string.login);
        }
    }

    public static class ActivityResultForGoogleSign extends ActivityResultContract<String, Intent> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, String input) {
            return GoogleDriveHelper.getInstance().getSignInIntent(context);
        }

        @Override
        public Intent parseResult(int resultCode, @Nullable Intent intent) {
            return intent;
        }

    }

}
