package com.lingdeqin.secrets.ui.authenticator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthenticatorViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AuthenticatorViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is authenticator fragment");
    }

    public LiveData<String> getText() {
            return mText;
    }
}