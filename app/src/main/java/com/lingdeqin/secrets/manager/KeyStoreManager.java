package com.lingdeqin.secrets.manager;

import android.app.Activity;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyStoreManager {

    private static final String TAG = "KeyStoreManager";
    private static final String MASTER_KEY = "MasterKey";
    private static KeyStoreManager instance;
    private KeyStore keyStore;


    private KeyStoreManager(){
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public synchronized static KeyStoreManager getInstance(){
        if (instance == null){
            instance = new KeyStoreManager();
        }
        return instance;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public Enumeration<String> aliases(){
        try {

            return keyStore.aliases();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean containsAlias(String alias){
        try {
            return keyStore.containsAlias(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean containsMasterKey(){
        return containsAlias(MASTER_KEY);
    }

    public Key getKey(String alias){
        try {
            return keyStore.getKey(alias,null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean saveEncrypt(byte[] key){
        return this.saveEncrypt(key, MASTER_KEY);
    }

    public Boolean saveEncrypt(String key, String alias){
        return this.saveEncrypt(key.getBytes(StandardCharsets.UTF_8), alias);
    }

    public Boolean saveEncrypt(byte[] key, String alias){
        try {
            SecretKey secretKey = new SecretKeySpec(key, KeyProperties.KEY_ALGORITHM_AES);
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
            KeyProtection keyProtection = new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationParameters(200,KeyProperties.AUTH_BIOMETRIC_STRONG)
                    .build();
            keyStore.setEntry(alias,entry,keyProtection);
            return true;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void encrypt(Activity activity, String plain, EncryptCallBack encryptCallBack){
        encrypt(activity,plain,MASTER_KEY,encryptCallBack);
    }

    public void encrypt(Activity activity, String plain, String alias, EncryptCallBack encryptCallBack){
        encrypt(activity,plain.getBytes(StandardCharsets.UTF_8),alias,encryptCallBack);
    }

    public void encrypt(Activity activity, byte[] plain, String alias, EncryptCallBack encryptCallBack){
        try {
            SecretKey secretKey = (SecretKey) keyStore.getKey(alias,null);
            final Cipher ci = Cipher.getInstance("AES/GCM/NoPadding");
            try{
                ci.init(Cipher.ENCRYPT_MODE,secretKey);
                byte[] iv = ci.getIV();
                byte[] cipher = ci.doFinal(plain);
                EncryptModel encryptModel = new EncryptModel();
                encryptModel.setCipher(cipher).setIv(iv);
                encryptCallBack.onSuccess(encryptModel);
            }catch (UserNotAuthenticatedException e){
                AuthManager.getInstance().biometric(activity, new AuthManager.AuthCallBack() {
                    @Override
                    public void onSuccess() {
                        encrypt(activity,plain,alias,encryptCallBack);
                    }
                    @Override
                    public void onFailure(String errMsg) {
                        encryptCallBack.onFailure(errMsg);
                    }
                });
            }
            
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            Log.i(TAG, "encrypt: IllegalBlockSizeException:"+e.getMessage());
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
            encryptCallBack.onFailure(e.getMessage());
        }

    }

    public void decrypt(Activity activity, byte[] iv, byte[] cipher, DecryptCallBack decryptCallBack){
        decrypt(activity,iv,cipher,MASTER_KEY,decryptCallBack);
    }

    public void decrypt(Activity activity, byte[] iv, byte[] cipher, String alias, DecryptCallBack decryptCallBack){
        try {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias,null);
            final Cipher ci = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            try {
                ci.init(Cipher.DECRYPT_MODE, entry.getSecretKey(), spec);
                byte[] plain = ci.doFinal(cipher);
                decryptCallBack.onSuccess(new String(plain, StandardCharsets.UTF_8));
            }catch (UserNotAuthenticatedException e){
                AuthManager.getInstance().biometric(activity, new AuthManager.AuthCallBack() {
                    @Override
                    public void onSuccess() {
                        decrypt(activity,iv,cipher,alias,decryptCallBack);
                    }
                    @Override
                    public void onFailure(String errMsg) {
                        decryptCallBack.onFailure(errMsg);
                    }
                });
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
            decryptCallBack.onFailure(e.getMessage());
        }
    }

    public interface EncryptCallBack {
        void onSuccess(EncryptModel encryptModel);
        void onFailure(String error);
    }

    public interface DecryptCallBack {
        void onSuccess(String plain);
        void onFailure(String error);
    }

    public static class EncryptModel{
        private byte[] iv;
        private byte[] cipher;
        public byte[] getIv() {
            return iv;
        }
        public EncryptModel setIv(byte[] iv) {
            this.iv = iv;
            return this;
        }
        public byte[] getCipher() {
            return cipher;
        }
        public EncryptModel setCipher(byte[] cipher) {
            this.cipher = cipher;
            return this;
        }
    }

}
