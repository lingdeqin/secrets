package com.lingdeqin.secrets.ui.nosecret;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.core.room.AppDatabase;
import com.lingdeqin.secrets.core.room.entity.Secret;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class NoSecretFragment extends Fragment {

    private static final String TAG = "NoSecretFragment";

    public NoSecretFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null) {
            int sid = bundle.getInt("sid");
            Log.i(TAG, "onCreate: sid = " + sid);
            AppDatabase appDatabase = AppDatabase.getInstance();
            Secret secret = appDatabase.secretDao().getSecretBySid(sid);
            Log.i(TAG, "onCreate: " + secret.account);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_no_secret, container, false);
    }
}