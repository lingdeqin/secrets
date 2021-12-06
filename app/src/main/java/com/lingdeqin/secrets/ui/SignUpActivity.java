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
import com.lingdeqin.secrets.security.KeyStoreManager;
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
//
                WeakReference<String> pwd = new WeakReference<String>(pwdEdit.get().getText().toString());
                WeakReference<String> repwd = new WeakReference<String>(repwdEdit.get().getText().toString());
                WeakReference<String> slat = new WeakReference<String>(slatEdit.get().getText().toString());
                WeakReference<String> reslat = new WeakReference<String>(reslatEdit.get().getText().toString());

                if (!pwd.get().equals(repwd.get())){
                    Snackbar.make(view, "两次输入密码不一致！", Snackbar.LENGTH_LONG).show();
                }if (!slat.get().equals(reslat.get())){
                    Snackbar.make(view, "两次输入盐不一致！", Snackbar.LENGTH_LONG).show();
                }else{
                    try {

                        byte[] masterHash = SecretUtil.PBKDF2(pwd.get().toCharArray(),slat.get().getBytes());
                        Log.i(TAG, "onClick: masterHash="+new String(masterHash, StandardCharsets.UTF_8));
                        Context context = getApplicationContext();

                        SecureRandom random = new SecureRandom();
                        byte[] aBytes = new byte[40];
                        byte[] bBytes = new byte[40];
                        random.nextBytes(aBytes);
                        random.nextBytes(bBytes);
                        String aStr = SecretUtil.byteToHex(aBytes);
                        String bStr = SecretUtil.byteToHex(bBytes);
                        BigInteger a = new BigInteger(SecretUtil.getInt(aStr));
                        BigInteger b = new BigInteger(SecretUtil.getInt(bStr));
                        BigInteger c = a.add(b);

                        Map<String,Object> map = new HashMap<>();
                        map.put("salt",slat.get());
                        map.put("c",c);

                        Boolean isSuccess = KeyStoreManager.getInstance().saveEncrypt(masterHash);
                        if (isSuccess){
                            KeyStoreManager.getInstance().encrypt(activity, aStr, new KeyStoreManager.EncryptCallBack() {
                                @Override
                                public void onSuccess(KeyStoreManager.EncryptModel encryptModel) {
                                    byte[] aCipher = encryptModel.getCipher();
                                    byte[] aIv = encryptModel.getIv();
                                    KeyStoreManager.getInstance().encrypt(activity, bStr, new KeyStoreManager.EncryptCallBack() {
                                        @Override
                                        public void onSuccess(KeyStoreManager.EncryptModel encryptModel) {
                                            byte[] bCipher = encryptModel.getCipher();
                                            byte[] bIv = encryptModel.getIv();
                                            map.put("aCipher",aCipher);
                                            map.put("aIv",aIv);
                                            map.put("bCipher",bCipher);
                                            map.put("bIv",bIv);
                                            JSONObject jsonObject = new JSONObject(map);
                                            Boolean success = SecretUtil.saveEncryptedFile(context,jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                                            if (success){
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }else{
                                                Log.i(TAG, "onSuccess: 保存文件出错");
                                            }
                                        }
                                        @Override
                                        public void onFailure(String error) {
                                        }
                                    });
                                }
                                @Override
                                public void onFailure(String error) {
                                }
                            });
                        }else{
                            Log.i(TAG, "onClick: err");
                        }
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                System.gc();
            }
        });

    }




}