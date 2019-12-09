package com.example.letitgoat.ui.home;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.letitgoat.ui.home.buy_recycler.BuyRecyclerFragment;

import java.util.List;

@SuppressWarnings("deprecation")
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<String> titleList;

    public ViewPagerAdapter(FragmentManager fm, List<String> titleList) {
        super(fm);
        this.titleList = titleList;
    }

    @Override
    public int getCount() {
        return titleList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return BuyRecyclerFragment.newInstance(titleList.get(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
}
