package uk.bowdlerize.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.bowdlerize.MainActivity;
import uk.bowdlerize.R;

public class StatsFragment extends Fragment
{
    public StatsFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);

        ((TextView) rootView.findViewById(R.id.censoredCount)).setText(Integer.toString(settings.getInt("censoredCount", 0)));
        ((TextView) rootView.findViewById(R.id.checkedCount)).setText(Integer.toString(settings.getInt("checkedCount", 0)));

        return rootView;
    }
}
