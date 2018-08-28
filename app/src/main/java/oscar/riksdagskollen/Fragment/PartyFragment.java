package oscar.riksdagskollen.Fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import oscar.riksdagskollen.R;
import oscar.riksdagskollen.Util.JSONModel.Party;

/**
 * Created by oscar on 2018-08-28.
 */

public class PartyFragment extends Fragment {

    private Party party;

    public static PartyFragment newInstance(Party party){
        Bundle args = new Bundle();
        args.putParcelable("party",party);
        PartyFragment newInstance = new PartyFragment();
        newInstance.setArguments(args);
        return newInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.party = getArguments().getParcelable("party");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(party.getName());

        View view = inflater.inflate(R.layout.fragment_party,container, false);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Set Tabs inside Toolbar
        TabLayout tabs = view.findViewById(R.id.result_tabs);
        tabs.setupWithViewPager(viewPager);
        return view;

    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        PartyListFragment listFragment = PartyListFragment.newInstance(party);
        PartyInfoFragment infoFragment = PartyInfoFragment.newInstance(party);

        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(listFragment, "Flöde");
        adapter.addFragment(infoFragment, "Info");
        viewPager.setAdapter(adapter);



    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }




}
