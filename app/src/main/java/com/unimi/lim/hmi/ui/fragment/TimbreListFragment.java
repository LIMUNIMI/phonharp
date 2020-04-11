package com.unimi.lim.hmi.ui.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.adapter.TimbreListViewAdapter;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private OnTimbreListClickListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            TimbreViewModel mViewModel = ViewModelProviders.of(getActivity()).get(TimbreViewModel.class);
            Log.d(getClass().getName(), " --> mViewModel " + mViewModel);
            mViewModel.selectAll().observe(getViewLifecycleOwner(), timbres -> {
                TimbreListViewAdapter timbreListViewAdapter = new TimbreListViewAdapter(timbres, mListener);
                recyclerView.setAdapter(timbreListViewAdapter);
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(getClass().getName(), " --> onAttach");
        if (context instanceof OnTimbreListClickListener) {
            mListener = (OnTimbreListClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTimbreListClickListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(getClass().getName(), " --> onDetach");
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTimbreListClickListener {
        void onTimbreClicked(Timbre item);
    }
}
