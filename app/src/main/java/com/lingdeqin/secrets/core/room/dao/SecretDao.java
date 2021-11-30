package com.lingdeqin.secrets.core.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lingdeqin.secrets.core.room.entity.Secret;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;


@Dao
public interface SecretDao {

    @Query("SELECT * FROM secret")
    List<Secret> getAll();

//    @Query("SELECT * FROM secret where sid = :sid")
//    Secret getSecretBySid(int sid);

    @Query("SELECT * FROM secret where sid = :sid")
    Flowable<Secret> getSecretBySid(int sid);

    @Query("SELECT * FROM secret")
    Flowable<Secret> getSecretByRxJava();

    @Query("SELECT * FROM secret")
    LiveData<List<Secret>> getSecretByLiveData();

    @Insert
    void insert(Secret secret);

    @Update
    int updateSecret(Secret secret);

    @Delete
    int deleteSecret(Secret secret);

    @Update
    int updateSecrets(Secret... secrets);

    @Delete
    int deleteSecrets(Secret... secrets);


}
