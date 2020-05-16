package com.unimi.lim.hmi.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.adapter.TimbreListViewAdapter;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import java.util.Objects;

import static com.unimi.lim.hmi.util.Constant.Settings.DEFAULT_TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.Settings.SELECTED_TIMBRE_ID;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class TimbreListFragment extends Fragment {

    private OnTimbreListListener timbreClickListener;

    public static Fragment newInstance() {
        return new TimbreListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timbre_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();

            // Configure RecyclerView
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            DividerItemDecoration itemDecor = new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(itemDecor);

            // Create timbre adapter and setup timbre list observer
            TimbreViewModel viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(TimbreViewModel.class);
            viewModel.selectAll().observe(getViewLifecycleOwner(), timbres -> {
                // Retrieves selected timbre from preferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                String selectedTimbreId = sharedPreferences.getString(SELECTED_TIMBRE_ID, DEFAULT_TIMBRE_ID);

                // Setup the adapter
                TimbreListViewAdapter timbreListViewAdapter = new TimbreListViewAdapter(timbres, timbreClickListener, selectedTimbreId);
                recyclerView.setAdapter(timbreListViewAdapter);
            });
        }
    }

    /**
     * Setup timbre click listener
     *
     * @param context application context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof OnTimbreListListener)) {
            throw new RuntimeException(context.toString() + " must implement OnTimbreListListener");
        }
        timbreClickListener = (OnTimbreListListener) context;
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

        void onSelect(Timbre item);

        void onEdit(Timbre item);
    }
}
