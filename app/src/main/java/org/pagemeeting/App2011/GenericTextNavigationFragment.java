package org.pagemeeting.App2011;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class GenericTextNavigationFragment extends ListFragment {
    private static final String TAG = GenericTextNavigationFragment.class.getSimpleName();

    public static final String NAVIGATION_TYPE = "navigation-type";
    public static final String NAVIGATION_VALUES = "navigation-values";

    private OnNavigationSelectedListener mCallback;

    // Container Activity must implement this interface
    public interface OnNavigationSelectedListener {
        public void onNavigationSelected(String navigationType, String id);
    }

    public GenericTextNavigationFragment() {
        // Required empty public constructor
    }

    public static GenericTextNavigationFragment newInstance(String navigationType, NavigationItem[] navigationValues) {
        GenericTextNavigationFragment fragment = new GenericTextNavigationFragment();
        Bundle args = new Bundle();
        args.putString(NAVIGATION_TYPE, navigationType);
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
        View view = inflater.inflate(R.layout.fragment_generictextnavigation, container, false);

        NavigationItem[] navigationValues = (NavigationItem[]) getArguments().getSerializable(NAVIGATION_VALUES);
        ArrayList<String> values = new ArrayList<String>();
        for (int i=0 ; i<navigationValues.length; i++)
            values.add(navigationValues[i].Text);

        AlphabeticalAdapter adapter = new AlphabeticalAdapter(getActivity().getApplicationContext(), R.layout.text_item, values);
        this.setListAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        ListView listView = getListView();
        if (listView.getCount() > 50)
            getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.d(TAG, "Clicked list item: " + position);

        Bundle arguments = getArguments();
        if (arguments != null) {
            String type = arguments.getString(NAVIGATION_TYPE);
            NavigationItem[] values = (NavigationItem[]) getArguments().getSerializable(NAVIGATION_VALUES);

            if (type != null && values != null && values.length > position)
                mCallback.onNavigationSelected(type, values[position].Id);
        }
    }

    @Override
    public void onAttach (Context context) {
        super.onAttach(context);

        try {
            //mCallback = (OnNavigationSelectedListener) context;
            mCallback = (OnNavigationSelectedListener) getParentFragment();
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

    public static class NavigationItem implements Serializable {
        public String Id, Text;

        public NavigationItem(String id, String text) {
            Id = id;
            Text = text;
        }
    }

    class AlphabeticalAdapter extends ArrayAdapter<String> implements SectionIndexer
    {
        private HashMap<String, Integer> alphaIndexer;
        private String[] sections;

        public AlphabeticalAdapter(Context c, int resource, List<String> data)
        {
            super(c, resource, data);
            alphaIndexer = new HashMap<String, Integer>();
            for (int i = 0; i < data.size(); i++)
            {
                String s = data.get(i).substring(0, 1).toUpperCase();
                if (!alphaIndexer.containsKey(s))
                    alphaIndexer.put(s, i);
            }

            Set<String> sectionLetters = alphaIndexer.keySet();
            ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
            Collections.sort(sectionList);
            sections = new String[sectionList.size()];
            for (int i = 0; i < sectionList.size(); i++)
                sections[i] = sectionList.get(i);
        }

        public int getPositionForSection(int section)
        {
            return alphaIndexer.get(sections[section]);
        }

        public int getSectionForPosition(int position)
        {
            for ( int i = sections.length - 1; i >= 0; i-- ) {
                if ( position >= alphaIndexer.get( sections[ i ] ) ) {
                    return i;
                }
            }
            return 0;
        }

        public Object[] getSections()
        {
            return sections;
        }
    }
}
