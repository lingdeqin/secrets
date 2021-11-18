package com.lingdeqin.secrets.security;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class AuthManager {

    private static final String TAG = "AuthManager";
    private static AuthManager instance;
    private static final Object lock = new Object();

    public static AuthManager getInstance(){
        if (instance == null){
            synchronized (lock){
                if (instance == null){
                    instance = new AuthManager();
                }
            }
        }
        return instance;
    }

    public void biometric(Activity activity, AuthCallBack authCallBack){
        BiometricManager biometricManager = BiometricManager.from(activity.getApplicationContext());
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "App can authenticate using biometrics.");
                auth((FragmentActivity) activity,authCallBack);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                activity.startActivityForResult(enrollIntent,1);

                break;
        }

    }

    private void auth(FragmentActivity activity, AuthCallBack authCallBack){

        Executor executor = ContextCompat.getMainExecutor(activity.getApplicationContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(activity.getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
                authCallBack.onFailure("Authentication error: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(activity.getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                authCallBack.onSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                authCallBack.onFailure("Authentication failed");
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("需要验证")
                .setSubtitle("验证以使用")
                .setNegativeButtonText("使用主密码")
                .build();
        biometricPrompt.authenticate(promptInfo);

    }

    public interface AuthCallBack {

        void onSuccess();

        void onFailure(String errMsg);

    }


}
