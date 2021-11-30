package com.lingdeqin.secrets.helper;

public class ClipboardHelper {

    private static ClipboardHelper instance;
    private static final Object lock = new Object();

    public static ClipboardHelper getInstance(){
        if (instance == null){
            synchronized (lock){
                if (instance == null){
                    instance = new ClipboardHelper();
                }
            }
        }
        return instance;
    }




}
