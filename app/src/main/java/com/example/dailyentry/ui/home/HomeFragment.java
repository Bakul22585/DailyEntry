package com.example.dailyentry.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.dailyentry.MainActivity;
import com.example.dailyentry.R;
import com.example.dailyentry.SessionManagement;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    TabLayout tabLayout;
    TabItem AllTransaction, CashTransaction, BankTransaction;
    ViewPager viewPager;
    TransactionPagerAdapter transactionPagerAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        tabLayout = root.findViewById(R.id.homeScreenTabBar);
        AllTransaction = root.findViewById(R.id.allTransaction);
        CashTransaction = root.findViewById(R.id.cashTransaction);
        BankTransaction = root.findViewById(R.id.bankTransaction);
        viewPager = root.findViewById(R.id.homeScreenTransactionViewPager);

        /*View navBar = inflater.inflate(R.layout.activity_main, container, false);
        NavigationView navigationView = navBar.findViewById(R.id.nav_view);

        View MenuHeader =  navigationView.getHeaderView(0);
        TextView menu_fullName = (TextView)MenuHeader.findViewById(R.id.loginUserFullName);
        TextView menu_user_email = (TextView)MenuHeader.findViewById(R.id.loginUserEmail);
        ImageView menu_user_img = MenuHeader.findViewById(R.id.loginUserImageView);


        SessionManagement sessionManagement = new SessionManagement(getActivity());
        String LoginUserFullName = sessionManagement.getSession("FirstName") +" "+ sessionManagement.getSession("LastName");
        String LoginUserEmail = sessionManagement.getSession("email");
        String LoginUserImg = sessionManagement.getSession("Img");

        if (!LoginUserImg.equals("")) {
            Picasso.get().load("http://restrictionsolution.com/ci/DailyEntry/"+ LoginUserImg).into(menu_user_img);
        }

        menu_fullName.setText(LoginUserFullName);
        menu_user_email.setText(LoginUserEmail);*/

        transactionPagerAdapter = new TransactionPagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(transactionPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                transactionPagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return root;
    }
}