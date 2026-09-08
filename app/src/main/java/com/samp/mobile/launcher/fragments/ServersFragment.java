package com.samp.mobile.launcher.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.joom.paranoid.Obfuscate;
import com.samp.mobile.R;
import com.samp.mobile.launcher.MainActivity;
import com.samp.mobile.launcher.config.Config;
import com.samp.mobile.launcher.util.ViewPagerWithoutSwipe;

@Obfuscate
public class ServersFragment extends Fragment {

    public final class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return ServerPagesItemFragment.newInstance(0);
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Config.SERVER_NAME;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_servers, container, false);
        ((MainActivity) getActivity()).hideKeyboard(getActivity());

        TabLayout tabLayout = view.findViewById(R.id.tabLayout_servers);
        ViewPagerWithoutSwipe pager = view.findViewById(R.id.viewPager_servers);
        pager.setAdapter(new PagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(pager);
        return view;
    }
}
