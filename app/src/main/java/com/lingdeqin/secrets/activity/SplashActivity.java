package com.lingdeqin.secrets.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.lingdeqin.secrets.MainActivity;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.security.KeyStoreManager;
import com.lingdeqin.secrets.utils.SecretUtil;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance();

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (keyStoreManager.containsAlias(SecretUtil.MASTER_KEY_ALIAS)){
            intent.setClass(getApplicationContext(),AuthActivity.class);
        }else{
            intent.setClass(getApplicationContext(),SignUpActivity.class);
        }
        startActivity(intent);

    }

}