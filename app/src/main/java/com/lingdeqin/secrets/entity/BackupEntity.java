package com.lingdeqin.secrets.entity;

import com.lingdeqin.secrets.core.room.entity.Secret;

import java.util.ArrayList;
import java.util.List;


public class BackupEntity {

    List<Secret> secrets;

    public List<Secret> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<Secret> secrets) {
        this.secrets = secrets;
    }

    public void addSecret(Secret secret) {
        if (this.secrets == null){
            this.secrets = new ArrayList<>();
        }
        this.secrets.add(secret);
    }

}
