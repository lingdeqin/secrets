package com.lingdeqin.secrets.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.core.room.AppDatabase;
import com.lingdeqin.secrets.core.room.entity.Secret;
import com.lingdeqin.secrets.security.AuthManager;
import com.lingdeqin.secrets.security.KeyStoreManager;
import com.lingdeqin.secrets.ui.MainActivity;
import com.lingdeqin.secrets.utils.SecretUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class ChangeMasterKeyFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_change_master_key, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        EditText oldPw = view.findViewById(R.id.edit_old_password);
        EditText newPw = view.findViewById(R.id.edit_new_password);
        EditText renewPw = view.findViewById(R.id.edit_new_password_again);
        Button button = view.findViewById(R.id.btn_confirm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!newPw.getText().toString().equals(renewPw.getText().toString())){
                    Snackbar.make(view,"密码不一样", Snackbar.LENGTH_LONG).show();
                }else{
                    String oldMasterKey = oldPw.getText().toString();
                    String s = SecretUtil.getEncryptedFileContent(getContext());
                    JSONObject json = JSONObject.parseObject(s);

                    byte[] aIv = json.getBytes("aIv");
                    byte[] aCipher = json.getBytes("aCipher");
                    byte[] bIv = json.getBytes("bIv");
                    byte[] bCipher = json.getBytes("bCipher");
                    String salt = json.getString("salt");
                    BigInteger c = json.getBigInteger("c");

                    try {
                        byte[] oldMasterHash = SecretUtil.PBKDF2(oldMasterKey.toCharArray(),salt.getBytes());
                        byte[] aBytes = SecretUtil.decryptAes(oldMasterHash,aIv,aCipher);
                        byte[] bBytes = SecretUtil.decryptAes(oldMasterHash,bIv,bCipher);

                        BigInteger a = new BigInteger(SecretUtil.getInt(new String(aBytes)));
                        BigInteger b = new BigInteger(SecretUtil.getInt(new String(bBytes)));
                        BigInteger aAddB = a.add(b);
                        if (c.compareTo(aAddB) == 0){
                            Boolean isSuccess = KeyStoreManager.getInstance().saveEncrypt(oldMasterHash);
                            if (isSuccess){

                                SecureRandom random = new SecureRandom();
                                byte[] newABytes = new byte[40];
                                byte[] newBBytes = new byte[40];
                                random.nextBytes(newABytes);
                                random.nextBytes(newBBytes);
                                String aStr = SecretUtil.byteToHex(newABytes);
                                String bStr = SecretUtil.byteToHex(newBBytes);
                                BigInteger A = new BigInteger(SecretUtil.getInt(aStr));
                                BigInteger B = new BigInteger(SecretUtil.getInt(bStr));
                                BigInteger C = A.add(B);

                                Map<String,Object> map = new HashMap<>();
                                map.put("salt",salt);
                                map.put("c",C);
                                byte[] newMasterHash = SecretUtil.PBKDF2(newPw.getText().toString().toCharArray(),salt.getBytes());
                                Boolean re = KeyStoreManager.getInstance().saveEncrypt(newMasterHash);
                                if (re) {
                                    KeyStoreManager.getInstance().encrypt(getActivity(), aStr, new KeyStoreManager.EncryptCallBack() {
                                        @Override
                                        public void onSuccess(KeyStoreManager.EncryptModel encryptModel) {
                                            byte[] aCipher = encryptModel.getCipher();
                                            byte[] aIv = encryptModel.getIv();
                                            KeyStoreManager.getInstance().encrypt(getActivity(), bStr, new KeyStoreManager.EncryptCallBack() {
                                                @Override
                                                public void onSuccess(KeyStoreManager.EncryptModel encryptModel) {
                                                    byte[] bCipher = encryptModel.getCipher();
                                                    byte[] bIv = encryptModel.getIv();
                                                    map.put("aCipher", aCipher);
                                                    map.put("aIv", aIv);
                                                    map.put("bCipher", bCipher);
                                                    map.put("bIv", bIv);
                                                    JSONObject jsonObject = new JSONObject(map);
                                                    Boolean success = SecretUtil.saveEncryptedFile(context, jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                                                    if (success) {
                                                        SecretKey oldSecretKey = new SecretKeySpec(oldMasterHash, KeyProperties.KEY_ALGORITHM_AES);
                                                        SecretKey newSecretKey = new SecretKeySpec(newMasterHash, KeyProperties.KEY_ALGORITHM_AES);
                                                        AppDatabase.getInstance().secretDao().getSecretBySingle()
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(Schedulers.newThread())
                                                                .subscribe(new SingleObserver<List<Secret>>() {
                                                                    @Override
                                                                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                                                                    }

                                                                    @Override
                                                                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Secret> secrets) {
                                                                        for (Secret secret:secrets) {
                                                                            try{
                                                                                final Cipher ci = Cipher.getInstance("AES/GCM/NoPadding");
                                                                                final GCMParameterSpec spec = new GCMParameterSpec(128, secret.iv);
                                                                                ci.init(Cipher.DECRYPT_MODE, oldSecretKey, spec);
                                                                                byte[] plain = ci.doFinal(secret.password);

                                                                                ci.init(Cipher.ENCRYPT_MODE,newSecretKey);
                                                                                byte[] iv = ci.getIV();
                                                                                byte[] cipher = ci.doFinal(plain);
                                                                                secret.iv = iv;
                                                                                secret.password = cipher;
                                                                                AppDatabase.getInstance().secretDao().updateSecret(secret);
                                                                            }catch (NoSuchAlgorithmException e) {
                                                                                e.printStackTrace();
                                                                            } catch (InvalidKeyException e) {
                                                                                e.printStackTrace();
                                                                            } catch (InvalidAlgorithmParameterException e) {
                                                                                e.printStackTrace();
                                                                            } catch (NoSuchPaddingException e) {
                                                                                e.printStackTrace();
                                                                            } catch (BadPaddingException e) {
                                                                                e.printStackTrace();
                                                                            } catch (IllegalBlockSizeException e) {
                                                                                e.printStackTrace();
                                                                            }

                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                                                                    }
                                                                });




                                                        Snackbar.make(view,"已修改",Snackbar.LENGTH_LONG).show();
                                                        getParentFragmentManager().popBackStack();
                                                    } else {
                                                        //Log.i(TAG, "onSuccess: 保存文件出错");
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

                                }
                            }
                        }else{
                            Snackbar.make(view, "原主密码不正确！", Snackbar.LENGTH_LONG).show();
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

                }
            }
        });

    }
}
