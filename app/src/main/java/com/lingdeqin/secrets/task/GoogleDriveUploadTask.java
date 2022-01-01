package com.lingdeqin.secrets.task;


import android.content.Context;
import android.util.Log;

import com.lingdeqin.secrets.helper.BackupHelper;

import java.io.IOException;

public class GoogleDriveUploadTask extends ZeroTask{

    private static final String TAG = "GoogleDriveUploadTask";

    private final Context mContext;

    public GoogleDriveUploadTask(Context context){
        this.mContext = context;
    }

    @Override
    protected void onRun() {
        try{
            String fileId = BackupHelper.backupByGoogleDrive(mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

