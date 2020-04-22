package com.unimi.lim.hmi.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.TimbreCfg;
import com.unimi.lim.hmi.ui.adapter.TimbreListViewAdapter;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import static com.unimi.lim.hmi.util.Constant.Settings.SELECTED_TIMBRE_ID;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class TimbreListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private OnTimbreListListener timbreClickListener;

    public static Fragment newInstance() {
        return new TimbreListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO remove useless code
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timbre_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO remove useless code

        Log.d(getClass().getName(), "--> VIEW CREATED");

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(itemDecor);

            // Create timbre adapter and setup timbre list observer
            TimbreViewModel viewModel = ViewModelProviders.of(getActivity()).get(TimbreViewModel.class);
            viewModel.selectAll().observe(getViewLifecycleOwner(), timbres -> {
                // Retrieves selected timbre from preferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                String selectedTimbreId = sharedPreferences.getString(SELECTED_TIMBRE_ID, "");

                // Setup the adapter
                TimbreListViewAdapter timbreListViewAdapter = new TimbreListViewAdapter(timbres, timbreClickListener, selectedTimbreId);
                recyclerView.setAdapter(timbreListViewAdapter);
            });
        }
    }

    /**
     * Setup timbre click listener
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(getClass().getName(), "--> ON ATTACH");

        if (context instanceof OnTimbreListListener) {
            timbreClickListener = (OnTimbreListListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTimbreListListener");
        }
    }

    /**
     * Reset timbre click listener
     */
    @Override
    public void onDetach() {
        super.onDetach();
        timbreClickListener = null;
    }

    /**
     * Interface to handles timbre list click
     */
    public interface OnTimbreListListener {
        void onSelect(TimbreCfg item);

        void onEdit(TimbreCfg item);
    }
}
