package com.lingdeqin.secrets.helper;

public class ClipboardHelper {

    private static ClipboardHelper instance;

    public static synchronized ClipboardHelper getInstance(){
        if (instance == null){
            instance = new ClipboardHelper();
        }
        return instance;
    }


}
