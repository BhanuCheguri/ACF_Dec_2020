package com.joinacf.acf.fragments;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.joinacf.acf.R;
import com.joinacf.acf.databinding.FragmentAdulterationBinding;


/**
 * Created by priya.cheguri on 8/14/2019.
 */

public class AdulterationFragment extends BaseFragment {

    FragmentAdulterationBinding dataBiding;


    public static AdulterationFragment newInstance() {
        return new AdulterationFragment();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        dataBiding = DataBindingUtil.inflate(inflater, R.layout.fragment_adulteration, null, false);

        setActionBarTitle(getString(R.string.title_adulteration));

        return dataBiding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}