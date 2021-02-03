package com.anticorruptionforce.acf.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.anticorruptionforce.acf.activities.MainActivity;
import com.anticorruptionforce.acf.activities.NewComplaintActivity;
import com.anticorruptionforce.acf.activities.NewLoginActivity;
import com.anticorruptionforce.acf.activities.MyProfileActivity;
import com.anticorruptionforce.acf.adapters.HomePageAdapter;
import com.anticorruptionforce.acf.modelclasses.WallPostsModel;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.databinding.FragmentHomeBinding;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.anticorruptionforce.acf.utilities.App;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by priya.cheguri on 8/14/2019.
 */

public class HomeFragment extends BaseFragment implements SearchView.OnQueryTextListener {

    FragmentHomeBinding dataBiding;
    String strPersonName, strPersonEmail, strLoginType;
    int mLastFirstVisibleItem;
    int mLastVisibleItemCount;
    Context context;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    APIRetrofitClient apiRetrofitClient;
    ArrayList<WallPostsModel.Result> lstWallPost;
    HomePageAdapter adapter;
    String m_strResult = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        dataBiding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, null, false);

        context = getContext();
        loadSharedPrefference();
        init();
        LoadAdapter();

        return dataBiding.getRoot();
    }

    private void loadSharedPrefference() {

        strLoginType = getStringSharedPreference(getActivity(), "LoginType");
        strPersonName = getStringSharedPreference(getActivity(), "PersonName");
        strPersonEmail = getStringSharedPreference(getActivity(), "personEmail");
    }

    private void init() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ((MainActivity) getActivity()).showBottomNavigation();


        dataBiding.lvHomeFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    Log.e(getClass().toString(), "scrolling up");
                    ((MainActivity) getActivity()).hideBottomNavigation();
                } else {
                    Log.e(getClass().toString(), "scrolling down");
                    ((MainActivity) getActivity()).showBottomNavigation();
                }
            }
        });

        dataBiding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                LoadAdapter();
            }
        });
        // Configure the refreshing colors
        dataBiding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void LoadAdapter() {
        showProgressDialog(getActivity());
        if (App.isNetworkAvailable())
            getWallPostDetails("-1", "-1");
        else {
            ChocoBar.builder().setView(dataBiding.mainLayout)
                    .setText("No Internet connection")
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    //.setActionText(android.R.string.ok)
                    .red()   // in built red ChocoBar
                    .show();
        }
        dataBiding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), NewComplaintActivity.class);
                intent.putExtra("Category", "");
                getActivity().startActivity(intent);

            }
        });
    }

    public void getWallPostDetails(String categoryID, String Days) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<WallPostsModel> call = api.getWallPostDetails(categoryID, Days);

        call.enqueue(new Callback<WallPostsModel>() {
            @Override
            public void onResponse(Call<WallPostsModel> call, Response<WallPostsModel> response) {
                System.out.println("getWallPostDetails::" + response);
                hideProgressDialog(getActivity());
                if (response != null) {
                    WallPostsModel myWallData = response.body();
                    if (myWallData != null) {
                        dataBiding.llNoData.setVisibility(View.GONE);
                        String status = myWallData.getStatus();
                        String msg = myWallData.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstWallPost = myWallData.getResult();
                            populateListView(lstWallPost);
                        } else
                            dataBiding.llNoData.setVisibility(View.VISIBLE);
                    } else
                        dataBiding.llNoData.setVisibility(View.VISIBLE);
                } else
                    dataBiding.llNoData.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<WallPostsModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(getActivity());
            }
        });
    }

    private void populateListView(ArrayList<WallPostsModel.Result> wallPostData) {
        adapter = new HomePageAdapter(context, wallPostData);
        dataBiding.lvHomeFeed.setLayoutManager(new LinearLayoutManager(context));
        dataBiding.lvHomeFeed.setItemAnimator(new DefaultItemAnimator());
        dataBiding.lvHomeFeed.setNestedScrollingEnabled(false);
        dataBiding.lvHomeFeed.setAdapter(adapter);
        hideProgressDialog(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                if (adapter!=null)
                    adapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                break;
            case R.id.myprofile:
                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    intent = new Intent(getContext(), MyProfileActivity.class);
                }
                getActivity().startActivity(intent);
                break;

            case R.id.logout:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void signOut() {
        try {
            if (strLoginType.equalsIgnoreCase("Google")) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                googleSignInClient.signOut();
                putBooleanSharedPreference(getActivity(), "LoggedIn", false);
                putStringSharedPreference(getActivity(),"mobile","");
                Toast.makeText(getApplicationContext(), "User Logged out successfully", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getActivity(), NewLoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            } else {
                FacebookSdk.sdkInitialize(getApplicationContext());
                AppEventsLogger.activateApp(getActivity());
                LoginManager.getInstance().logOut();
                putBooleanSharedPreference(getActivity(), "LoggedIn", false);

                Intent intent = new Intent(getActivity(), NewLoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
            putBooleanSharedPreference(getActivity(), "FirstTime", false);
            putStringSharedPreference(getActivity(), "LoginType", "");
            putStringSharedPreference(getActivity(), "personName", "");
            putStringSharedPreference(getActivity(), "personEmail", "");
            putStringSharedPreference(getActivity(), "personId", "");
        } catch (Exception e) {
            e.printStackTrace();
            //FirebaseCrashlytics.getInstance().setCustomKey("CorruptionFragment", e.getMessage());
        }
    }

    /*@Override
    public void onResume() {
        super.onResume();
        Log.e("FromFragment","resume");
        if(App.isNetworkAvailable())
            getWallPostDetails("-1","-1");
        else{
            ChocoBar.builder().setView(dataBiding.mainLayout)
                    .setText("No Internet connection")
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    //.setActionText(android.R.string.ok)
                    .red()   // in built red ChocoBar
                    .show();
        }
    }*/


    @Override
    public void onResume() {
        Log.e("DEBUG", "onResume of LoginFragment");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e("DEBUG", "OnPause of loginFragment");
        super.onPause();
    }
}