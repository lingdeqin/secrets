package com.lingdeqin.secrets.ui.secret;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lingdeqin.secrets.R;

public class SecretFragment extends Fragment {

    private SecretViewModel mViewModel;

    public static SecretFragment newInstance() {
        return new SecretFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_secret, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //TextView tv = getView().findViewById(R.id.testtext);
        //String dd = getArguments().getString("id");
        //tv.setText(dd);
        mViewModel = new ViewModelProvider(this).get(SecretViewModel.class);
        // TODO: Use the ViewModel
        

    }

}