package com.lingdeqin.secrets.ui.nosecret;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.base.MyApplication;
import com.lingdeqin.secrets.core.room.AppDatabase;
import com.lingdeqin.secrets.core.room.entity.Secret;
import com.lingdeqin.secrets.security.KeyStoreManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.subscribers.BlockingBaseSubscriber;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class NoSecretFragment extends Fragment {

    private static final String TAG = "NoSecretFragment";
    private EditText secretDomain;
    private EditText secretAccount;
    private EditText secretPassword;
    private EditText secretUrl;
    private EditText secretRemark;
    private Button btnCopy;
    private Bundle bundle;

    public NoSecretFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
    }

    @Override
    public void onStart() {
        super.onStart();

        secretDomain = getView().findViewById(R.id.secret_domain);
        secretAccount = getView().findViewById(R.id.secret_account);
        secretPassword = getView().findViewById(R.id.secret_password);
        secretUrl = getView().findViewById(R.id.secret_url);
        secretRemark = getView().findViewById(R.id.secret_remark);
        btnCopy = getView().findViewById(R.id.btn_copy);

        if (bundle != null) {
            int sid = bundle.getInt("sid");
            Log.i(TAG, "onCreate: sid = " + sid);
            AppDatabase appDatabase = AppDatabase.getInstance();
            Flowable<Secret> secretFlowable =  appDatabase.secretDao().getSecretBySid(sid);
            secretFlowable.subscribe(new BlockingBaseSubscriber<Secret>() {
                @Override
                public void onNext(Secret secret) {
                    Log.i(TAG, "onNext: secret"+secret.account);
                    secretDomain.setText(secret.domain);
                    secretAccount.setText(secret.account);
                    //secretPassword.setText(secret.password.toString());
                    KeyStoreManager.getInstance().decrypt(getActivity(), secret.iv, secret.password, new KeyStoreManager.DecryptCallBack() {
                        @Override
                        public void onSuccess(String plain) {
                            secretPassword.setText(plain);
                        }

                        @Override
                        public void onFailure(String error) {
                            Snackbar.make(getView(),"密码解密失败："+error,Snackbar.LENGTH_LONG).show();
                        }
                    });
                    secretUrl.setText(secret.url);
                    secretRemark.setText(secret.remark);
                }

                @Override
                public void onError(Throwable t) {
                    Log.i(TAG, "onError: ===="+t.getMessage());
                }
            });
        }
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) MyApplication.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.clearPrimaryClip();

                ClipData mClipData = ClipData.newPlainText("secret", secretPassword.getText());
                cm.setPrimaryClip(mClipData);

                Observable.timer(10, TimeUnit.SECONDS)
                        .subscribe(new Observer<Long>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }
                            @Override
                            public void onNext(@NonNull Long aLong) {
                                ClipboardManager cm = (ClipboardManager) MyApplication.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.clearPrimaryClip();
                            }
                            @Override
                            public void onError(@NonNull Throwable e) {
                            }
                            @Override
                            public void onComplete() {
                            }
                        });

                Snackbar.make(getView(),"已复制到剪切板",Snackbar.LENGTH_LONG).show();
            }
        });

        CheckBox cb = getView().findViewById(R.id.is_visible_password);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    secretPassword.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                } else {
                    secretPassword.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD
                            | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }
            }
        });

    }

    private void deleteSecret(){
        int sid = bundle.getInt("sid");
        

        Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Integer> emitter) throws Throwable {
                Secret secret = new Secret();
                secret.sid = sid;
                AppDatabase appDatabase = AppDatabase.getInstance();
                int r = appDatabase.secretDao().deleteSecret(secret);
                Log.i(TAG, "subscribe: r"+r);
                emitter.onSuccess(1);

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new SingleObserver<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}

            @Override
            public void onSuccess(@NonNull Integer integer) {
                Snackbar.make(getView(),"已删除",Snackbar.LENGTH_LONG).show();
                getParentFragmentManager().popBackStack();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Snackbar.make(getView(),"删除失败"+e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_no_secret, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@androidx.annotation.NonNull Menu menu, @androidx.annotation.NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.delete, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_delete:
                deleteSecret();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        ClipboardManager cm = (ClipboardManager) MyApplication.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.clearPrimaryClip();
        super.onDestroy();
    }
}