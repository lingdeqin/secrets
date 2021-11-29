package com.lingdeqin.secrets.activity;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.security.keystore.KeyProperties;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;
import com.lingdeqin.secrets.MainActivity;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.security.AuthManager;
import com.lingdeqin.secrets.security.KeyStoreManager;
import com.lingdeqin.secrets.utils.SecretUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    private EditText editAuth;
    private Button btnUnlock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        editAuth = findViewById(R.id.edit_auth);
        btnUnlock = findViewById(R.id.btn_unlock);
        CheckBox cb = findViewById(R.id.is_visible_password);

        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance();
        if (keyStoreManager.containsMasterKey()){
            AuthManager.getInstance().biometric(this, new AuthManager.AuthCallBack() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                @Override
                public void onFailure(String errMsg) {

                }
            });
        }
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    editAuth.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                } else {
                    editAuth.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD
                            | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }
            }
        });

        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String masterKey = editAuth.getText().toString();

                String s = SecretUtil.getEncryptedFileContent(getApplicationContext());
                JSONObject json = JSONObject.parseObject(s);

                byte[] aIv = json.getBytes("aIv");
                byte[] aCipher = json.getBytes("aCipher");
                byte[] bIv = json.getBytes("bIv");
                byte[] bCipher = json.getBytes("bCipher");
                String salt = json.getString("salt");
                BigInteger c = json.getBigInteger("c");

                try {
                    byte[] masterHash = SecretUtil.PBKDF2(masterKey.toCharArray(),salt.getBytes());
                    byte[] aBytes = SecretUtil.decryptAes(masterHash,aIv,aCipher);
                    byte[] bBytes = SecretUtil.decryptAes(masterHash,bIv,bCipher);

                    BigInteger a = new BigInteger(SecretUtil.getInt(new String(aBytes)));
                    BigInteger b = new BigInteger(SecretUtil.getInt(new String(bBytes)));
                    BigInteger aAddB = a.add(b);
                    if (c.compareTo(aAddB) == 0){
                        Boolean isSuccess = KeyStoreManager.getInstance().saveEncrypt(masterHash);
                        if (isSuccess){
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }else{
                        Log.i(TAG, "onClick: err");
                    }

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (AEADBadTagException e){
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
                Snackbar.make(view, "主密码不正确！", Snackbar.LENGTH_LONG).show();

            }
        });

    }

}