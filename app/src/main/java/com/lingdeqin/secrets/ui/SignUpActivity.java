package com.lingdeqin.secrets.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.manager.KeyStoreManager;
import com.lingdeqin.secrets.manager.SignUpManager;
import com.lingdeqin.secrets.utils.SecretUtil;


import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "FirstActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button btn = findViewById(R.id.btn_confirm);

        Activity activity = this;

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                WeakReference<EditText> pwdEdit = new WeakReference<EditText>(findViewById(R.id.edit_main_password));
                WeakReference<EditText> repwdEdit = new WeakReference<EditText>(findViewById(R.id.edit_main_re_password));
                WeakReference<EditText> slatEdit = new WeakReference<EditText>(findViewById(R.id.edit_salt));
                WeakReference<EditText> reslatEdit = new WeakReference<EditText>(findViewById(R.id.edit_re_salt));

                WeakReference<String> pwd = new WeakReference<String>(pwdEdit.get().getText().toString());
                WeakReference<String> repwd = new WeakReference<String>(repwdEdit.get().getText().toString());
                WeakReference<String> slat = new WeakReference<String>(slatEdit.get().getText().toString());
                WeakReference<String> reslat = new WeakReference<String>(reslatEdit.get().getText().toString());

                if (!pwd.get().equals(repwd.get())){
                    Snackbar.make(view, "两次输入密码不一致！", Snackbar.LENGTH_LONG).show();
                }if (!slat.get().equals(reslat.get())){
                    Snackbar.make(view, "两次输入盐不一致！", Snackbar.LENGTH_LONG).show();
                }else{
                    SignUpManager.getInstance().signUp(getApplicationContext(), activity, pwd, slat, new SignUpManager.CallBack() {
                        @Override
                        public void onSuccess() {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        @Override
                        public void onFailure(String error) {
                            Snackbar.make(view, "err:"+error, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                System.gc();
            }
        });
    }
}