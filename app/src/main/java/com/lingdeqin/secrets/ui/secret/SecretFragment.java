package com.lingdeqin.secrets.ui.secret;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.core.room.AppDatabase;
import com.lingdeqin.secrets.core.room.entity.Secret;
import com.lingdeqin.secrets.security.KeyStoreManager;
import com.lingdeqin.secrets.utils.UIUtil;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.subscribers.BlockingBaseSubscriber;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SecretFragment extends Fragment {

    private static final String TAG = "SecretFragment";
    private SecretViewModel mViewModel;
    private Bundle bundle;
    private EditText editDomain;
    private EditText editAccount;
    private EditText editPassword;
    private EditText editUrl;
    private EditText editRemark;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_secret, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editDomain = getView().findViewById(R.id.edit_domain);
        editAccount = getView().findViewById(R.id.edit_account);
        editPassword = getView().findViewById(R.id.edit_password);
        editUrl = getView().findViewById(R.id.edit_url);
        editRemark = getView().findViewById(R.id.edit_remark);

        CheckBox cb = getView().findViewById(R.id.is_visible_password);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    editPassword.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                } else {
                    editPassword.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD
                            | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }
            }
        });

        if (bundle != null) {
            int sid = bundle.getInt("sid");
            Log.i(TAG, "onCreate: sid = " + sid);
            AppDatabase appDatabase = AppDatabase.getInstance();
            Flowable<Secret> secretFlowable =  appDatabase.secretDao().getSecretBySid(sid);
            secretFlowable.subscribe(new BlockingBaseSubscriber<Secret>() {
                @Override
                public void onNext(Secret secret) {
                    Log.i(TAG, "onNext: secret"+secret.account);
                    editDomain.setText(secret.domain);
                    editAccount.setText(secret.account);
                    //secretPassword.setText(secret.password.toString());
                    KeyStoreManager.getInstance().decrypt(getActivity(), secret.iv, secret.password, new KeyStoreManager.DecryptCallBack() {
                        @Override
                        public void onSuccess(String plain) {
                            editPassword.setText(plain);
                        }

                        @Override
                        public void onFailure(String error) {
                            Snackbar.make(getView(),"密码解密失败："+error,Snackbar.LENGTH_LONG).show();
                        }
                    });
                    editUrl.setText(secret.url);
                    editRemark.setText(secret.remark);
                }

                @Override
                public void onError(Throwable t) {
                    Log.i(TAG, "onError: ===="+t.getMessage());
                }
            });
        }

    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        mViewModel = new ViewModelProvider(this).get(SecretViewModel.class);
//        // TODO: Use the ViewModel
//
//    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_save:
                saveSecret();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSecret(){

        Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull SingleEmitter<Integer> emitter) throws Throwable {

                UIUtil.hideKeyboard(getContext(),getView());

                if (editDomain.getText().toString().isEmpty()){
                    Snackbar.make(getView(), "Domain不能为空！", Snackbar.LENGTH_LONG).show();
                }else {

                    KeyStoreManager.getInstance().encrypt(getActivity(), editPassword.getText().toString(), new KeyStoreManager.EncryptCallBack() {
                        @Override
                        public void onSuccess(KeyStoreManager.EncryptModel encryptModel) {

                            AppDatabase appDatabase = AppDatabase.getInstance();
                            Secret secret = new Secret();
                            secret.domain = editDomain.getText().toString();
                            secret.account = editAccount.getText().toString();
                            secret.iv = encryptModel.getIv();
                            secret.password = encryptModel.getCipher();
                            secret.url = editUrl.getText().toString();
                            secret.remark = editRemark.getText().toString();
                            if (bundle != null && bundle.containsKey("sid")){
                                int sid = bundle.getInt("sid");
                                secret.sid = sid;
                                appDatabase.secretDao().updateSecret(secret);
                            }else{
                                appDatabase.secretDao().insert(secret);
                            }
                            emitter.onSuccess(1);

                        }

                        @Override
                        public void onFailure(String error) {
                            Snackbar.make(getView(), "保存失败！err:"+error, Snackbar.LENGTH_LONG).show();
                        }
                    });

                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Integer integer) {
                        Log.i(TAG, "onSuccess: integer:" + integer);
                        Snackbar.make(getView(), "保存成功！", Snackbar.LENGTH_LONG).show();
                        getParentFragmentManager().popBackStack();
                    }
                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Snackbar.make(getView(), "保存失败！" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

}