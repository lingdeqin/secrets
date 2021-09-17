package com.lingdeqin.secrets.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.lingdeqin.secrets.core.room.AppDatabase;
import com.lingdeqin.secrets.core.room.entity.Secret;

import java.util.List;

public class SecretsViewModel extends AndroidViewModel {

    private LiveData<List<Secret>> secretLiveData;

    public SecretsViewModel(@NonNull Application application) {
        super(application);

        AppDatabase appDatabase = AppDatabase.getInstance();
        secretLiveData = appDatabase.secretDao().getSecretByLiveData();

    }

    public LiveData<List<Secret>> getSecrets(){
        return secretLiveData;
    }

}
