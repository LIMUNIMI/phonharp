package com.unimi.lim.hmi.ui.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.adapter.TimbreListViewAdapter;

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
    private TimbreDao timbreDao;
    private TimbreListViewAdapter timbreListViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TimbreListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TimbreListFragment newInstance(int columnCount) {
        Log.d("TIMBRE_FRAGMENT", "new Instance");
        TimbreListFragment fragment = new TimbreListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timbreDao = TimbreDao.getInstance();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(getClass().getName(), " --> onCreateView");

        View view = inflater.inflate(R.layout.fragment_timbre_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            timbreListViewAdapter = new TimbreListViewAdapter(timbreDao.selectAll(), mListener);
            recyclerView.setAdapter(timbreListViewAdapter);
        }
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        // Reload list
        timbreListViewAdapter.notifyDataSetChanged();
        Log.d(getClass().getName(), " --> onResume");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTimbreListClickListener) {
            mListener = (OnTimbreListClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTimbreListClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
