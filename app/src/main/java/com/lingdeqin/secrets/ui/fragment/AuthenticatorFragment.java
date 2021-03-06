package com.lingdeqin.secrets.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.ui.viewmodel.AuthenticatorViewModel;

public class AuthenticatorFragment extends Fragment {

    private AuthenticatorViewModel authenticatorViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        authenticatorViewModel =
                new ViewModelProvider(this).get(AuthenticatorViewModel.class);
        View root = inflater.inflate(R.layout.fragment_authenticator, container, false);
        final TextView textView = root.findViewById(R.id.text_authenticator);
        authenticatorViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}