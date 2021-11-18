package com.lingdeqin.secrets.ui.secrets;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.core.room.entity.Secret;
import com.lingdeqin.secrets.ui.drawable.TextDrawable;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Secret}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MySecretsRecyclerViewAdapter extends RecyclerView.Adapter<MySecretsRecyclerViewAdapter.ViewHolder> {

    private final List<Secret> mValues;
    private Context mContext = null;
    private SecretsFragment secretsFragment = null;

    public MySecretsRecyclerViewAdapter(List<Secret> items, Context context, SecretsFragment sf) {
        mValues = items;
        mContext = context;
        secretsFragment = sf;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_secrets, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).domain);
        holder.mContentView.setText(mValues.get(position).account);
        String text = mValues.get(position).domain.substring(0,1);

        TextDrawable textDrawable = TextDrawable.builder().buildRound(text, Color.RED);
        holder.imageView.setImageDrawable(textDrawable);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                secretsFragment.navSecret(mValues.get(position).sid);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final ImageView imageView;
        public Secret mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
            imageView = (ImageView) view.findViewById(R.id.ic_secret);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}