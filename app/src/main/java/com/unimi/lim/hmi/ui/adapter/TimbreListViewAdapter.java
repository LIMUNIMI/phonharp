package com.unimi.lim.hmi.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.recyclerview.widget.RecyclerView;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.fragment.TimbreListFragment.OnTimbreListClickListener;

import java.util.List;

public class TimbreListViewAdapter extends RecyclerView.Adapter<TimbreListViewAdapter.ViewHolder> {

    private final List<Timbre> timbreList;
    private final OnTimbreListClickListener timbreClickListener;

    private Timbre checked;
    private boolean showChecked;

    public TimbreListViewAdapter(List<Timbre> items, OnTimbreListClickListener listener, boolean showChecked, String selectedTimbreId) {
        this.timbreList = items;
        this.timbreClickListener = listener;
        this.showChecked = showChecked;
        if (showChecked) {
            items.stream().filter(t -> selectedTimbreId.equalsIgnoreCase(t.getId())).findFirst().ifPresent(t -> t.setChecked(true));
        }
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

        // Bind model to view holder
        holder.mContentView.setText(timbreList.get(position).getContent());

        // Show check marker
        addCheckMarker(holder);

        // Setup click listener
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showChecked) {
                    // Remove check marker from previous item
                    if (checked != null) {
                        checked.setChecked(false);
                        notifyDataSetChanged();
                    }
                    // Set current item as checked
                    holder.mItem.setChecked(true);

                    // Add check marker
                    addCheckMarker(holder);
                }

                // Invoke listener
                Log.d(getClass().getName(), "Clicked item with id " + holder.mItem.getId());
                timbreClickListener.onTimbreClicked(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timbreList.size();
    }

    private void addCheckMarker(ViewHolder holder) {
        // Add or remove check marker to provided holder
        if (showChecked && holder.mItem.isChecked()) {
            checked = holder.mItem;
            holder.mContentView.setChecked(true);
            holder.mContentView.setCheckMarkDrawable(R.drawable.ic_check_black_24dp);
        } else {
            holder.mContentView.setChecked(false);
            holder.mContentView.setCheckMarkDrawable(R.drawable.ic_uncheck_white_24dp);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // TODO implement real version
        public final View mView;
        public final CheckedTextView mContentView;
        public Timbre mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.timbre_list_item);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
