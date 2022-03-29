package com.lingdeqin.secrets.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.lingdeqin.secrets.ui.MainActivity;
import com.lingdeqin.secrets.utils.SecretUtil;

import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class SignUpManager {
    private static final String TAG = "SignUpManager";
    private static SignUpManager instance;

    public synchronized static SignUpManager getInstance(){
        if (instance == null){
            instance = new SignUpManager();
        }
        return instance;
    }

    public void signUp(Context context, Activity activity, WeakReference<String> pwd, WeakReference<String> slat, CallBack callBack){
        try {

            byte[] masterHash = SecretUtil.PBKDF2(pwd.get().toCharArray(),slat.get().getBytes());
            Log.i(TAG, "onClick: masterHash="+new String(masterHash, StandardCharsets.UTF_8));

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
                                    callBack.onSuccess();
                                }else{
                                    //Log.i(TAG, "onSuccess: 保存文件出错");
                                    callBack.onFailure("保存文件出错");
                                }
                            }
                            @Override
                            public void onFailure(String error) {
                                callBack.onFailure(error);
                            }
                        });
                    }
                    @Override
                    public void onFailure(String error) {
                        callBack.onFailure(error);
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

    public interface CallBack {
        void onSuccess();
        void onFailure(String error);
    }

}
