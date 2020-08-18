package com.joinacf.acf.fragments;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.joinacf.acf.activities.MainActivity;
import com.joinacf.acf.activities.NewComplaintActivity;
import com.joinacf.acf.activities.NewLoginActivity;
import com.joinacf.acf.activities.MyProfileActivity;
import com.joinacf.acf.adapters.HomePageAdapter;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.R;
import com.joinacf.acf.databinding.FragmentCorruptionBinding;
import com.joinacf.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;
import java.util.List;

/*import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;*/

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by priya.cheguri on 8/14/2019.
 */

public class CorruptionFragment extends BaseFragment{

    FragmentCorruptionBinding dataBiding;
    APIRetrofitClient apiRetrofitClient;
    ArrayList<WallPostsModel.Result> lstWallPost;
    List<WallPostsModel> myProfileData;
    HomePageAdapter adapter;
    boolean bFirst;
    String strPersonName,strPersonEmail,strLoginType;
    int mLastFirstVisibleItem;
    int mLastVisibleItemCount;

    Context context ;
    public static CorruptionFragment newInstance() {
        return new CorruptionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_corruption, container, false);
        dataBiding = DataBindingUtil.inflate(inflater, R.layout.fragment_corruption, null, false);
        context = getContext();
        setActionBarTitle(getString(R.string.title_corruption));

        init();
        LoadAdapter();
        loadSharedPrefference();
        return dataBiding.getRoot();

    }

    private void  init() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        setHasOptionsMenu(true);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //((MainActivity)getActivity()).showBottomNavigation();

        dataBiding.lvCategoryFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    Log.e(getClass().toString(), "scrolling up");
                    ((MainActivity)getActivity()).hideBottomNavigation();
                } else {
                    Log.e(getClass().toString(), "scrolling down");
                    ((MainActivity)getActivity()).showBottomNavigation();
                }
            }
        });
    }

    private void loadSharedPrefference() {

        bFirst = getBooleanSharedPreference(getActivity(), "FirstTime");
        strLoginType = getStringSharedPreference(getActivity(), "LoginType");
        strPersonName = getStringSharedPreference(getActivity(), "PersonName");
        strPersonEmail = getStringSharedPreference(getActivity(), "personEmail");
        if (bFirst) {
            //showWelcomeAlert(strPersonName);
        }
    }

    private void LoadAdapter()
    {
        showProgressDialog(getActivity());
        if(App.isNetworkAvailable())
            getWallPostDetails("1","-1");
        else{
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
                intent.putExtra("Category","Corruption");
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
                if(response != null) {
                    WallPostsModel myWallData  = response.body();
                    if(myWallData != null) {
                        dataBiding.llNoData.setVisibility(View.GONE);
                        String status = myWallData.getStatus();
                        String msg = myWallData.getMessage();
                        if(msg.equalsIgnoreCase("SUCCESS")) {
                            lstWallPost = myWallData.getResult();
                            populateListView(lstWallPost);
                        }else
                        {
                            dataBiding.llNoData.setVisibility(View.VISIBLE);
                            hideProgressDialog(getActivity());
                        }
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
            public void onFailure(Call<WallPostsModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(getActivity());
            }
        });
        // return lstWallPost;
    }

    private void populateListView(ArrayList<WallPostsModel.Result> wallPostData) {
        adapter = new HomePageAdapter(context,wallPostData);
        dataBiding.lvCategoryFeed.setLayoutManager(new LinearLayoutManager(context));
        dataBiding.lvCategoryFeed.setItemAnimator(new DefaultItemAnimator());
        dataBiding.lvCategoryFeed.setNestedScrollingEnabled(false);
        dataBiding.lvCategoryFeed.setAdapter(adapter);
        hideProgressDialog(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.menu, menu);
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.corruption_menu, menu);
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
                try {
                    if (adapter != null)
                        adapter.getFilter().filter(newText.toString());
                }catch (Exception e)
                {
                    Crashlytics.logException(e);
                }
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

    public void signOut()
    {
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
            putBooleanSharedPreference(getActivity(), "FirstTime", false);
            putStringSharedPreference(getActivity(), "LoginType", "");
            putStringSharedPreference(getActivity(), "personName", "");
            putStringSharedPreference(getActivity(), "personEmail", "");
            putStringSharedPreference(getActivity(), "personId", "");
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }

    }
}