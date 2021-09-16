
package com.lingdeqin.secrets.core.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.lingdeqin.secrets.base.MyApplication;
import com.lingdeqin.secrets.core.room.dao.SecretDao;
import com.lingdeqin.secrets.core.room.entity.Secret;


@Database(entities = {Secret.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SecretDao secretDao();

    private static volatile AppDatabase dataBases;

    private static final String DATA_TABLE_NAME="zero-db";

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();


    public static AppDatabase getInstance(){
        if (dataBases==null){
            synchronized (AppDatabase.class){
                if (dataBases==null){
                    dataBases=buildDatabase(MyApplication.getContext());
                    dataBases.updateDatabaseCreated(MyApplication.getContext().getApplicationContext());
                }
            }
        }
        return dataBases;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static AppDatabase buildDatabase(final Context appContext) {
        // AppDatabase appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "zero-db").build();
        return Room.databaseBuilder(appContext, AppDatabase.class, DATA_TABLE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                    }
                })
                .build();
    }

    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATA_TABLE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }
}
