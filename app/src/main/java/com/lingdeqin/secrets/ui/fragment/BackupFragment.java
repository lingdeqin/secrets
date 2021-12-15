package com.lingdeqin.secrets.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.lingdeqin.secrets.R;

import java.util.Collections;

public class BackupFragment extends PreferenceFragmentCompat {

    private static final String TAG = "BackupFragment";
    private ActivityResultLauncher googleSignLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.backup_preferences, rootKey);

        //GoogleDrive
        Preference GoogleDrive = findPreference("GoogleDrive");
        GoogleDrive.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.i("TAG", "onPreferenceClick: GoogleDrive=======");
                googleSignLauncher.launch("");
                return false;
            }
        });

        Preference GoogleDriveSignOut = findPreference("GoogleDriveSignOut");
        GoogleDriveSignOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

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

                GoogleSignIn.getSignedInAccountFromIntent(result)
                        .addOnSuccessListener(googleAccount -> {
                            Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                            // Use the authenticated account to sign in to the Drive service.
                            GoogleAccountCredential credential =
                                    GoogleAccountCredential.usingOAuth2(
                                            getContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
                            credential.setSelectedAccount(googleAccount.getAccount());
                            Drive googleDriveService =
                                    new Drive.Builder(
                                            AndroidHttp.newCompatibleTransport(),
                                            new GsonFactory(),
                                            credential)
                                            .setApplicationName("Drive API Migration")
                                            .build();

                            mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                            mDriveServiceHelper.createFile().addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    Log.i(TAG, "onSuccess: "+ s);
                                }
                            });

                        })
                        .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));

            }
        });


    }

    public static class ActivityResultForGoogleSign extends ActivityResultContract<String, Intent> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, String input) {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                            .build();
            GoogleSignInClient client = GoogleSignIn.getClient(context, signInOptions);

            return client.getSignInIntent();
        }

        @Override
        public Intent parseResult(int resultCode, @Nullable Intent intent) {
            return intent;
        }

    }

}
