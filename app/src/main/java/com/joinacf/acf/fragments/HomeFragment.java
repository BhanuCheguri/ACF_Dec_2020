package com.joinacf.acf.fragments;

import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.joinacf.acf.activities.NewComplaintActivity;
import com.joinacf.acf.activities.NewLoginActivity;
import com.joinacf.acf.activities.ProfileActivity;
import com.joinacf.acf.adapters.HomePageAdapter;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.R;
import com.joinacf.acf.databinding.FragmentHomeBinding;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by priya.cheguri on 8/14/2019.
 */

public class HomeFragment extends BaseFragment implements SearchView.OnQueryTextListener {

    FragmentHomeBinding dataBiding;
    private OnFragmentInteractionListener listener;
    private GoogleApiClient mGoogleApiClient;
    boolean bFirst;
    String strPersonName,strPersonEmail,strLoginType;
    int mLastFirstVisibleItem;
    int mLastVisibleItemCount;


    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    APIRetrofitClient apiRetrofitClient;
    ArrayList<WallPostsModel> lstWallPost;
    HomePageAdapter adapter;
    List<WallPostsModel> myProfileData;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        dataBiding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, null, false);
        //dataBiding.search.clearFocus();
        //setSupportActionBar(dataBiding.toolBar);



        loadSharedPrefference();
        init();
        LoadAdapter();

        dataBiding.lvHomeFeed.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.e(getClass().toString(), "scrolling up");
                } else if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.e(getClass().toString(), "scrolling down");
                } else if (mLastVisibleItemCount < visibleItemCount) {
                    Log.e(getClass().toString(), "scrolling down");
                } else if (mLastVisibleItemCount > visibleItemCount) {
                    Log.e(getClass().toString(), "scrolling up");
                }
                mLastFirstVisibleItem = firstVisibleItem;
                mLastVisibleItemCount = visibleItemCount;
            }

            public void onScrollStateChanged(AbsListView listView, int scrollState) {
            }
        });
        return dataBiding.getRoot();
    }

    private void showWelcomeAlert(String strPersonName) {

        ViewGroup viewGroup = getActivity().findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_welcome, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        TextView login_name = (TextView) dialogView.findViewById(R.id.login_name);
        login_name.setText(strPersonName);

        Handler mHandler = new Handler();
        Runnable mRunnable = new Runnable () {

            public void run() {
                if(alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
            }
        };
        mHandler.postDelayed(mRunnable,2000);

    }

    private void loadSharedPrefference() {

        bFirst = getBooleanSharedPreference(getActivity(), "FirstTime");
        strLoginType = getStringSharedPreference(getActivity(), "LoginType");
        strPersonName = getStringSharedPreference(getActivity(), "PersonName");
        //Toast.makeText(getActivity(),strPersonName ,Toast.LENGTH_LONG).show();
        strPersonEmail = getStringSharedPreference(getActivity(), "personEmail");
        if (bFirst) {
            //showWelcomeAlert(strPersonName);
        }
    }

    private void  init() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void LoadAdapter()
    {
        showProgressDialog(getActivity());
        getWallPostDetails("-1","-1");

        dataBiding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), NewComplaintActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }

    public ArrayList<WallPostsModel> getWallPostDetails(String categoryID, String Days) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<List<WallPostsModel>> call = api.getWallPostDetails(categoryID, Days);

        call.enqueue(new Callback<List<WallPostsModel>>() {
            @Override
            public void onResponse(Call<List<WallPostsModel>> call, Response<List<WallPostsModel>> response) {
                if(response != null) {
                    myProfileData = response.body();
                    if(myProfileData.size() > 0) {
                        dataBiding.llNoData.setVisibility(View.GONE);
                        lstWallPost = new ArrayList<WallPostsModel>();
                        for (Object object : myProfileData) {
                            lstWallPost.add((WallPostsModel) object);
                        }
                        populateListView(lstWallPost);
                    }else {
                        dataBiding.llNoData.setVisibility(View.VISIBLE);
                        hideProgressDialog(getActivity());
                    }
                }else {
                    dataBiding.llNoData.setVisibility(View.VISIBLE);
                    hideProgressDialog(getActivity());
                }
            }

            @Override
            public void onFailure(Call<List<WallPostsModel>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(getActivity());
            }
        });
        return lstWallPost;
    }

    private void populateListView(ArrayList<WallPostsModel> wallPostData) {
        adapter = new HomePageAdapter(getActivity(),wallPostData);
        dataBiding.lvHomeFeed.setAdapter(adapter);
        hideProgressDialog(getActivity());
    }


    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.menu, menu);
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(adapter != null)
                    adapter.getFilter().filter(newText.toString());
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:

                break;
            case R.id.myprofile:
                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    intent = new Intent(getContext(), ProfileActivity.class);
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


    public interface OnFragmentInteractionListener {
    }

    /*@Override
    public void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }*/

    public void signOut()
    {
       /* if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }*/
        try {
            if (strLoginType.equalsIgnoreCase("Google")) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                googleSignInClient.signOut();
                putBooleanSharedPreference(getActivity(), "LoggedIn",false);
                Toast.makeText(getApplicationContext(), "User Logged out successfully", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getActivity(), NewLoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            } else {
                FacebookSdk.sdkInitialize(getApplicationContext());
                AppEventsLogger.activateApp(getActivity());
                LoginManager.getInstance().logOut();
                putBooleanSharedPreference(getActivity(), "LoggedIn",false);

                Intent intent = new Intent(getActivity(), NewLoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
    }
}