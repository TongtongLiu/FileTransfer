package com.network.filetransfer;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragmentsList;

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragmentsList = fragments;
    }

    @Override
    public Fragment getItem(int index) {
        return fragmentsList.get(index);
    }

    @Override
    public int getCount() {
        return fragmentsList.size();
    }

}
