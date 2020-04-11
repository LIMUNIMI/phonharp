package com.unimi.lim.hmi.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.fragment.TimbreListFragment.OnTimbreListClickListener;

import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class TimbreListViewAdapter extends RecyclerView.Adapter<TimbreListViewAdapter.ViewHolder> {

    private final List<Timbre> mValues;
    private final OnTimbreListClickListener mListener;

    public TimbreListViewAdapter(List<Timbre> items, OnTimbreListClickListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_timbre_list_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).getContent());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getClass().getName(), "Clicked item with id " + holder.mItem.getId());
                if (null != mListener) {
                    mListener.onTimbreClicked(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public Timbre mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
