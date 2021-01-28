package org.pagemeeting.App2011;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

public class AbstractAdapter extends ArrayAdapter<AbstractAdapter.AbstractItem> {
    public AbstractAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public AbstractAdapter(Context context, int resource, List<AbstractItem> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.abstract_item, null);
        }

        AbstractItem abstractItem = getItem(position);

        if (abstractItem != null) {
            TextView codeText = (TextView) v.findViewById(R.id.code);
            TextView titleText = (TextView) v.findViewById(R.id.title);
            TextView authorText = (TextView) v.findViewById(R.id.author);
            TextView dateText = (TextView) v.findViewById(R.id.date);

            if (codeText != null) {
                codeText.setText(abstractItem.Code);
            }
            if (titleText != null) {
                titleText.setText(abstractItem.Title);
            }
            if (authorText != null) {
                authorText.setText(abstractItem.Author);
            }
            if (dateText != null) {
                dateText.setText(abstractItem.Date);
            }
            if (!abstractItem.HasAbstract) {
                v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.backgroundColorDisabled));
                v.setClickable(true);
            } else {
                v.setBackgroundColor(0xFFF);
                v.setClickable(false);
            }
        }

        return v;
    }

    public static class AbstractItem implements Serializable {
        public String Id, Code, Title, Author, Date;
        public boolean HasAbstract;

        public AbstractItem(String id, String code, String title, String author, String date, boolean hasAbstract) {
            Id = id;
            Code = code;
            Title = title;
            Author = author;
            Date = date;
            HasAbstract = hasAbstract;
        }
    }
}
