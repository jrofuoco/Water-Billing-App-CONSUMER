package com.example.consumerapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
    private String accountno; // <-- add this

    public PagerAdapter(FragmentManager fm, String displayName) {
        super(fm);
        this.accountno = displayName; // <-- store the value
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Page1Fragment();
            case 1:
                return Page2Fragment.newInstance(accountno, "");
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
