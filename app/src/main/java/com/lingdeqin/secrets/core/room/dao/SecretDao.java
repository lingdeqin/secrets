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
    
    @Query("SELECT * FROM secret")
    Flowable<Secret> getSecretByRxJava();

    @Query("SELECT * FROM secret")
    LiveData<Secret> getSecretByLiveData();

    @Insert
    void insert(Secret secret);

    @Update
    void updateUsers(Secret... secrets);

    @Delete
    void deleteUsers(Secret... secrets);

}