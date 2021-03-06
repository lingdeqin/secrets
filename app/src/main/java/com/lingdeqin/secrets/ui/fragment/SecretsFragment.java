package com.lingdeqin.secrets.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lingdeqin.secrets.ui.MainActivity;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.core.room.entity.Secret;
import com.lingdeqin.secrets.ui.adapter.MySecretsRecyclerViewAdapter;
import com.lingdeqin.secrets.ui.viewmodel.SecretsViewModel;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class SecretsFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = "=====SecretsFragment=====:";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private FloatingActionButton fab;

    private SecretsViewModel secretsViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SecretsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SecretsFragment newInstance(int columnCount) {
        SecretsFragment fragment = new SecretsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_secrets_list, container, false);
        fab = getActivity().findViewById(R.id.fab_add);
        SecretsFragment secretsFragment = this;
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            secretsViewModel = new ViewModelProvider(this.getActivity()).get(SecretsViewModel.class);
            secretsViewModel.getSecrets().observe(this.getActivity(), new Observer<List<Secret>>() {
                @Override
                public void onChanged(List<Secret> list) {
                    recyclerView.setAdapter(new MySecretsRecyclerViewAdapter(list,context,secretsFragment));
                }
            });

        }
        return view;
    }

    public void navSecret(int sid){
        MainActivity parentActivity = (MainActivity) getActivity();
        parentActivity.navSecret(sid);
    }

    private void fabAdd(Boolean isShow){
        MainActivity parentActivity = (MainActivity) getActivity();
        parentActivity.fabAdd(isShow);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        fabAdd(true);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //fabAdd(true);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
        fabAdd(false);
    }


}