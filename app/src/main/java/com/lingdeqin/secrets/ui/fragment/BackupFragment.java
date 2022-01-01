package com.lingdeqin.secrets.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.services.drive.model.About;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.helper.GoogleDriveHelper;
import com.lingdeqin.secrets.task.GoogleDriveUploadTask;
import com.lingdeqin.secrets.ui.dialog.GoogleAccountDialogFragment;

import java.io.IOException;

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
        GoogleDriveInfo = findPreference("GoogleDriveInfo");
        updateUI();
        GoogleSignInAccount googleSignInAccount = GoogleDriveHelper.getInstance().getGoogleSignInAccount(getContext());
        if (googleSignInAccount != null){
            Single.create(new SingleOnSubscribe<About>() {
                @Override
                public void subscribe(@io.reactivex.rxjava3.annotations.NonNull SingleEmitter<About> emitter) throws Throwable {
                    try {
                        About about = GoogleDriveHelper.getInstance().about(getContext());
                        emitter.onSuccess(about);
                    }catch (IOException e){
                        emitter.onError(e);
                    }


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
                            e.printStackTrace();
                            Log.i(TAG, "onError: " + e.getMessage());

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
        inflater.inflate(R.menu.cloud_upload, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_cloud_upload:
                Log.i(TAG, "onOptionsItemSelected: ");
                backup();
                break;
        }
        return super.onOptionsItemSelected(item);
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
                        Snackbar.make(getView(),"欢迎登录"+googleSignInAccount.getEmail(),Snackbar.LENGTH_LONG).show();
                        updateUI();
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
            GoogleDriveInfo.setTitle(getString(R.string.login));
            GoogleDriveInfo.setSummary(null);
        }
    }

    private void backup(){
        GoogleDriveUploadTask googleDriveUploadTask = new GoogleDriveUploadTask(getContext());
        googleDriveUploadTask.start();
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
