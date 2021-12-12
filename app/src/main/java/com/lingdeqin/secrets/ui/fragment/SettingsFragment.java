package com.lingdeqin.secrets.ui.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.core.room.AppDatabase;
import com.lingdeqin.secrets.core.room.entity.Secret;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";
    private ActivityResultLauncher exportLauncher;
    private ActivityResultLauncher importLauncher;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        setMenuVisibility(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //更改主密钥
        Preference masterKey = findPreference("masterKey");
        masterKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.nav_change_master_key);
                return false;
            }
        });

        //备份
        Preference backup = findPreference("backup");
        backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.nav_backup_setting);
                return false;
            }
        });


        //导出
        Preference export = findPreference("export");
        export.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startExport();
                return true;
            }
        });

        //导入
        Preference mImport = findPreference("import");
        mImport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startImport();
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private void startExport(){
        try {
            exportLauncher.launch("secrets.json");
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void startImport(){
        try {
            importLauncher.launch("application/json");
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        importLauncher = registerForActivityResult(new ActivityResultForOpenDocument(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Single.create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull SingleEmitter<Integer> emitter) throws Throwable {

                        InputStream inputStream = null;
                        BufferedReader br = null;
                        try {
                            inputStream = getContext().getContentResolver().openInputStream(result);
                            byte[] tem = new byte[1024];
                            br = new BufferedReader(new InputStreamReader(inputStream));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null){
                                sb.append(line);
                            }
                            //System.out.println(sb.toString());
                            Log.i(TAG, "subscribe: "+sb.toString());
                            JSONObject jsonObject = JSON.parseObject(sb.toString());
                            JSONArray jsonArray = jsonObject.getJSONArray("secrets");
                            for (int i=0;i<jsonArray.size();i++) {
                                Secret secret = jsonArray.getObject(i,Secret.class);
                                Secret s = new Secret();
                                s.account = secret.account;
                                s.domain = secret.domain;
                                s.remark = secret.remark;
                                s.url = secret.url;
                                s.password = secret.password;
                                s.iv = secret.iv;
                                AppDatabase.getInstance().secretDao().insert(s);
                            }
                            emitter.onSuccess(1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            emitter.onError(e);
                        } finally {
                            try {
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.newThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

                            @Override
                            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Integer integer) {
                                Snackbar.make(getView(),"导入成功",Snackbar.LENGTH_LONG).show();
                                getParentFragmentManager().popBackStack();
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                Snackbar.make(getView(),"导入失败:"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                            }
                        });
            }
        });


        exportLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Single.create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull SingleEmitter<Integer> emitter) throws Throwable {
                        OutputStream outStream = null;
                        try {
                            outStream = getContext().getContentResolver().openOutputStream(result, "w");
                            List<Secret> secrets = AppDatabase.getInstance().secretDao().getAll();
                            Map<String,Object> data = new HashMap<>();
                            data.put("secrets",secrets);
                            outStream.write(JSON.toJSONBytes(data));
                            emitter.onSuccess(1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            emitter.onError(e);
                        } finally {
                            try {
                                if (outStream != null) {
                                    outStream.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.newThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

                            @Override
                            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Integer integer) {
                                Snackbar.make(getView(),"导出成功",Snackbar.LENGTH_LONG).show();
                                getParentFragmentManager().popBackStack();
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                Snackbar.make(getView(),"导出失败:"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                            }
                        });


            }
        });
    }

    public static class ActivityResultForCreateDocument extends ActivityResultContract<String, Uri> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, String input) {
            return new Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("application/json")
                    .putExtra(Intent.EXTRA_TITLE, input);
        }

        @Override
        public Uri parseResult(int resultCode, @Nullable Intent intent) {
            if (intent == null || resultCode != Activity.RESULT_OK) return null;
            return intent.getData();
        }
    }

    public static class ActivityResultForOpenDocument extends ActivityResultContract<String, Uri> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, String input) {
            return new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("application/json");
        }

        @Override
        public Uri parseResult(int resultCode, @Nullable Intent intent) {
            if (intent == null || resultCode != Activity.RESULT_OK) return null;
            return intent.getData();
        }
    }



}