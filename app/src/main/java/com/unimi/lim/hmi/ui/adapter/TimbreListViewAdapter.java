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

public class TimbreListViewAdapter extends RecyclerView.Adapter<TimbreListViewAdapter.ViewHolder> {

    private final List<Timbre> timbreList;
    private final OnTimbreListClickListener timbreClickListener;

    public TimbreListViewAdapter(List<Timbre> items, OnTimbreListClickListener listener) {
        timbreList = items;
        timbreClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_timbre_list_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Associate timbre values to view adapter fields
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = timbreList.get(position);
        holder.mContentView.setText(timbreList.get(position).getContent());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getClass().getName(), "Clicked item with id " + holder.mItem.getId());
                if (timbreClickListener != null) {
                    timbreClickListener.onTimbreClicked(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return timbreList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // TODO implement real version
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
