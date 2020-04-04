package com.joinacf.acf.activities;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.joinacf.acf.R;
import com.joinacf.acf.bottom_nav.BottomNavigationViewNew;
import com.joinacf.acf.fragments.CorruptionFragment;
import com.joinacf.acf.fragments.FindnFixFragment;
import com.joinacf.acf.fragments.HomeFragment;
import com.joinacf.acf.fragments.MoreGridFragment;
import com.joinacf.acf.fragments.SocialEvilFragment;

public class MainActivity extends BaseActivity {

    private TextView mTextMessage;
    Fragment fragment = null;

    private BottomNavigationViewNew.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationViewNew.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadHomeFragment();
                    return true;
                case R.id.navigation_corruption:
                    loadCorruptionFragment();
                    return true;
                case R.id.navigation_findnfix:
                    loadFinnFixFragment();
                    return true;
                case R.id.navigation_socialevil:
                    loadSocialEvilFragment();
                    return true;
                case R.id.navigation_more:
                    loadMoreFragment();
                    return true;
            }
            return false;
        }

    };


    private void loadHomeFragment() {

        HomeFragment fragment = HomeFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    private void loadCorruptionFragment() {

        CorruptionFragment fragment = CorruptionFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    private void loadFinnFixFragment() {

        FindnFixFragment fragment = FindnFixFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }
    private void loadSocialEvilFragment() {

        SocialEvilFragment fragment = SocialEvilFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    private void loadMoreFragment() {

        MoreGridFragment fragment = MoreGridFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationViewNew navigation = (BottomNavigationViewNew) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        loadHomeFragment();

        /*BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        //CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        //layoutParams.setBehavior(new BottomNavigationViewBehavior());
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        loadHomeFragment();
                        return true;
                    case R.id.navigation_corruption:
                        loadCorruptionFragment();
                        return true;
                    case R.id.navigation_findnfix:
                        loadFinnFixFragment();
                        return true;
                    case R.id.navigation_socialevil:
                        loadSocialEvilFragment();
                        return true;
                    case R.id.navigation_more:
                        loadMoreFragment();
                        return true;
                }
                return true;
            }
        });*/

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            showAlert(MainActivity.this,"Exit Alert","Do you want to exit from the Application?",true,"Yes","No");
        }
        return true;
    }
}

 /*extends BottomBarHolderActivity implements HomeFragment.OnFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationPage page1 = new NavigationPage("Home", ContextCompat.getDrawable(this, R.drawable.ic_home_black_24dp), HomeFragment.newInstance());
        NavigationPage page2 = new NavigationPage("Corruption", ContextCompat.getDrawable(this, R.drawable.ic_corruption), CorruptionFragment.newInstance());
        //NavigationPage page3 = new NavigationPage("Adulteration", ContextCompat.getDrawable(this, R.drawable.ic_adulteration), AdulterationFragment.newInstance());
        NavigationPage page4 = new NavigationPage("Find n Fix", ContextCompat.getDrawable(this, R.drawable.ic_findnfix), FindnFixFragment.newInstance());
        NavigationPage page5 = new NavigationPage("Social Evil", ContextCompat.getDrawable(this, R.drawable.ic_social_evil), SocialEvilFragment.newInstance());
        NavigationPage page6 = new NavigationPage("More", ContextCompat.getDrawable(this, R.drawable.ic_more), MoreGridFragment.newInstance());

        List<NavigationPage> navigationPages = new ArrayList<>();
        navigationPages.add(page1);
        navigationPages.add(page2);
         navigationPages.add(page4);
        navigationPages.add(page5);
        navigationPages.add(page6);

        super.setupBottomBarHolderActivity(navigationPages);
    }
}*/