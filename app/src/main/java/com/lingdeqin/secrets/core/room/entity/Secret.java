package com.lingdeqin.secrets.core.room.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Secret {
    @PrimaryKey(autoGenerate = true)
    public int sid;
    @ColumnInfo(name = "domain")
    public String domain;
    @ColumnInfo(name = "account")
    public String account;
    @ColumnInfo(name = "password")
    public byte[] password;
    @ColumnInfo(name = "iv")
    public byte[] iv;
    @ColumnInfo(name = "url")
    public String url;
    @ColumnInfo(name = "remark")
    public String remark;

}
