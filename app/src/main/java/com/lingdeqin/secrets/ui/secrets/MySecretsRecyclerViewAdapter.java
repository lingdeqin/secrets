package com.lingdeqin.secrets.ui.secrets;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lingdeqin.secrets.MainActivity;
import com.lingdeqin.secrets.R;
import com.lingdeqin.secrets.ui.secrets.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MySecretsRecyclerViewAdapter extends RecyclerView.Adapter<MySecretsRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private Context mContext = null;
    private SecretsFragment secretsFragment = null;

    public MySecretsRecyclerViewAdapter(List<DummyItem> items, Context context, SecretsFragment sf) {
        mValues = items;
        mContext = context;
        secretsFragment = sf;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_secrets, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext, "position:"+position+", id:"+mValues.get(position).id
                     //   +", content:"+mValues.get(position).content+
                    //    ", details:"+mValues.get(position).details, Toast.LENGTH_SHORT).show();
               // navController = Navigation.findNavController(MainActivity., R.id.nav_host_fragment);
                //NavController controller = Navigation.findNavController(MainActivity.this, R.id.);
                secretsFragment.navSecret(mValues.get(position).id);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}