package org.pagemeeting.App2011;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class AbstractNavigationFragment extends ListFragment {
    private static final String TAG = AbstractNavigationFragment.class.getSimpleName();
    private static final String NAVIGATION_VALUES = "navigation-values";

    OnAbstractSelectedListener mCallback;

    // Container Activity must implement this interface
    public interface OnAbstractSelectedListener {
        public void onAbstractSelected(String id);
    }

    public AbstractNavigationFragment() {
        // Required empty public constructor
    }

    public static AbstractNavigationFragment newInstance(ArrayList<AbstractAdapter.AbstractItem> navigationValues) {
        AbstractNavigationFragment fragment = new AbstractNavigationFragment();
        Bundle args = new Bundle();
        args.putSerializable(NAVIGATION_VALUES, navigationValues);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_abstractnavigation, container, false);

        ArrayList<AbstractAdapter.AbstractItem> navigationValues = (ArrayList<AbstractAdapter.AbstractItem>) getArguments().getSerializable(NAVIGATION_VALUES);
        AbstractAdapter adapter = new AbstractAdapter(getActivity().getApplicationContext(), R.layout.abstract_item, navigationValues);
        this.setListAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.d(TAG, "Clicked list item: " + position);

        ArrayList<AbstractAdapter.AbstractItem> navigationValues = (ArrayList<AbstractAdapter.AbstractItem>) getArguments().getSerializable(NAVIGATION_VALUES);

        if (navigationValues != null && navigationValues.size() > position) {
            AbstractAdapter.AbstractItem item = navigationValues.get(position);
            if (item.HasAbstract)
                mCallback.onAbstractSelected(item.Id);
        }
    }

    @Override
    public void onAttach (Context context) {
        super.onAttach(context);

        try {
            //mCallback = (OnNavigationSelectedListener) context;
            mCallback = (OnAbstractSelectedListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }
}
