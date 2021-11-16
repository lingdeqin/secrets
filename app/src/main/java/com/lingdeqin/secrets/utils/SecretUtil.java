package com.lingdeqin.secrets.utils;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecretUtil {

    private static final String TAG = "SecretUtil";
    public static final String MASTER_KEY_ALIAS = "MasterZero";
    private static final String KEY_FILE_NAME = "secret";

    public static Boolean saveEncryptedFile(Context context, byte[] content){
        return saveEncryptedFile(context,MASTER_KEY_ALIAS,content);
    }

    public static Boolean saveEncryptedFile(Context context,String keyAlias, byte[] content){
        MasterKey masterKey = GenMasterKey(context,keyAlias);
        return saveEncryptedFile(context,masterKey,KEY_FILE_NAME,content);

    }

    public static Boolean saveEncryptedFile(Context context, MasterKey masterKey, String fileName, byte[] content){
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (file.exists()){
                if(!file.delete()){
                    Log.i(TAG, "saveEncryptedFile: 删除失败");
                }
            }

            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    context,
                    file,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB)
                    .build();
            OutputStream outputStream = encryptedFile.openFileOutput();
            outputStream.write(content);
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getEncryptedFileContent(Context context){
        return getEncryptedFileContent(context,MASTER_KEY_ALIAS);
    }
    public static String getEncryptedFileContent(Context context,String keyAlias){
        MasterKey masterKey = GenMasterKey(context,keyAlias);
        return getEncryptedFileContent(context,masterKey,KEY_FILE_NAME);

    }

    public static String getEncryptedFileContent(Context context, MasterKey masterKey, String fileName){
        try {
            File file = new File(context.getFilesDir(), fileName);
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    context,
                    file,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB)
                    .build();

            InputStream inputStream = encryptedFile.openFileInput();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int nextByte = inputStream.read();
            while (nextByte != -1) {
                byteArrayOutputStream.write(nextByte);
                nextByte = inputStream.read();
            }

            return byteArrayOutputStream.toString();

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MasterKey GenMasterKey(Context context, String keyAlias){

        final KeyGenParameterSpec spec = new KeyGenParameterSpec
                .Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build();
        return GenMasterKey(context,keyAlias,spec);

    }

    public static MasterKey GenMasterKey(Context context, String keyAlias, KeyGenParameterSpec spec) {
        try {
            return new MasterKey.Builder(context,keyAlias)
                    .setKeyGenParameterSpec(spec)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptAes(byte[] key,byte[] iv,byte[] cipher) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKey secretKey = new SecretKeySpec(key, KeyProperties.KEY_ALGORITHM_AES);
        final Cipher ci = Cipher.getInstance("AES/GCM/NoPadding");
        final GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        ci.init(Cipher.DECRYPT_MODE, secretKey, spec);
        byte[] plain = ci.doFinal(cipher);
        return plain;
    }

    public static byte[] PBKDF2(char[] password, byte[] slat){

        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password,slat,12399,128);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String byteToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String getInt(String s){
        Pattern p = Pattern.compile("[^0-9]");
        Matcher m = p.matcher(s);
        return m.replaceAll("").trim();
    }

}
