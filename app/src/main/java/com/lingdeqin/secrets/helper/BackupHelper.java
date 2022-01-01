package com.lingdeqin.secrets.helper;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.android.gms.drive.DriveFolder;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.core.room.AppDatabase;
import com.lingdeqin.secrets.core.room.entity.Secret;
import com.lingdeqin.secrets.entity.BackupEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class BackupHelper {

    private static final String BACKUP_FOLDER = "SecretsBackup";
    private static final String MIME_TYPE_JSON = "application/json";
    private static final String FILE_PREFIX_JSON = "secrets";
    private static final String FILE_SUFFIX_JSON = ".json";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String backupByGoogleDrive(Context context) throws IOException {

        FileList fileList = GoogleDriveHelper.getInstance().list(context);
        String folderId = null;
        for (File driveFile:fileList.getFiles()) {
            if (driveFile.getMimeType().equals(DriveFolder.MIME_TYPE)
                    && driveFile.getName().equals(BACKUP_FOLDER)){
                folderId = driveFile.getId();
            }
        }
        if (folderId == null){
            File folder = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .setName(BACKUP_FOLDER);
            folderId = GoogleDriveHelper.getInstance().create(context,folder);
        }
        File file = new File()
                .setParents(Collections.singletonList(folderId))
                .setMimeType(MIME_TYPE_JSON)
                .setName(getBackupFileName());
        List<Secret> secrets = AppDatabase.getInstance().secretDao().getAll();

        BackupEntity backupEntity = new BackupEntity();
        backupEntity.setSecrets(secrets);

        return GoogleDriveHelper.getInstance().create(context,file, JSON.toJSONBytes(backupEntity));
    }

    public static String getBackupFileName() {
        return FILE_PREFIX_JSON + dtf.format(LocalDateTime.now()) + FILE_SUFFIX_JSON;
    }
}
