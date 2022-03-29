package com.lingdeqin.secrets.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.manager.AuthManager;
import com.lingdeqin.secrets.manager.KeyStoreManager;
import com.lingdeqin.secrets.utils.SecretUtil;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    private EditText editAuth;
    private Button btnUnlock;
    private Button btnFingerprint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        editAuth = findViewById(R.id.edit_auth);
        btnUnlock = findViewById(R.id.btn_unlock);
        btnFingerprint = findViewById(R.id.btn_fingerprint);
        CheckBox cb = findViewById(R.id.is_visible_password);

        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance();
        if (keyStoreManager.containsMasterKey()){
            biometric();
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

        btnFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometric();
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

    private void biometric(){
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

}