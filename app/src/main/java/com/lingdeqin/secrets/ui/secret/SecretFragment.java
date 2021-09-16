package com.lingdeqin.secrets.ui.secret;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.core.room.AppDatabase;
import com.lingdeqin.secrets.core.room.entity.Secret;
import com.lingdeqin.secrets.utils.UIUtil;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SecretFragment extends Fragment {

    private static final String TAG = "SecretFragment";
    private SecretViewModel mViewModel;

    public static SecretFragment newInstance() {
        return new SecretFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_secret, container, false);
    }

    @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SecretViewModel.class);
        // TODO: Use the ViewModel

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.secret, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_save:
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

                EditText editDomain = getView().findViewById(R.id.edit_domain);
                EditText editAccount = getView().findViewById(R.id.edit_account);
                EditText editPassword = getView().findViewById(R.id.edit_password);
                EditText editUrl = getView().findViewById(R.id.edit_url);
                EditText editRemark = getView().findViewById(R.id.edit_remark);
                //TextUtils.isEmpty()
                if (editDomain.getText().toString().isEmpty()){
                    Snackbar.make(getView(), "Domain不能为空！", Snackbar.LENGTH_LONG).show();
                }else {
                    AppDatabase appDatabase = AppDatabase.getInstance();
                    Secret secret = new Secret();

                    secret.domain = editDomain.getText().toString();
                    secret.account = editAccount.getText().toString();
                    secret.password = editPassword.getText().toString();
                    secret.url = editUrl.getText().toString();
                    secret.remark = editRemark.getText().toString();

                    appDatabase.secretDao().insert(secret);
                    emitter.onSuccess(1);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Integer integer) {
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