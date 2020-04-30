package com.unimi.lim.hmi.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.fragment.TimbreListFragment;
import com.unimi.lim.hmi.util.TimbreUtils;

import java.util.List;

public class TimbreListViewAdapter extends RecyclerView.Adapter<TimbreListViewAdapter.ViewHolder> {

    private final List<Timbre> timbreList;
    private final TimbreListFragment.OnTimbreListListener timbreClickListener;

    private Timbre checked;

    public TimbreListViewAdapter(List<Timbre> items, TimbreListFragment.OnTimbreListListener listener, String selectedTimbreId) {
        this.timbreList = items;
        this.timbreClickListener = listener;

        // Setup checked item
        Log.d(getClass().getName(), "Setup adapter, checked item " + selectedTimbreId);
        items.stream().filter(t -> selectedTimbreId.equalsIgnoreCase(t.getId())).findFirst().ifPresent(t -> t.setChecked(true));
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
        holder.timbre = timbreList.get(position);

        // Bind model to view holder
        holder.title.setText(timbreList.get(position).getName());
        holder.desc.setText(TimbreUtils.buildDescription(holder.timbre));

        // Show check marker
        addCheckMarker(holder);

        // Radio button click listener
        holder.radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove check marker from previous item
                if (checked != null) {
                    checked.setChecked(false);
                    notifyDataSetChanged();
                }
                // Set current item as checked
                holder.timbre.setChecked(true);

                // Add check marker
                addCheckMarker(holder);

                // Invoke listener
                Log.d(getClass().getName(), "Select item with id " + holder.timbre.getId());
                timbreClickListener.onSelect(holder.timbre);
            }
        });

        holder.textLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Invoke listener
                Log.d(getClass().getName(), "Edit item with id " + holder.timbre.getId());
                timbreClickListener.onEdit(holder.timbre);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timbreList.size();
    }

    private void addCheckMarker(ViewHolder holder) {
        // Add or remove check marker to provided holder
        // Note that check marker field cannot be set on ViewHolder because ViewHolder instances
        // are recycled by the framework: the number of ViewHolder instances is equals to the number
        // of visible items.
        if (holder.timbre.isChecked()) {
            checked = holder.timbre;
            holder.radio.setChecked(true);
        } else {
            holder.radio.setChecked(false);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final LinearLayout textLayout;
        public final TextView title;
        public final TextView desc;
        public final RadioButton radio;
        public Timbre timbre;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            textLayout = view.findViewById(R.id.timbre_list_item);
            title = view.findViewById(R.id.timbre_list_item_title);
            desc = view.findViewById(R.id.timbre_list_item_desc);
            radio = view.findViewById(R.id.timbre_list_item_rad);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + title.getText() + "'";
        }
    }
}
